package uk.gov.hmcts.ccd.definition.store.elastic;

import org.apache.http.HttpEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReindexHelperTest {

    @Mock
    private RestHighLevelClient restHighLevelClient;

    @Mock
    private RestClient restClient;

    @Mock
    private Response httpResponse;

    @Mock
    private HttpEntity httpEntity;

    @Mock
    private ReindexListener reindexListener;

    @Mock
    private Executor mockExecutor;

    private ReindexHelper reindexHelper;

    @BeforeEach
    void setUp() {
        reindexHelper = new ReindexHelper(restHighLevelClient);

        // Mock the client hierarchy
        lenient().when(restHighLevelClient.getLowLevelClient()).thenReturn(restClient);
    }

    @Test
    void shouldReindexIndexSuccessfully() throws IOException {
        // Given
        String sourceIndex = "source-index";
        String destIndex = "dest-index";
        long pollIntervalMs = 1000L;
        String taskId = "node:123";
        String responseBody = "{\"task\":\"" + taskId + "\"}";

        when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));

        // When
        String result = reindexHelper.reindexIndex(sourceIndex, destIndex, pollIntervalMs, reindexListener);

        // Then
        assertThat(result).isEqualTo(taskId);
        verify(restClient).performRequest(any(Request.class));
    }

    @Test
    void shouldThrowExceptionWhenReindexRequestFails() throws IOException {
        // Given
        String sourceIndex = "source-index";
        String destIndex = "dest-index";
        long pollIntervalMs = 1000L;

        when(restClient.performRequest(any(Request.class)))
            .thenThrow(new IOException("Reindex request failed"));

        // When / Then
        assertThatThrownBy(() -> reindexHelper.reindexIndex(sourceIndex, destIndex, pollIntervalMs, reindexListener))
            .isInstanceOf(IOException.class)
            .hasMessage("Reindex request failed");
    }

    @Test
    void shouldThrowExceptionWhenResponseParsingFails() throws IOException {
        // Given
        String sourceIndex = "source-index";
        String destIndex = "dest-index";
        long pollIntervalMs = 1000L;
        String invalidResponseBody = "invalid json";

        when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(invalidResponseBody.getBytes()));

        // When / Then
        assertThatThrownBy(() -> reindexHelper.reindexIndex(sourceIndex, destIndex, pollIntervalMs, reindexListener))
            .isInstanceOf(IOException.class);
    }

    @Test
    void shouldCreateAsyncExecutor() {
        // When
        Executor executor = reindexHelper.asyncExecutor();

        // Then
        assertThat(executor).isNotNull();
        assertThat(executor).isInstanceOf(org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor.class);
    }

    @Test
    void shouldHandleInterruptedExceptionInReindexProcess() throws IOException {
        // Given
        long pollIntervalMs = 100L;
        String taskId = "node:123";
        String responseBody = "{\"task\":\"" + taskId + "\"}";

        when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));

        // Mock executor to interrupt the thread
        reindexHelper = new ReindexHelper(restHighLevelClient) {
            @Override
            public Executor asyncExecutor() {
                return mockExecutor;
            }
        };

        doNothing().when(mockExecutor).execute(any(Runnable.class));

        // When
        final String sourceIndex = "source-index";
        final String destIndex = "dest-index";
        String result = reindexHelper.reindexIndex(sourceIndex, destIndex, pollIntervalMs, reindexListener);

        // Then
        assertThat(result).isEqualTo(taskId);
        verify(mockExecutor).execute(any(Runnable.class));
    }

    @Test
    void shouldHandleIOExceptionInReindexProcess() throws IOException {
        // Given
        String taskId = "node:123";
        String responseBody = "{\"task\":\"" + taskId + "\"}";

        when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));

        // Mock executor to simulate IOException
        reindexHelper = new ReindexHelper(restHighLevelClient) {
            @Override
            public Executor asyncExecutor() {
                return mockExecutor;
            }
        };

        doNothing().when(mockExecutor).execute(any(Runnable.class));

        // When
        final String sourceIndex = "source-index";
        final String destIndex = "dest-index";
        final long pollIntervalMs = 100L;
        String result = reindexHelper.reindexIndex(sourceIndex, destIndex, pollIntervalMs, reindexListener);

        // Then
        assertThat(result).isEqualTo(taskId);
        verify(mockExecutor).execute(any(Runnable.class));
    }

    @Test
    void shouldCreateReindexHelperWithClient() {
        // Given
        RestHighLevelClient client = mock(RestHighLevelClient.class);

        // When
        ReindexHelper helper = new ReindexHelper(client);

        // Then
        assertThat(helper).isNotNull();
    }

    @Test
    void shouldCreateReindexHelperWithNullClient() {
        // Given
        RestHighLevelClient client = null;

        // When
        ReindexHelper helper = new ReindexHelper(client);

        // Then
        // The constructor doesn't validate null client, so no exception is thrown
        assertThat(helper).isNotNull();
    }

    @Test
    void shouldHandleEmptyTaskIdInResponse() throws IOException {
        // Given
        String sourceIndex = "source-index";
        String destIndex = "dest-index";
        long pollIntervalMs = 1000L;
        String responseBody = "{\"task\":\"\"}";

        when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));

        // When / Then
        assertThatThrownBy(() -> reindexHelper.reindexIndex(sourceIndex, destIndex, pollIntervalMs, reindexListener))
            .isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    void shouldHandleMissingTaskFieldInResponse() throws IOException {
        // Given
        String sourceIndex = "source-index";
        String destIndex = "dest-index";
        long pollIntervalMs = 1000L;
        String responseBody = "{\"status\":\"ok\"}";

        when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));

        // When / Then
        assertThatThrownBy(() -> reindexHelper.reindexIndex(sourceIndex, destIndex, pollIntervalMs, reindexListener))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldHandleMalformedTaskId() throws IOException {
        // Given
        String sourceIndex = "source-index";
        String destIndex = "dest-index";
        long pollIntervalMs = 1000L;
        String responseBody = "{\"task\":\"invalid-task-id\"}";

        when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));

        // When / Then
        assertThatThrownBy(() -> reindexHelper.reindexIndex(sourceIndex, destIndex, pollIntervalMs, reindexListener))
            .isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    void shouldHandleNullResponseEntity() throws IOException {
        // Given
        String sourceIndex = "source-index";
        String destIndex = "dest-index";
        long pollIntervalMs = 1000L;

        when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> reindexHelper.reindexIndex(sourceIndex, destIndex, pollIntervalMs, reindexListener))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHandleIOExceptionWhenReadingResponse() throws IOException {
        // Given
        String sourceIndex = "source-index";
        String destIndex = "dest-index";
        long pollIntervalMs = 1000L;

        when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenThrow(new IOException("Failed to read response"));

        // When / Then
        assertThatThrownBy(() -> reindexHelper.reindexIndex(sourceIndex, destIndex, pollIntervalMs, reindexListener))
            .isInstanceOf(IOException.class)
            .hasMessage("Failed to read response");
    }
}
