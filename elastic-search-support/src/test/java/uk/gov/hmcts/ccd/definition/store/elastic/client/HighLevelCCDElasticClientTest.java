package uk.gov.hmcts.ccd.definition.store.elastic.client;

import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.ElasticGlobalSearchListener.GLOBAL_SEARCH;

@ExtendWith(MockitoExtension.class)
class HighLevelCCDElasticClientTest {

    @Mock
    private CcdElasticSearchProperties config;

    @Mock
    private RestHighLevelClient elasticClient;

    @Mock
    private org.elasticsearch.client.IndicesClient indicesClientMock;

    @Mock
    private org.elasticsearch.client.RestClient restClient;

    @Mock
    private org.elasticsearch.client.Response httpResponse;

    @Mock
    private org.apache.http.HttpEntity httpEntity;

    @Mock
    private CreateIndexResponse createIndexResponse;

    @Mock
    private AcknowledgedResponse acknowledgedResponse;

    @Mock
    private GetAliasesResponse getAliasesResponse;

    @Mock
    private RefreshResponse refreshResponse;

    @Mock
    private ReindexListener reindexListener;

    private HighLevelCCDElasticClient client;

    @BeforeEach
    void setUp() {
        client = new HighLevelCCDElasticClient(config, elasticClient);

        // Mock elasticClient.indices() to return the mocked IndicesClient
        lenient().when(elasticClient.indices()).thenReturn(indicesClientMock);
    }

    @Test
    void shouldCreateIndexSuccessfully() throws IOException {
        // Given
        String indexName = "test-index";
        String alias = "test-alias";
        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        when(indicesClientMock.create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(createIndexResponse);
        when(createIndexResponse.isAcknowledged()).thenReturn(true);

        // When
        boolean result = client.createIndex(indexName, alias);

        // Then
        assertThat(result).isTrue();
        verify(indicesClientMock).create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldCreateIndexWithGlobalSearchSettings() throws IOException {
        // Given
        String indexName = "test-index";
        String alias = GLOBAL_SEARCH;
        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        when(indicesClientMock.create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(createIndexResponse);
        when(createIndexResponse.isAcknowledged()).thenReturn(true);

        // When
        boolean result = client.createIndex(indexName, alias);

        // Then
        assertThat(result).isTrue();
        verify(indicesClientMock).create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldCreateIndexWithCaseInsensitiveGlobalSearch() throws IOException {
        // Given
        String indexName = "test-index";
        String alias = GLOBAL_SEARCH.toUpperCase();
        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        when(indicesClientMock.create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(createIndexResponse);
        when(createIndexResponse.isAcknowledged()).thenReturn(true);

        // When
        boolean result = client.createIndex(indexName, alias);

        // Then
        assertThat(result).isTrue();
        verify(indicesClientMock).create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldReturnFalseWhenCreateIndexFails() throws IOException {
        // Given
        String indexName = "test-index";
        String alias = "test-alias";
        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        when(indicesClientMock.create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(createIndexResponse);
        when(createIndexResponse.isAcknowledged()).thenReturn(false);

        // When
        boolean result = client.createIndex(indexName, alias);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenCreateIndexFailsWithIOException() throws IOException {
        // Given
        String indexName = "test-index";
        String alias = "test-alias";
        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        when(indicesClientMock.create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT)))
            .thenThrow(new IOException("Create index failed"));

        // When / Then
        assertThatThrownBy(() -> client.createIndex(indexName, alias))
            .isInstanceOf(IOException.class)
            .hasMessage("Create index failed");
    }

    @Test
    void shouldUpsertMappingSuccessfully() throws IOException {
        // Given
        Map<String, Set<org.elasticsearch.cluster.metadata.AliasMetadata>> aliases = new HashMap<>();
        Set<org.elasticsearch.cluster.metadata.AliasMetadata> aliasSet = new HashSet<>();
        aliasSet.add(mock(org.elasticsearch.cluster.metadata.AliasMetadata.class));
        aliases.put("test-index-000001", aliasSet);
        when(indicesClientMock.getAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(getAliasesResponse);
        when(getAliasesResponse.getAliases()).thenReturn(aliases);
        when(indicesClientMock.putMapping(any(PutMappingRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(acknowledgedResponse);
        when(acknowledgedResponse.isAcknowledged()).thenReturn(true);

        // When
        final String aliasName = "test-alias";
        final String caseTypeMapping = "{\"properties\":{\"field1\":{\"type\":\"text\"}}}";
        boolean result = client.upsertMapping(aliasName, caseTypeMapping);

        // Then
        assertThat(result).isTrue();
        verify(indicesClientMock).getAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT));
        verify(indicesClientMock).putMapping(any(PutMappingRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldReturnFalseWhenUpsertMappingFails() throws IOException {
        // Given
        Map<String, Set<org.elasticsearch.cluster.metadata.AliasMetadata>> aliases = new HashMap<>();
        Set<org.elasticsearch.cluster.metadata.AliasMetadata> aliasSet = new HashSet<>();
        aliasSet.add(mock(org.elasticsearch.cluster.metadata.AliasMetadata.class));
        aliases.put("test-index-000001", aliasSet);
        when(indicesClientMock.getAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(getAliasesResponse);
        when(getAliasesResponse.getAliases()).thenReturn(aliases);
        when(indicesClientMock.putMapping(any(PutMappingRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(acknowledgedResponse);
        when(acknowledgedResponse.isAcknowledged()).thenReturn(false);

        // When
        final String aliasName = "test-alias";
        final String caseTypeMapping = "{\"properties\":{\"field1\":{\"type\":\"text\"}}}";
        boolean result = client.upsertMapping(aliasName, caseTypeMapping);

        // Then
        assertThat(result).isFalse();
        verify(indicesClientMock).getAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT));
        verify(indicesClientMock).putMapping(any(PutMappingRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldThrowExceptionWhenUpsertMappingFailsWithIOException() throws IOException {
        // Given
        Map<String, Set<org.elasticsearch.cluster.metadata.AliasMetadata>> aliases = new HashMap<>();
        Set<org.elasticsearch.cluster.metadata.AliasMetadata> aliasSet = new HashSet<>();
        aliasSet.add(mock(org.elasticsearch.cluster.metadata.AliasMetadata.class));
        aliases.put("test-index-000001", aliasSet);
        when(indicesClientMock.getAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(getAliasesResponse);
        when(getAliasesResponse.getAliases()).thenReturn(aliases);
        when(indicesClientMock.putMapping(any(PutMappingRequest.class), eq(RequestOptions.DEFAULT)))
            .thenThrow(new IOException("Upsert mapping failed"));

        // When / Then
        final String aliasName = "test-alias";
        final String caseTypeMapping = "{\"properties\":{\"field1\":{\"type\":\"text\"}}}";
        assertThatThrownBy(() -> client.upsertMapping(aliasName, caseTypeMapping))
            .isInstanceOf(IOException.class)
            .hasMessage("Upsert mapping failed");
    }

    @Test
    void shouldCheckAliasExistsSuccessfully() throws IOException {
        // Given
        String alias = "test-alias";
        when(indicesClientMock.existsAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(true);

        // When
        boolean result = client.aliasExists(alias);

        // Then
        assertThat(result).isTrue();
        verify(indicesClientMock).existsAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldReturnFalseWhenAliasDoesNotExist() throws IOException {
        // Given
        String alias = "test-alias";
        when(indicesClientMock.existsAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(false);

        // When
        boolean result = client.aliasExists(alias);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenAliasExistsFailsWithIOException() throws IOException {
        // Given
        String alias = "test-alias";
        when(indicesClientMock.existsAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT)))
            .thenThrow(new IOException("Alias exists check failed"));

        // When / Then
        assertThatThrownBy(() -> client.aliasExists(alias))
            .isInstanceOf(IOException.class)
            .hasMessage("Alias exists check failed");
    }

    @Test
    void shouldCloseClientSuccessfully() throws IOException {
        // When
        client.close();

        // Then
        verify(elasticClient).close();
    }

    @Test
    void shouldHandleIOExceptionWhenClosingClient() throws IOException {
        // Given
        doThrow(new IOException("Close failed")).when(elasticClient).close();

        // When
        client.close();

        // Then
        verify(elasticClient).close();
        // Verify that the error is logged, but no exception is rethrown
    }

    @Test
    void shouldGetAliasSuccessfully() throws IOException {
        // Given
        String alias = "test-alias";
        when(indicesClientMock.getAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(getAliasesResponse);

        // When
        GetAliasesResponse result = client.getAlias(alias);

        // Then
        assertThat(result).isEqualTo(getAliasesResponse);
        verify(indicesClientMock).getAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldThrowExceptionWhenGetAliasFailsWithIOException() throws IOException {
        // Given
        String alias = "test-alias";
        when(indicesClientMock.getAlias(any(GetAliasesRequest.class), eq(RequestOptions.DEFAULT)))
            .thenThrow(new IOException("Get alias failed"));

        // When / Then
        assertThatThrownBy(() -> client.getAlias(alias))
            .isInstanceOf(IOException.class)
            .hasMessage("Get alias failed");
    }

    @Test
    void shouldSetIndexReadOnlySuccessfully() throws IOException {
        // Given
        String indexName = "test-index";
        boolean readOnly = true;
        when(indicesClientMock.putSettings(any(UpdateSettingsRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(acknowledgedResponse);

        // When
        client.setIndexReadOnly(indexName, readOnly);

        // Then
        verify(indicesClientMock).putSettings(any(UpdateSettingsRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldSetIndexReadOnlyFalse() throws IOException {
        // Given
        String indexName = "test-index";
        boolean readOnly = false;
        when(indicesClientMock.putSettings(any(UpdateSettingsRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(acknowledgedResponse);

        // When
        client.setIndexReadOnly(indexName, readOnly);

        // Then
        verify(indicesClientMock).putSettings(any(UpdateSettingsRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldThrowExceptionWhenSetIndexReadOnlyFailsWithIOException() throws IOException {
        // Given
        String indexName = "test-index";
        boolean readOnly = true;
        when(indicesClientMock.putSettings(any(UpdateSettingsRequest.class), eq(RequestOptions.DEFAULT)))
            .thenThrow(new IOException("Set index read-only failed"));

        // When / Then
        assertThatThrownBy(() -> client.setIndexReadOnly(indexName, readOnly))
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
        when(indicesClientMock.create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(createIndexResponse);
        when(createIndexResponse.isAcknowledged()).thenReturn(true);
        when(indicesClientMock.putMapping(any(PutMappingRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(acknowledgedResponse);
        when(acknowledgedResponse.isAcknowledged()).thenReturn(true);

        // When
        boolean result = client.createIndexAndMapping(indexName, caseTypeMapping);

        // Then
        assertThat(result).isTrue();
        verify(indicesClientMock).create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT));
        verify(indicesClientMock).putMapping(any(PutMappingRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldReturnFalseWhenCreateIndexFailsInCreateIndexAndMapping() throws IOException {
        // Given
        String indexName = "test-index";
        String caseTypeMapping = "{\"properties\":{\"field1\":{\"type\":\"text\"}}}";

        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        when(indicesClientMock.create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(createIndexResponse);
        when(createIndexResponse.isAcknowledged()).thenReturn(false);
        when(indicesClientMock.putMapping(any(PutMappingRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(acknowledgedResponse);
        when(acknowledgedResponse.isAcknowledged()).thenReturn(true);

        // When
        boolean result = client.createIndexAndMapping(indexName, caseTypeMapping);

        // Then
        assertThat(result).isFalse();
        verify(indicesClientMock).create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT));
        verify(indicesClientMock).putMapping(any(PutMappingRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldThrowExceptionWhenCreateIndexAndMappingFailsWithIOException() throws IOException {
        // Given
        String indexName = "test-index";
        String caseTypeMapping = "{\"properties\":{\"field1\":{\"type\":\"text\"}}}";

        when(config.getIndexShards()).thenReturn(2);
        when(config.getIndexShardsReplicas()).thenReturn(1);
        when(config.getCasesIndexMappingFieldsLimit()).thenReturn(1000);
        when(indicesClientMock.create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT)))
            .thenThrow(new IOException("Create index and mapping failed"));

        // When / Then
        assertThatThrownBy(() -> client.createIndexAndMapping(indexName, caseTypeMapping))
            .isInstanceOf(IOException.class)
            .hasMessage("Create index and mapping failed");
    }

    @Test
    void shouldUpdateAliasSuccessfully() throws IOException {
        // Given
        String aliasName = "test-alias";
        String oldIndex = "old-index";
        String newIndex = "new-index";
        when(indicesClientMock.updateAliases(any(), eq(RequestOptions.DEFAULT)))
            .thenReturn(acknowledgedResponse);
        when(acknowledgedResponse.isAcknowledged()).thenReturn(true);

        // When
        boolean result = client.updateAlias(aliasName, oldIndex, newIndex);

        // Then
        assertThat(result).isTrue();
        verify(indicesClientMock).updateAliases(any(), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldReturnFalseWhenUpdateAliasFails() throws IOException {
        // Given
        String aliasName = "test-alias";
        String oldIndex = "old-index";
        String newIndex = "new-index";
        when(indicesClientMock.updateAliases(any(), eq(RequestOptions.DEFAULT)))
            .thenReturn(acknowledgedResponse);
        when(acknowledgedResponse.isAcknowledged()).thenReturn(false);

        // When
        boolean result = client.updateAlias(aliasName, oldIndex, newIndex);

        // Then
        assertThat(result).isFalse();
        verify(indicesClientMock).updateAliases(any(), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldThrowExceptionWhenUpdateAliasFailsWithIOException() throws IOException {
        // Given
        String aliasName = "test-alias";
        String oldIndex = "old-index";
        String newIndex = "new-index";
        when(indicesClientMock.updateAliases(any(), eq(RequestOptions.DEFAULT)))
            .thenThrow(new IOException("Update alias failed"));

        // When / Then
        assertThatThrownBy(() -> client.updateAlias(aliasName, oldIndex, newIndex))
            .isInstanceOf(IOException.class)
            .hasMessage("Update alias failed");
    }

    @Test
    void shouldRemoveIndexSuccessfully() throws IOException {
        // Given
        String indexName = "test-index";
        when(indicesClientMock.delete(any(DeleteIndexRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(acknowledgedResponse);
        when(acknowledgedResponse.isAcknowledged()).thenReturn(true);

        // When
        boolean result = client.removeIndex(indexName);

        // Then
        assertThat(result).isTrue();
        verify(indicesClientMock).delete(any(DeleteIndexRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldReturnFalseWhenRemoveIndexFails() throws IOException {
        // Given
        String indexName = "test-index";
        when(indicesClientMock.delete(any(DeleteIndexRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(acknowledgedResponse);
        when(acknowledgedResponse.isAcknowledged()).thenReturn(false);

        // When
        boolean result = client.removeIndex(indexName);

        // Then
        assertThat(result).isFalse();
        verify(indicesClientMock).delete(any(DeleteIndexRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldThrowExceptionWhenRemoveIndexFailsWithIOException() throws IOException {
        // Given
        String indexName = "test-index";
        when(indicesClientMock.delete(any(DeleteIndexRequest.class), eq(RequestOptions.DEFAULT)))
            .thenThrow(new IOException("Remove index failed"));

        // When / Then
        assertThatThrownBy(() -> client.removeIndex(indexName))
            .isInstanceOf(IOException.class)
            .hasMessage("Remove index failed");
    }

    @Test
    void shouldReindexDataSuccessfully() throws IOException {
        // Given
        String oldIndex = "old-index";
        String newIndex = "new-index";
        String taskId = "node:123";
        String responseBody = "{\"task\":\"" + taskId + "\"}";

        // Mock the low-level client for ReindexHelper
        when(elasticClient.getLowLevelClient()).thenReturn(restClient);
        when(restClient.performRequest(any())).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new java.io.ByteArrayInputStream(responseBody.getBytes()));

        // When
        String result = client.reindexData(oldIndex, newIndex, reindexListener);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(taskId);
    }

    @Test
    void shouldReindexDataThrowExceptionWhenReindexHelperFails() throws IOException {
        // Given
        String oldIndex = "old-index";
        String newIndex = "new-index";
        // Mock the low-level client for ReindexHelper
        when(elasticClient.getLowLevelClient()).thenReturn(restClient);
        when(restClient.performRequest(any())).thenThrow(new IOException("Reindex request failed"));

        // When / Then
        assertThatThrownBy(() -> client.reindexData(oldIndex, newIndex, reindexListener))
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void shouldRefreshIndexesSuccessfully() throws IOException {
        // Given
        String[] indexes = {"index1", "index2"};
        when(indicesClientMock.refresh(any(RefreshRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(refreshResponse);

        // When
        client.refresh(indexes);

        // Then
        verify(indicesClientMock).refresh(any(RefreshRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldHandleEmptyIndexesInRefresh() throws IOException {
        // Given
        String[] indexes = {};
        when(indicesClientMock.refresh(any(RefreshRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(refreshResponse);

        // When
        client.refresh(indexes);

        // Then
        verify(indicesClientMock).refresh(any(RefreshRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldHandleNullIndexesInRefresh() throws IOException {
        // Given
        String[] indexes = null;
        when(indicesClientMock.refresh(any(RefreshRequest.class), eq(RequestOptions.DEFAULT)))
            .thenReturn(refreshResponse);

        // When
        client.refresh(indexes);

        // Then
        verify(indicesClientMock).refresh(any(RefreshRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    void shouldThrowExceptionWhenRefreshFailsWithIOException() throws IOException {
        // Given
        String[] indexes = {"index1"};
        when(indicesClientMock.refresh(any(RefreshRequest.class), eq(RequestOptions.DEFAULT)))
            .thenThrow(new IOException("Refresh failed"));

        // When / Then
        assertThatThrownBy(() -> client.refresh(indexes))
            .isInstanceOf(IOException.class)
            .hasMessage("Refresh failed");
    }
}
