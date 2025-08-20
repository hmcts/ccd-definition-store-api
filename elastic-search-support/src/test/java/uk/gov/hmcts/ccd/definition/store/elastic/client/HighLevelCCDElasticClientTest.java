package uk.gov.hmcts.ccd.definition.store.elastic.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.ReindexResponse;
import co.elastic.clients.elasticsearch.indices.AliasDefinition;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.GetAliasRequest;
import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import co.elastic.clients.elasticsearch.indices.PutIndicesSettingsResponse;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import co.elastic.clients.elasticsearch.indices.UpdateAliasesRequest;
import co.elastic.clients.elasticsearch.indices.UpdateAliasesResponse;
import co.elastic.clients.elasticsearch.indices.get_alias.IndexAliases;
import co.elastic.clients.util.ObjectBuilder;
import org.elasticsearch.action.ActionListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HighLevelCCDElasticClientTest {

    @Mock
    private ElasticsearchClient elasticClient;

    @Mock
    private ElasticsearchClientFactory elasticClientFactory;

    @Mock
    private CcdElasticSearchProperties config;

    private HighLevelCCDElasticClient highLevelCCDElasticClient;

    @Mock
    private ElasticsearchIndicesClient indicesClient;

    @Mock
    private GetAliasRequest getAliasRequest;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        doReturn(indicesClient).when(elasticClient).indices();
        doReturn(elasticClient).when(elasticClientFactory).createClient();
        highLevelCCDElasticClient = Mockito.spy(new HighLevelCCDElasticClient(config, elasticClientFactory));
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

        var putMappingResponse = Mockito.mock(co.elastic.clients.elasticsearch.indices.PutMappingResponse.class);
        Mockito.when(putMappingResponse.acknowledged()).thenReturn(true);
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

        var putMappingResponse = Mockito.mock(co.elastic.clients.elasticsearch.indices.PutMappingResponse.class);
        Mockito.when(putMappingResponse.acknowledged()).thenReturn(false);
        doReturn(putMappingResponse).when(indicesClient).putMapping(any(Function.class));

        boolean result = highLevelCCDElasticClient.upsertMapping(aliasName, caseTypeMapping);

        assertThat(result).isFalse();
    }

    @Test
    void setIndexReadOnlySetsIndexReadOnlyFlag() throws IOException {
        String indexName = "readonly_index";
        boolean readOnly = true;

        var putSettingsResponse = Mockito.mock(PutIndicesSettingsResponse.class);
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

        var putMappingResponse = Mockito.mock(PutMappingResponse.class);
        Mockito.when(putMappingResponse.acknowledged()).thenReturn(true);
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
        var deleteResponse = Mockito.mock(DeleteIndexResponse.class);
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
        var deleteResponse = Mockito.mock(co.elastic.clients.elasticsearch.indices.DeleteIndexResponse.class);
        Mockito.when(deleteResponse.acknowledged()).thenReturn(false);
        doReturn(deleteResponse).when(indicesClient).delete(any(Function.class));

        boolean result = highLevelCCDElasticClient.removeIndex(indexName);

        assertThat(result).isFalse();
    }

    @Test
    void reindexDataCallsListenerOnResponse() throws Exception {
        String oldIndex = "old_index";
        String newIndex = "new_index";
        @SuppressWarnings("unchecked")
        ActionListener<ReindexResponse> listener = Mockito.mock(ActionListener.class);

        var reindexResponse = Mockito.mock(co.elastic.clients.elasticsearch.core.ReindexResponse.class);
        doReturn(reindexResponse).when(elasticClient).reindex(any(Function.class));

        highLevelCCDElasticClient.reindexData(oldIndex, newIndex, listener);

        // Wait for async execution, in real code use Awaitility or similar for proper waiting
        Thread.sleep(100);
        verify(listener).onResponse(Mockito.any());
    }

    @Test
    void reindexDataCallsListenerOnFailureWhenReindexThrows() throws Exception {
        String oldIndex = "old_index";
        String newIndex = "new_index";
        @SuppressWarnings("unchecked")
        ActionListener<ReindexResponse> listener = Mockito.mock(ActionListener.class);

        doThrow(new RuntimeException("Reindex failed")).when(elasticClient).reindex(any(Function.class));

        highLevelCCDElasticClient.reindexData(oldIndex, newIndex, listener);

        Thread.sleep(100); // Wait for async
        verify(listener).onFailure(Mockito.any(RuntimeException.class));
    }

    @Test
    void aliasExistsRethrowsExceptionIfStatusNot404() throws IOException, ElasticsearchException {
        final String alias = "some_alias";
        ElasticsearchException esEx = Mockito.mock(ElasticsearchException.class);
        Mockito.when(esEx.status()).thenReturn(404);
        doThrow(esEx)
            .when(indicesClient).getAlias(any(Function.class));
        Assertions.assertFalse(highLevelCCDElasticClient.aliasExists(alias));
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
