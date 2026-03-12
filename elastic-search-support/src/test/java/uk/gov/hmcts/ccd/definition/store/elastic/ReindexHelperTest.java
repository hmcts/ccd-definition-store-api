package uk.gov.hmcts.ccd.definition.store.elastic;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;

import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReindexHelperTest {

    @Mock
    private RestClient restClient;

    @Mock
    private Response httpResponse;

    @Mock
    private HttpEntity httpEntity;

    @Mock
    private ReindexListener listener;

    @Mock
    private CcdElasticSearchProperties config;

    private final Executor syncExecutor = Runnable::run;

    private ReindexHelper reindexHelper;

    @BeforeEach
    void setUp() {
        reindexHelper = new ReindexHelper(restClient, config, syncExecutor);
    }

    private void mockReindexSubmitResponse(String taskId) throws IOException {
        String body = "{\"task\":\"" + taskId + "\"}";
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        when(httpResponse.getEntity()).thenReturn(httpEntity);
    }

    private Response mockTaskResponse(String json) throws IOException {
        Response resp = mock(Response.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        when(resp.getEntity()).thenReturn(entity);
        return resp;
    }

    private ResponseException mock404() {
        return mock404("task not found");
    }

    private ResponseException mock404(String body) {
        ResponseException ex = mock(ResponseException.class);
        Response resp = mock(Response.class);
        StatusLine statusLine = mock(StatusLine.class);

        when(statusLine.getStatusCode()).thenReturn(404);
        when(resp.getStatusLine()).thenReturn(statusLine);
        when(resp.getEntity()).thenReturn(new StringEntity(body, StandardCharsets.UTF_8));
        when(ex.getResponse()).thenReturn(resp);

        return ex;
    }

    @Nested
    class StartReindexTask {

        @Test
        void shouldReturnTaskId() throws IOException {
            mockReindexSubmitResponse("node:123");
            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenReturn(mockTaskResponse("{\"completed\":true,\"response\":{\"total\":0,\"failures\":[]}}"));

            String result = reindexHelper.reindexIndex("src", "dest", 10L, listener);

            assertThat(result).isEqualTo("node:123");
        }

        @Test
        void shouldIncludeBatchSizeInRequestBody() throws IOException {
            when(config.getBatchSize()).thenReturn(2500);
            mockReindexSubmitResponse("node:456");
            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenReturn(mockTaskResponse("{\"completed\":true,\"response\":{\"total\":0,\"failures\":[]}}"));

            reindexHelper.reindexIndex("src-idx", "dest-idx", 10L, listener);

            ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
            verify(restClient, atLeastOnce()).performRequest(captor.capture());

            Request reindexRequest = captor.getAllValues().get(0);
            String jsonBody = EntityUtils.toString(reindexRequest.getEntity());

            assertThat(jsonBody).contains("\"size\": 2500");
            assertThat(jsonBody).contains("\"index\": \"src-idx\"");
            assertThat(jsonBody).contains("\"index\": \"dest-idx\"");
        }

        @Test
        void shouldThrowWhenTaskIdIsEmpty() throws IOException {
            String body = "{\"task\":\"\"}";
            when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
            when(httpResponse.getEntity()).thenReturn(httpEntity);
            when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);

            assertThatThrownBy(() -> reindexHelper.reindexIndex("src", "dest", 10L, listener))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("did not contain a valid task ID");
        }

        @Test
        void shouldThrowWhenTaskFieldIsMissing() throws IOException {
            String body = "{\"status\":\"ok\"}";
            when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
            when(httpResponse.getEntity()).thenReturn(httpEntity);
            when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);

            assertThatThrownBy(() -> reindexHelper.reindexIndex("src", "dest", 10L, listener))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("did not contain a valid task ID");
        }

        @Test
        void shouldThrowWhenNetworkFails() throws IOException {
            when(restClient.performRequest(any(Request.class)))
                .thenThrow(new IOException("Connection refused"));

            assertThatThrownBy(() -> reindexHelper.reindexIndex("src", "dest", 10L, listener))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Connection refused");
        }

        @Test
        void shouldThrowWhenResponseIsUnparseable() throws IOException {
            String body = "not json";
            when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
            when(httpResponse.getEntity()).thenReturn(httpEntity);
            when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);

            assertThatThrownBy(() -> reindexHelper.reindexIndex("src", "dest", 10L, listener))
                .isInstanceOf(IOException.class);
        }
    }

    @Nested
    class MonitorAndCompletion {

        @Test
        void shouldCallOnSuccessWhenTaskCompletesWithoutFailures() throws IOException {
            mockReindexSubmitResponse("node:1");
            String taskDone = "{\"completed\":true,\"response\":{\"total\":100,\"created\":100,"
                + "\"updated\":0,\"batches\":2,\"failures\":[]}}";

            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenReturn(mockTaskResponse(taskDone));

            reindexHelper.reindexIndex("src", "dest", 10L, listener);

            verify(listener).onSuccess();
            verify(listener, never()).onFailure(any());
        }

        @Test
        void shouldCallOnFailureWhenTaskCompletesWithError() throws IOException {
            mockReindexSubmitResponse("node:1");
            String taskError = "{\"completed\":true,\"error\":{\"type\":\"exception\","
                + "\"reason\":\"something broke\"}}";

            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenReturn(mockTaskResponse(taskError));

            reindexHelper.reindexIndex("src", "dest", 10L, listener);

            verify(listener).onFailure(any(RuntimeException.class));
            verify(listener, never()).onSuccess();
        }

        @Test
        void shouldCallOnFailureWhenTaskCompletesWithFailures() throws IOException {
            mockReindexSubmitResponse("node:1");
            String taskFail = "{\"completed\":true,\"response\":{\"total\":10,"
                + "\"failures\":[{\"index\":\"dest\",\"cause\":{\"type\":\"error\"}}]}}";

            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenReturn(mockTaskResponse(taskFail));

            reindexHelper.reindexIndex("src", "dest", 10L, listener);

            verify(listener).onFailure(any(RuntimeException.class));
            verify(listener, never()).onSuccess();
        }

        @Test
        void shouldCallOnFailureWhenTaskCompletesWithUnknownStatus() throws IOException {
            mockReindexSubmitResponse("node:1");
            String taskUnknown = "{\"completed\":true}";

            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenReturn(mockTaskResponse(taskUnknown));

            reindexHelper.reindexIndex("src", "dest", 10L, listener);

            verify(listener).onFailure(any(RuntimeException.class));
            verify(listener, never()).onSuccess();
        }

        @Test
        void shouldPollUntilTaskCompletes() throws IOException {
            mockReindexSubmitResponse("node:1");
            String inProgress = "{\"completed\":false,\"task\":{\"action\":\"indices:data/write/reindex\"}}";
            String done = "{\"completed\":true,\"response\":{\"total\":5,\"created\":5,"
                + "\"updated\":0,\"batches\":1,\"failures\":[]}}";

            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenReturn(mockTaskResponse(inProgress))
                .thenReturn(mockTaskResponse(inProgress))
                .thenReturn(mockTaskResponse(done));

            reindexHelper.reindexIndex("src", "dest", 10L, listener);

            verify(listener).onSuccess();
        }
    }

    @Nested
    class TaskNotFound {

        @Test
        void shouldGiveUpAfterMaxNotFoundRetries() throws IOException {
            mockReindexSubmitResponse("node:1");

            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenThrow(mock404())
                .thenThrow(mock404())
                .thenThrow(mock404())
                .thenThrow(mock404())
                .thenThrow(mock404())
                .thenThrow(mock404())
                .thenThrow(mock404())
                .thenThrow(mock404())
                .thenThrow(mock404())
                .thenThrow(mock404());

            reindexHelper.reindexIndex("src", "dest", 10L, listener);

            verify(listener).onFailure(any(RuntimeException.class));
            verify(listener, never()).onSuccess();
        }

        @Test
        void shouldTreatCompletedWithoutStoredResultsAsSuccess() throws IOException {
            mockReindexSubmitResponse("node:1");
            String notStoredBody = "{\"error\":{\"root_cause\":[{\"type\":\"resource_not_found_exception\","
                + "\"reason\":\"task [node:1] isn't running and hasn't stored its results\"}],"
                + "\"type\":\"resource_not_found_exception\","
                + "\"reason\":\"task [node:1] isn't running and hasn't stored its results\"},\"status\":404}";

            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenThrow(mock404(notStoredBody));

            reindexHelper.reindexIndex("src", "dest", 10L, listener);

            verify(listener).onSuccess();
            verify(listener, never()).onFailure(any());
        }

        @Test
        void shouldRecoverIfTaskAppearsBeforeMaxRetries() throws IOException {
            mockReindexSubmitResponse("node:1");
            String done = "{\"completed\":true,\"response\":{\"total\":1,\"created\":1,"
                + "\"updated\":0,\"batches\":1,\"failures\":[]}}";

            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenThrow(mock404())
                .thenThrow(mock404())
                .thenReturn(mockTaskResponse(done));

            reindexHelper.reindexIndex("src", "dest", 10L, listener);

            verify(listener).onSuccess();
            verify(listener, never()).onFailure(any());
        }
    }

    @Nested
    class CallbackSafety {

        @Test
        void shouldNotPropagateExceptionFromOnSuccessCallback() throws IOException {
            mockReindexSubmitResponse("node:1");
            String done = "{\"completed\":true,\"response\":{\"total\":1,\"created\":1,"
                + "\"updated\":0,\"batches\":1,\"failures\":[]}}";

            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenReturn(mockTaskResponse(done));

            org.mockito.Mockito.doThrow(new RuntimeException("callback blew up")).when(listener).onSuccess();

            reindexHelper.reindexIndex("src", "dest", 10L, listener);
        }

        @Test
        void shouldNotPropagateExceptionFromOnFailureCallback() throws IOException {
            mockReindexSubmitResponse("node:1");
            String taskError = "{\"completed\":true,\"error\":{\"type\":\"exception\",\"reason\":\"bad\"}}";

            when(restClient.performRequest(any(Request.class)))
                .thenReturn(httpResponse)
                .thenReturn(mockTaskResponse(taskError));

            org.mockito.Mockito.doThrow(new RuntimeException("callback blew up")).when(listener).onFailure(any());

            reindexHelper.reindexIndex("src", "dest", 10L, listener);
        }
    }

    @Nested
    class AsyncExecutor {

        @Test
        void shouldUseProvidedExecutor() throws IOException {
            Executor mockExec = mock(Executor.class);
            doNothing().when(mockExec).execute(any(Runnable.class));

            reindexHelper = new ReindexHelper(restClient, config, mockExec);
            mockReindexSubmitResponse("node:1");
            when(restClient.performRequest(any(Request.class))).thenReturn(httpResponse);

            reindexHelper.reindexIndex("src", "dest", 10L, listener);

            verify(mockExec).execute(any(Runnable.class));
        }

        @Test
        void shouldCreateDefaultAsyncExecutor() {
            Executor executor = reindexHelper.asyncExecutor();

            assertThat(executor)
                .isNotNull()
                .isInstanceOf(org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor.class);
        }
    }
}
