package uk.gov.hmcts.ccd.definition.store.elastic.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.AliasDefinition;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.GetAliasRequest;
import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import co.elastic.clients.elasticsearch.indices.PutIndicesSettingsRequest;
import co.elastic.clients.elasticsearch.indices.PutIndicesSettingsResponse;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import co.elastic.clients.elasticsearch.indices.RefreshResponse;
import co.elastic.clients.elasticsearch.indices.UpdateAliasesRequest;
import co.elastic.clients.elasticsearch.indices.UpdateAliasesResponse;
import co.elastic.clients.elasticsearch.indices.get_alias.IndexAliases;
import co.elastic.clients.transport.rest5_client.low_level.Response;
import co.elastic.clients.util.ObjectBuilder;
import org.apache.http.HttpEntity;
import org.assertj.core.api.Assertions;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.ElasticGlobalSearchListener.GLOBAL_SEARCH;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HighLevelCCDElasticClientTest {

    @Mock
    private ElasticsearchClient elasticClient;

    @Mock
    private RestClient restClient;

    @Mock
    private ElasticsearchClientFactory elasticClientFactory;

    @Mock
    private CcdElasticSearchProperties config;

    private HighLevelCCDElasticClient highLevelCCDElasticClient;

    @Mock
    private ElasticsearchIndicesClient indicesClient;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        doReturn(indicesClient).when(elasticClient).indices();
        doReturn(elasticClient).when(elasticClientFactory).createClient();
        Executor direct = Runnable::run;
        highLevelCCDElasticClient = spy(new HighLevelCCDElasticClient(config, elasticClientFactory, direct));
    }

    @Test
    void createIndexCreatesIndexWithAliasSuccessfully() throws IOException {
        final String indexName = "test_index";
        final String alias = "test_alias";

        CreateIndexResponse response = new CreateIndexResponse.Builder()
            .index(indexName)
            .acknowledged(true)
            .shardsAcknowledged(true)
            .build();
        doReturn(response)
            .when(indicesClient)
            .create(Mockito.<Function<CreateIndexRequest.Builder, ObjectBuilder<CreateIndexRequest>>>any());

        GetAliasResponse realAliasResponse = new GetAliasResponse.Builder()
            .aliases(Map.of(alias, createIndexAliases(indexName)))
            .build();
        realGetAliasResponseStub(realAliasResponse);

        assertThat(highLevelCCDElasticClient.createIndex(indexName, alias)).isTrue();
    }

    @Test
    void aliasExistsReturnsTrueWhenAliasExists() throws IOException {
        final String alias = "existing_alias";
        final String index = "some_index";

        GetAliasResponse realAliasResponse = new GetAliasResponse.Builder()
            .aliases(Map.of(alias, createIndexAliases(index)))
            .build();
        realGetAliasResponseStub(realAliasResponse);

        boolean exists = highLevelCCDElasticClient.aliasExists(alias);

        assertThat(exists).isTrue();
        verify(indicesClient).getAlias(Mockito.<java.util.function.Function<GetAliasRequest.Builder,
            co.elastic.clients.util.ObjectBuilder<GetAliasRequest>>>any());
    }

    @Test
    void aliasExistsReturnsFalseWhenAliasDoesNotExist() throws IOException {
        final String alias = "non_existing_alias";

        GetAliasResponse realAliasResponse = new GetAliasResponse.Builder()
            .aliases(Collections.emptyMap())
            .build();
        realGetAliasResponseStub(realAliasResponse);

        assertThat(highLevelCCDElasticClient.aliasExists(alias)).isFalse();
        verify(indicesClient)
            .getAlias(Mockito.<java.util.function.Function<GetAliasRequest.Builder,
                co.elastic.clients.util.ObjectBuilder<GetAliasRequest>>>any());
    }

    @Test
    void updateAliasUpdatesAliasSuccessfully() throws IOException {
        final String alias = "test_alias";
        final String oldIndex = "old_index";
        final String newIndex = "new_index";

        UpdateAliasesResponse realAliasesResponse = new UpdateAliasesResponse.Builder()
            .acknowledged(true)
            .build();
        realUpdateAliasesResponseStub(realAliasesResponse);

        assertThat(highLevelCCDElasticClient.updateAlias(alias, oldIndex, newIndex)).isTrue();
        verify(indicesClient)
            .updateAliases(Mockito.<java.util.function.Function<UpdateAliasesRequest.Builder,
                co.elastic.clients.util.ObjectBuilder<UpdateAliasesRequest>>>any());
    }

    @Test
    void updateAliasFailsWhenAliasUpdateNotAcknowledged() throws IOException {
        final String alias = "test_alias";
        final String oldIndex = "old_index";
        final String newIndex = "new_index";

        UpdateAliasesResponse realAliasesResponse = new UpdateAliasesResponse.Builder()
            .acknowledged(false)
            .build();
        realUpdateAliasesResponseStub(realAliasesResponse);

        assertThat(highLevelCCDElasticClient.updateAlias(alias, oldIndex, newIndex)).isFalse();
        verify(indicesClient).updateAliases(Mockito.<java.util.function.Function<UpdateAliasesRequest.Builder,
            co.elastic.clients.util.ObjectBuilder<UpdateAliasesRequest>>>any());
    }

    @Test
    void aliasExistsReturnsFalseOn404ElasticsearchException() throws IOException {
        final String alias = "missing_alias";

        assertThat(highLevelCCDElasticClient.aliasExists(alias)).isFalse();
    }

    @Test
    void upsertMappingUpsertsMappingSuccessfully() throws IOException {
        String aliasName = "test_alias";
        String caseTypeMapping = "{\"properties\":{}}";
        String indexName = "test_index";

        GetAliasResponse aliasResponse = new GetAliasResponse.Builder()
            .aliases(Map.of(aliasName, createIndexAliases(indexName)))
            .build();

        doReturn(aliasResponse).when(indicesClient).getAlias(any(Function.class));

        var putMappingResponse = mock(PutMappingResponse.class);
        when(putMappingResponse.acknowledged()).thenReturn(true);
        doReturn(putMappingResponse).when(indicesClient).putMapping(any(Function.class));

        boolean result = highLevelCCDElasticClient.upsertMapping(aliasName, caseTypeMapping);

        assertThat(result).isTrue();
        verify(indicesClient).getAlias(any(Function.class));
        verify(indicesClient).putMapping(any(Function.class));
    }

    @Test
    void upsertMappingThrowsIOExceptionIfAliasLookupFails() throws IOException {
        String aliasName = "test_alias";
        String caseTypeMapping = "{\"properties\":{}}";

        doThrow(new IOException("Alias lookup failed"))
            .doThrow(new IOException("Alias lookup failed"))
            .doThrow(new IOException("Alias lookup failed"))
            .doThrow(new IOException("Alias lookup failed"))
            .when(indicesClient).getAlias(any(Function.class));

        assertThatThrownBy(() -> highLevelCCDElasticClient.upsertMapping(aliasName, caseTypeMapping))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Failed to execute get alias for upsert mapping after 3 attempts");
    }

    @Test
    void upsertMappingReturnsFalseIfPutMappingNotAcknowledged() throws IOException {
        String aliasName = "test_alias";
        String caseTypeMapping = "{\"properties\":{}}";
        String indexName = "test_index";

        GetAliasResponse aliasResponse = new GetAliasResponse.Builder()
            .aliases(Map.of(aliasName, createIndexAliases(indexName)))
            .build();
        doReturn(aliasResponse).when(indicesClient).getAlias(any(Function.class));

        var putMappingResponse = mock(PutMappingResponse.class);
        when(putMappingResponse.acknowledged()).thenReturn(false);
        doReturn(putMappingResponse).when(indicesClient).putMapping(any(Function.class));

        boolean result = highLevelCCDElasticClient.upsertMapping(aliasName, caseTypeMapping);

        assertThat(result).isFalse();
    }

    @Test
    void setIndexReadOnlySetsIndexReadOnlyFlag() throws IOException {
        String indexName = "readonly_index";
        boolean readOnly = true;

        var putSettingsResponse = mock(PutIndicesSettingsResponse.class);
        doReturn(putSettingsResponse).when(indicesClient).putSettings(any(Function.class));

        highLevelCCDElasticClient.setIndexReadOnly(indexName, readOnly);

        verify(indicesClient).putSettings(any(Function.class));
    }

    @Test
    void setIndexReadOnlyThrowsIOExceptionOnFailure() throws IOException {
        String indexName = "readonly_index";
        boolean readOnly = true;

        doThrow(new IOException("PutSettings failed"))
            .when(indicesClient).putSettings(any(Function.class));

        assertThatThrownBy(() -> highLevelCCDElasticClient.setIndexReadOnly(indexName, readOnly))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("PutSettings failed");
    }

    @Test
    void createIndexAndMappingCreatesIndexAndMappingSuccessfully() throws IOException {
        String indexName = "new_index";
        String caseTypeMapping = "{\"properties\":{}}";

        CreateIndexResponse createIndexResponse = new CreateIndexResponse.Builder()
            .index(indexName)
            .acknowledged(true)
            .shardsAcknowledged(true)
            .build();
        doReturn(createIndexResponse).when(indicesClient).create(any(Function.class));

        var putMappingResponse = mock(PutMappingResponse.class);
        when(putMappingResponse.acknowledged()).thenReturn(true);
        doReturn(putMappingResponse).when(indicesClient).putMapping(any(Function.class));

        boolean result = highLevelCCDElasticClient.createIndexAndMapping(indexName, caseTypeMapping);

        assertThat(result).isTrue();
        verify(indicesClient).create(any(Function.class));
        verify(indicesClient).putMapping(any(Function.class));
    }

    @Test
    void createIndexAndMappingThrowsIOExceptionIfCreateFails() throws IOException {
        String indexName = "failing_index";
        String caseTypeMapping = "{\"properties\":{}}";

        doThrow(new IOException("CreateIndex failed"))
            .doThrow(new IOException("CreateIndex failed"))
            .doThrow(new IOException("CreateIndex failed"))
            .doThrow(new IOException("CreateIndex failed"))
            .when(indicesClient).create(any(Function.class));

        assertThatThrownBy(() -> highLevelCCDElasticClient.createIndexAndMapping(indexName, caseTypeMapping))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Failed to execute create index with mapping after 3 attempts");
    }

    @Test
    void createIndexAndMappingThrowsIOExceptionIfPutMappingFails() throws IOException {
        String indexName = "new_index";
        String caseTypeMapping = "{\"properties\":{}}";

        CreateIndexResponse createIndexResponse = new CreateIndexResponse.Builder()
            .index(indexName)
            .acknowledged(true)
            .shardsAcknowledged(true)
            .build();
        doReturn(createIndexResponse).when(indicesClient).create(any(Function.class));

        doThrow(new IOException("PutMapping failed")).when(indicesClient).putMapping(any(Function.class));

        assertThatThrownBy(() -> highLevelCCDElasticClient.createIndexAndMapping(indexName, caseTypeMapping))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("PutMapping failed");
    }

    @Test
    void removeIndexDeletesIndexSuccessfully() throws IOException {
        String indexName = "index_to_remove";
        var deleteResponse = mock(DeleteIndexResponse.class);
        Mockito.when(deleteResponse.acknowledged()).thenReturn(true);
        doReturn(deleteResponse).when(indicesClient).delete(any(Function.class));

        boolean result = highLevelCCDElasticClient.removeIndex(indexName);

        assertThat(result).isTrue();
        verify(indicesClient).delete(any(Function.class));
    }

    @Test
    void removeIndexThrowsIOExceptionIfDeleteFails() throws IOException {
        String indexName = "index_to_remove";
        doThrow(new IOException("Delete failed")).when(indicesClient).delete(any(Function.class));

        assertThatThrownBy(() -> highLevelCCDElasticClient.removeIndex(indexName))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Delete failed");
    }

    @Test
    void removeIndexReturnsFalseIfDeleteNotAcknowledged() throws IOException {
        String indexName = "index_to_remove";
        var deleteResponse = mock(DeleteIndexResponse.class);
        when(deleteResponse.acknowledged()).thenReturn(false);
        doReturn(deleteResponse).when(indicesClient).delete(any(Function.class));

        boolean result = highLevelCCDElasticClient.removeIndex(indexName);

        assertThat(result).isFalse();
    }

    @Test
    void reindexDataCallsListenerOnResponse() throws Exception {
        String oldIndex = "old_index";
        String newIndex = "new_index";
        String taskId = "node:123";

        String startResponseBody = "{\"task\":\"" + taskId + "\"}";

        String taskResponseBody = """
            {
              "completed": true,
              "response": {
                "total": 10,
                "created": 10,
                "updated": 0,
                "batches": 1,
                "failures": []
              }
            }
            """;

        Response startResponse = mock(Response.class);
        Response taskResponse = mock(Response.class);
        HttpEntity startEntity = mock(HttpEntity.class);
        HttpEntity taskEntity = mock(HttpEntity.class);

        when(startResponse.getEntity()).thenReturn((org.apache.hc.core5.http.HttpEntity) startEntity);
        when(taskResponse.getEntity()).thenReturn((org.apache.hc.core5.http.HttpEntity) taskEntity);

        when(startEntity.getContent())
            .thenReturn(new ByteArrayInputStream(startResponseBody.getBytes(StandardCharsets.UTF_8)));
        when(taskEntity.getContent())
            .thenReturn(new ByteArrayInputStream(taskResponseBody.getBytes(StandardCharsets.UTF_8)));

        // Low-level client comes from the factory
        when(elasticClientFactory.createLowLevelClient()).thenReturn(restClient);

        // Route the two HTTP calls: one to /_reindex, one to /_tasks/{taskId}
        when(restClient.performRequest(any(Request.class)))
            .thenAnswer(invocation -> {
                Request req = invocation.getArgument(0);
                String method = req.getMethod();
                String endpoint = req.getEndpoint();

                if ("POST".equalsIgnoreCase(method) && endpoint.startsWith("/_reindex")) {
                    return startResponse;
                }
                if ("GET".equalsIgnoreCase(method) && endpoint.startsWith("/_tasks/")) {
                    return taskResponse;
                }
                throw new IllegalStateException("Unexpected request: " + method + " " + endpoint);
            });

        // Run async work synchronously to avoid latches / flakiness
        Executor directExecutor = Runnable::run;

        highLevelCCDElasticClient =
            new HighLevelCCDElasticClient(config, elasticClientFactory, directExecutor);

        ReindexListener listener = mock(ReindexListener.class);

        // When
        String returnedTaskId =
            highLevelCCDElasticClient.reindexData(oldIndex, newIndex, listener);

        // Then
        assertThat(returnedTaskId).isEqualTo(taskId);
        verify(listener).onSuccess();
        verify(listener, never()).onFailure(any());
    }

    @Test
    void reindexDataCallsListenerOnFailureWhenReindexThrows() throws Exception {
        String oldIndex = "old_index";
        String newIndex = "new_index";
        @SuppressWarnings("unchecked")
        ReindexListener listener = mock(ReindexListener.class);

        CountDownLatch latch = new CountDownLatch(1);

        doThrow(new RuntimeException("Reindex failed")).when(elasticClient).reindex(any(Function.class));

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(listener).onFailure(any(RuntimeException.class));

        highLevelCCDElasticClient.reindexData(oldIndex, newIndex, listener);

        latch.await(); // Wait for async
        verify(listener).onFailure(Mockito.any(RuntimeException.class));
    }

    @Test
    void aliasExistsRethrowsExceptionIfStatusNot404() throws IOException, ElasticsearchException {
        final String alias = "some_alias";
        ElasticsearchException esEx = mock(ElasticsearchException.class);
        Mockito.when(esEx.status()).thenReturn(404);
        doThrow(esEx)
            .when(indicesClient).getAlias(any(Function.class));
        assertThat(highLevelCCDElasticClient.aliasExists(alias)).isFalse();
    }

    @Test
    void shouldCreateIndexSuccessfully() throws IOException {
        // Given
        String indexName = "test-index";
        String alias = "test-alias";
        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        CreateIndexResponse createIndexResponse = mock(CreateIndexResponse.class);
        when(indicesClient.create(ArgumentMatchers.<CreateIndexRequest>any())).thenReturn(createIndexResponse);
        when(createIndexResponse.acknowledged()).thenReturn(true);

        // When
        boolean result = highLevelCCDElasticClient.createIndex(indexName, alias);

        // Then
        assertThat(result).isTrue();
        verify(indicesClient).create(ArgumentMatchers.<CreateIndexRequest>any());
    }

    @Test
    void shouldCreateIndexWithGlobalSearchSettings() throws IOException {
        // Given
        String indexName = "test-index";
        String alias = GLOBAL_SEARCH;
        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        CreateIndexResponse createIndexResponse = mock(CreateIndexResponse.class);
        when(indicesClient.create(ArgumentMatchers.<CreateIndexRequest>any())).thenReturn(createIndexResponse);
        when(createIndexResponse.acknowledged()).thenReturn(true);

        // When
        boolean result = highLevelCCDElasticClient.createIndex(indexName, alias);

        // Then
        assertThat(result).isTrue();
        verify(indicesClient).create(ArgumentMatchers.<CreateIndexRequest>any());
    }

    @Test
    void shouldCreateIndexWithCaseInsensitiveGlobalSearch() throws IOException {
        // Given
        String indexName = "test-index";
        String alias = GLOBAL_SEARCH.toUpperCase();
        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        CreateIndexResponse createIndexResponse = mock(CreateIndexResponse.class);
        when(indicesClient.create(ArgumentMatchers.<CreateIndexRequest>any())).thenReturn(createIndexResponse);
        when(createIndexResponse.acknowledged()).thenReturn(true);

        // When
        boolean result = highLevelCCDElasticClient.createIndex(indexName, alias);

        // Then
        assertThat(result).isTrue();
        verify(indicesClient).create(ArgumentMatchers.<CreateIndexRequest>any());
    }

    @Test
    void shouldThrowExceptionWhenCreateIndexFailsWithIOException() throws IOException {
        // Given
        String indexName = "test-index";
        String alias = "test-alias";
        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        when(indicesClient.create(ArgumentMatchers.<CreateIndexRequest>any()))
            .thenThrow(new IOException("Create index failed"));

        // When / Then
        Assertions.assertThatThrownBy(() -> highLevelCCDElasticClient.createIndex(indexName, alias))
            .isInstanceOf(IOException.class)
            .hasMessage("Create index failed");
    }

    @Test
    void shouldReturnFalseWhenUpsertMappingFails() throws IOException {
        //Given
        GetAliasResponse getAliasResponse = mock(GetAliasResponse.class);
        Map<String, IndexAliases> aliasMap = new HashMap<>();
        aliasMap.put("test-index-000001", mock(IndexAliases.class));
        when(indicesClient.getAlias((GetAliasRequest) any())).thenReturn(getAliasResponse);
        when(getAliasResponse.aliases()).thenReturn(aliasMap);
        PutMappingResponse putMappingResponse = mock(PutMappingResponse.class);
        when(indicesClient.putMapping((PutMappingRequest) any())).thenReturn(putMappingResponse);
        when(putMappingResponse.acknowledged()).thenReturn(false);

        // When
        String aliasName = "test-alias";
        String caseTypeMapping = "{\"properties\":{\"field1\":{\"type\":\"text\"}}}";
        boolean result = highLevelCCDElasticClient.upsertMapping(aliasName, caseTypeMapping);

        // Then
        assertThat(result).isFalse();
        verify(indicesClient).getAlias((GetAliasRequest) any());
        verify(indicesClient).putMapping((PutMappingRequest) any());
    }

    @Test
    void shouldThrowExceptionWhenUpsertMappingFailsWithIOException() throws IOException {
        // Given
        GetAliasResponse getAliasResponse = mock(GetAliasResponse.class);
        Map<String, IndexAliases> aliasMap = new HashMap<>();
        aliasMap.put("test-index-000001", mock(IndexAliases.class));
        when(indicesClient.getAlias((GetAliasRequest) any())).thenReturn(getAliasResponse);
        when(getAliasResponse.aliases()).thenReturn(aliasMap);
        when(indicesClient.putMapping((PutMappingRequest) any())).thenThrow(new IOException("Upsert mapping failed"));

        // When / Then
        final String aliasName = "test-alias";
        final String caseTypeMapping = "{\"properties\":{\"field1\":{\"type\":\"text\"}}}";
        Assertions.assertThatThrownBy(() -> highLevelCCDElasticClient.upsertMapping(aliasName, caseTypeMapping))
            .isInstanceOf(IOException.class)
            .hasMessage("Upsert mapping failed");
    }

    @Test
    void shouldCheckAliasExistsSuccessfully() throws IOException {
        // Given
        String alias = "test-alias";

        GetAliasResponse getAliasResponse = mock(GetAliasResponse.class);
        Map<String, IndexAliases> aliasMap = new HashMap<>();
        aliasMap.put("test-index-000001", mock(IndexAliases.class));
        when(indicesClient.getAlias((GetAliasRequest) any())).thenReturn(getAliasResponse);
        when(getAliasResponse.aliases()).thenReturn(aliasMap);

        // When
        boolean result = highLevelCCDElasticClient.aliasExists(alias);

        // Then
        assertThat(result).isTrue();
        verify(indicesClient).getAlias((GetAliasRequest) any());
    }

    @Test
    void shouldReturnFalseWhenAliasDoesNotExist() throws IOException {
        // Given
        String alias = "test-alias";
        GetAliasResponse getAliasResponse = mock(GetAliasResponse.class);
        when(indicesClient.getAlias((GetAliasRequest) any())).thenReturn(getAliasResponse);
        when(getAliasResponse.aliases()).thenReturn(Collections.emptyMap());

        // When
        boolean result = highLevelCCDElasticClient.aliasExists(alias);

        // Then
        assertThat(result).isFalse();
        verify(indicesClient).getAlias((GetAliasRequest) any());
    }

    @Test
    void shouldThrowExceptionWhenAliasExistsFailsWithIOException() throws IOException {
        // Given
        String alias = "test-alias";
        GetAliasResponse getAliasResponse = mock(GetAliasResponse.class);
        when(indicesClient.getAlias((GetAliasRequest) any())).thenReturn(getAliasResponse);
        when(getAliasResponse.aliases()).thenThrow(new IOException("Alias exists check failed"));

        // When / Then
        Assertions.assertThatThrownBy(() -> highLevelCCDElasticClient.aliasExists(alias))
            .isInstanceOf(IOException.class)
            .hasMessage("Alias exists check failed");
    }

    @Test
    void shouldCloseClientSuccessfully() throws IOException {
        // When
        highLevelCCDElasticClient.close();

        // Then
        verify(elasticClient).close();
    }

    @Test
    void shouldHandleIOExceptionWhenClosingClient() throws IOException {
        // Given
        doThrow(new IOException("Close failed")).when(elasticClient).close();

        // When
        highLevelCCDElasticClient.close();

        // Then
        verify(elasticClient).close();
        // Verify that the error is logged, but no exception is rethrown
    }

    @Test
    void shouldGetAliasSuccessfully() throws IOException {
        // Given
        String alias = "test-alias";
        GetAliasResponse getAliasResponse = mock(GetAliasResponse.class);
        when(indicesClient.getAlias((GetAliasRequest) any())).thenReturn(getAliasResponse);

        // When
        GetAliasResponse result = highLevelCCDElasticClient.getAlias(alias);

        // Then
        assertThat(result).isEqualTo(getAliasResponse);
        verify(indicesClient).getAlias((GetAliasRequest) any());
    }

    @Test
    void shouldThrowExceptionWhenGetAliasFailsWithIOException() throws IOException {
        // Given
        String alias = "test-alias";
        when(indicesClient.getAlias((GetAliasRequest) any())).thenThrow(new IOException("Get alias failed"));

        // When / Then
        Assertions.assertThatThrownBy(() -> highLevelCCDElasticClient.getAlias(alias))
            .isInstanceOf(IOException.class)
            .hasMessage("Get alias failed");
    }

    @Test
    void shouldSetIndexReadOnlySuccessfully() throws IOException {
        // Given
        String indexName = "test-index";
        boolean readOnly = true;
        PutIndicesSettingsResponse putSettingsResponse = mock(PutIndicesSettingsResponse.class);
        when(indicesClient.putSettings((PutIndicesSettingsRequest) any())).thenReturn(putSettingsResponse);

        // When
        highLevelCCDElasticClient.setIndexReadOnly(indexName, readOnly);

        // Then
        verify(indicesClient).putSettings((PutIndicesSettingsRequest) any());
    }

    @Test
    void shouldSetIndexReadOnlyFalse() throws IOException {
        // Given
        String indexName = "test-index";
        boolean readOnly = false;
        PutIndicesSettingsResponse putSettingsResponse = mock(PutIndicesSettingsResponse.class);
        when(indicesClient.putSettings((PutIndicesSettingsRequest) any())).thenReturn(putSettingsResponse);

        // When
        highLevelCCDElasticClient.setIndexReadOnly(indexName, readOnly);

        // Then
        verify(indicesClient).putSettings((PutIndicesSettingsRequest) any());
    }

    @Test
    void shouldThrowExceptionWhenSetIndexReadOnlyFailsWithIOException() throws IOException {
        // Given
        String indexName = "test-index";
        boolean readOnly = true;
        when(indicesClient.putSettings((PutIndicesSettingsRequest) any()))
            .thenThrow(new IOException("Set index read-only failed"));

        // When / Then
        Assertions.assertThatThrownBy(() -> highLevelCCDElasticClient.setIndexReadOnly(indexName, readOnly))
            .isInstanceOf(IOException.class)
            .hasMessage("Set index read-only failed");
    }

    @Test
    void shouldCreateIndexAndMappingSuccessfully() throws IOException {
        // Given
        String indexName = "test-index";
        String caseTypeMapping = "{\"properties\":{\"field1\":{\"type\":\"text\"}}}";

        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);

        CreateIndexResponse createIndexResponse = mock(CreateIndexResponse.class);
        when(indicesClient.create((CreateIndexRequest) any())).thenReturn(createIndexResponse);
        when(createIndexResponse.acknowledged()).thenReturn(true);

        PutMappingResponse putMappingResponse = mock(PutMappingResponse.class);
        when(indicesClient.putMapping((PutMappingRequest) any())).thenReturn(putMappingResponse);
        when(putMappingResponse.acknowledged()).thenReturn(true);

        // When
        boolean result = highLevelCCDElasticClient.createIndexAndMapping(indexName, caseTypeMapping);

        // Then
        assertThat(result).isTrue();
        verify(indicesClient).create((CreateIndexRequest) any());
        verify(indicesClient).putMapping((PutMappingRequest) any());
    }

    @Test
    void shouldReturnFalseWhenCreateIndexFailsInCreateIndexAndMapping() throws IOException {
        // Given
        String indexName = "test-index";
        String caseTypeMapping = "{\"properties\":{\"field1\":{\"type\":\"text\"}}}";

        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        CreateIndexResponse createIndexResponse = mock(CreateIndexResponse.class);
        when(indicesClient.create((CreateIndexRequest) any())).thenReturn(createIndexResponse);
        when(createIndexResponse.acknowledged()).thenReturn(false);

        PutMappingResponse putMappingResponse = mock(PutMappingResponse.class);
        when(indicesClient.putMapping((PutMappingRequest) any())).thenReturn(putMappingResponse);
        when(putMappingResponse.acknowledged()).thenReturn(true);

        // When
        boolean result = highLevelCCDElasticClient.createIndexAndMapping(indexName, caseTypeMapping);

        // Then
        assertThat(result).isFalse();
        verify(indicesClient).create((CreateIndexRequest) any());
        verify(indicesClient).putMapping((PutMappingRequest) any());
    }

    @Test
    void shouldThrowExceptionWhenCreateIndexAndMappingFailsWithIOException() throws IOException {
        // Given
        String indexName = "test-index";
        String caseTypeMapping = "{\"properties\":{\"field1\":{\"type\":\"text\"}}}";

        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        when(indicesClient.create((CreateIndexRequest) any()))
            .thenThrow(new IOException("Create index and mapping failed"));

        // When / Then
        Assertions.assertThatThrownBy(() -> highLevelCCDElasticClient.createIndexAndMapping(indexName, caseTypeMapping))
            .isInstanceOf(IOException.class)
            .hasMessage("Create index and mapping failed");
    }

    @Test
    void shouldUpdateAliasSuccessfully() throws IOException {
        // Given
        String aliasName = "test-alias";
        String oldIndex = "old-index";
        String newIndex = "new-index";

        UpdateAliasesResponse updateAliasesResponse = mock(UpdateAliasesResponse.class);
        when(indicesClient.updateAliases((UpdateAliasesRequest) any())).thenReturn(updateAliasesResponse);
        when(updateAliasesResponse.acknowledged()).thenReturn(true);

        // When
        boolean result = highLevelCCDElasticClient.updateAlias(aliasName, oldIndex, newIndex);

        // Then
        assertThat(result).isTrue();
        verify(indicesClient).updateAliases((UpdateAliasesRequest) any());
    }

    @Test
    void shouldReturnFalseWhenUpdateAliasFails() throws IOException {
        // Given
        String aliasName = "test-alias";
        String oldIndex = "old-index";
        String newIndex = "new-index";
        UpdateAliasesResponse updateAliasesResponse = mock(UpdateAliasesResponse.class);
        when(indicesClient.updateAliases((UpdateAliasesRequest) any())).thenReturn(updateAliasesResponse);
        when(updateAliasesResponse.acknowledged()).thenReturn(false);

        // When
        boolean result = highLevelCCDElasticClient.updateAlias(aliasName, oldIndex, newIndex);

        // Then
        assertThat(result).isFalse();
        verify(indicesClient).updateAliases((UpdateAliasesRequest) any());
    }

    @Test
    void shouldThrowExceptionWhenUpdateAliasFailsWithIOException() throws IOException {
        // Given
        String aliasName = "test-alias";
        String oldIndex = "old-index";
        String newIndex = "new-index";
        when(indicesClient.updateAliases((UpdateAliasesRequest) any()))
            .thenThrow(new IOException("Update alias failed"));

        // When / Then
        Assertions.assertThatThrownBy(() -> highLevelCCDElasticClient.updateAlias(aliasName, oldIndex, newIndex))
            .isInstanceOf(IOException.class)
            .hasMessage("Update alias failed");
    }

    @Test
    void shouldRemoveIndexSuccessfully() throws IOException {
        // Given
        String indexName = "test-index";
        DeleteIndexResponse deleteIndexResponse = mock(DeleteIndexResponse.class);
        when(indicesClient.delete((co.elastic.clients.elasticsearch.indices.DeleteIndexRequest) any()))
            .thenReturn(deleteIndexResponse);
        when(deleteIndexResponse.acknowledged()).thenReturn(true);

        // When
        boolean result = highLevelCCDElasticClient.removeIndex(indexName);

        // Then
        assertThat(result).isTrue();
        verify(indicesClient).delete((co.elastic.clients.elasticsearch.indices.DeleteIndexRequest) any());
    }

    @Test
    void shouldReturnFalseWhenRemoveIndexFails() throws IOException {
        // Given
        String indexName = "test-index";
        DeleteIndexResponse deleteIndexResponse = mock(DeleteIndexResponse.class);
        when(indicesClient.delete((co.elastic.clients.elasticsearch.indices.DeleteIndexRequest) any()))
            .thenReturn(deleteIndexResponse);
        when(deleteIndexResponse.acknowledged()).thenReturn(false);

        // When
        boolean result = highLevelCCDElasticClient.removeIndex(indexName);

        // Then
        assertThat(result).isFalse();
        verify(indicesClient).delete((co.elastic.clients.elasticsearch.indices.DeleteIndexRequest) any());
    }

    @Test
    void shouldThrowExceptionWhenRemoveIndexFailsWithIOException() throws IOException {
        // Given
        String indexName = "test-index";
        when(indicesClient.delete((co.elastic.clients.elasticsearch.indices.DeleteIndexRequest) any()))
            .thenThrow(new IOException("Remove index failed"));

        // When / Then
        Assertions.assertThatThrownBy(() -> highLevelCCDElasticClient.removeIndex(indexName))
            .isInstanceOf(IOException.class)
            .hasMessage("Remove index failed");
    }

    @Test
    void shouldReindexDataSuccessfully() throws IOException {
        // Given
        String oldIndex = "old-index";
        String newIndex = "new-index";
        String taskId = "node:123";
        String startResponseBody = "{\"task\":\"" + taskId + "\"}";

        String taskResponseBody = """
            {
              "completed": true,
              "response": {
                "total": 10,
                "created": 10,
                "updated": 0,
                "batches": 1,
                "failures": []
              }
            }
            """;

        Response startResponse = mock(Response.class);
        Response taskResponse = mock(Response.class);
        HttpEntity startEntity = mock(org.apache.http.HttpEntity.class);
        HttpEntity taskEntity = mock(org.apache.http.HttpEntity.class);

        when(startResponse.getEntity()).thenReturn((org.apache.hc.core5.http.HttpEntity) startEntity);
        when(taskResponse.getEntity()).thenReturn((org.apache.hc.core5.http.HttpEntity) taskEntity);

        when(startEntity.getContent())
            .thenReturn(new java.io.ByteArrayInputStream(startResponseBody.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        when(taskEntity.getContent())
            .thenReturn(new java.io.ByteArrayInputStream(taskResponseBody.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        when(elasticClientFactory.createLowLevelClient()).thenReturn(restClient);

        when(restClient.performRequest(any(org.elasticsearch.client.Request.class)))
            .thenAnswer(invocation -> {
                org.elasticsearch.client.Request req = invocation.getArgument(0);
                String method = req.getMethod();
                String endpoint = req.getEndpoint();

                if ("POST".equalsIgnoreCase(method) && endpoint.startsWith("/_reindex")) {
                    return startResponse;
                } else if ("GET".equalsIgnoreCase(method) && endpoint.startsWith("/_tasks/")) {
                    return taskResponse;
                } else {
                    return taskResponse;
                }
            });

        Executor directExecutor = Runnable::run;
        highLevelCCDElasticClient = new HighLevelCCDElasticClient(config, elasticClientFactory, directExecutor);
        ReindexListener listener = mock(ReindexListener.class);

        // When
        String result = highLevelCCDElasticClient.reindexData(oldIndex, newIndex, listener);

        // Then
        assertThat(result).isNotNull().isEqualTo(taskId);
    }

    @Test
    void shouldReindexDataThrowExceptionWhenReindexHelperFails() throws IOException {
        // Given
        String oldIndex = "old-index";
        String newIndex = "new-index";

        when(elasticClientFactory.createLowLevelClient()).thenReturn(restClient);
        when(restClient.performRequest(any(Request.class))).thenThrow(new IOException("Reindex request failed"));
        Executor directExecutor = Runnable::run;
        highLevelCCDElasticClient =
            new HighLevelCCDElasticClient(config, elasticClientFactory, directExecutor);
        ReindexListener listener = mock(ReindexListener.class);

        // When / Then
        Assertions.assertThatThrownBy(() -> highLevelCCDElasticClient.reindexData(oldIndex, newIndex, listener))
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void shouldRefreshIndexesSuccessfully() throws IOException {
        // Given
        String[] indexes = {"index1", "index2"};
        RefreshResponse refreshResponse = mock(RefreshResponse.class);
        when(indicesClient.refresh((co.elastic.clients.elasticsearch.indices.RefreshRequest) any()))
            .thenReturn(refreshResponse);

        // When
        highLevelCCDElasticClient.refresh(indexes);

        // Then
        verify(indicesClient).refresh((co.elastic.clients.elasticsearch.indices.RefreshRequest) any());
    }

    @Test
    void shouldHandleEmptyIndexesInRefresh() throws IOException {
        // Given
        String[] indexes = {};
        RefreshResponse refreshResponse = mock(RefreshResponse.class);
        when(indicesClient.refresh((co.elastic.clients.elasticsearch.indices.RefreshRequest) any()))
            .thenReturn(refreshResponse);

        // When
        highLevelCCDElasticClient.refresh(indexes);

        // Then
        verify(indicesClient).refresh((co.elastic.clients.elasticsearch.indices.RefreshRequest) any());
    }

    @Test
    void shouldHandleNullIndexesInRefresh() throws IOException {
        // Given
        String[] indexes = null;
        RefreshResponse refreshResponse = mock(RefreshResponse.class);
        when(indicesClient.refresh((co.elastic.clients.elasticsearch.indices.RefreshRequest) any()))
            .thenReturn(refreshResponse);

        // When
        highLevelCCDElasticClient.refresh(indexes);

        // Then
        verify(indicesClient).refresh((co.elastic.clients.elasticsearch.indices.RefreshRequest) any());
    }

    @Test
    void shouldThrowExceptionWhenRefreshFailsWithIOException() throws IOException {
        // Given
        String[] indexes = {"index1"};
        when(indicesClient.refresh((co.elastic.clients.elasticsearch.indices.RefreshRequest) any()))
            .thenThrow(new IOException("Refresh failed"));

        // When / Then
        Assertions.assertThatThrownBy(() -> highLevelCCDElasticClient.refresh(indexes))
            .isInstanceOf(IOException.class)
            .hasMessage("Refresh failed");
    }

    private void realGetAliasResponseStub(GetAliasResponse realAliasResponse) throws IOException {
        doReturn(realAliasResponse)
            .when(indicesClient)
            .getAlias(Mockito.<Function<GetAliasRequest.Builder, ObjectBuilder<GetAliasRequest>>>any());

    }

    private void realUpdateAliasesResponseStub(UpdateAliasesResponse realAliasesResponse) throws IOException {
        doReturn(realAliasesResponse)
            .when(indicesClient)
            .updateAliases(Mockito.<Function<UpdateAliasesRequest.Builder, ObjectBuilder<UpdateAliasesRequest>>>any());
    }

    private IndexAliases createIndexAliases(String index) {
        IndexAliases.Builder builder = new IndexAliases.Builder();
        builder.aliases(index, new AliasDefinition.Builder().build());
        return builder.build();
    }
}