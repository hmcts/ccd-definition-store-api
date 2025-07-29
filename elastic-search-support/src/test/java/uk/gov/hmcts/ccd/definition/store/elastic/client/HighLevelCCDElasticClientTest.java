package uk.gov.hmcts.ccd.definition.store.elastic.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.AliasDefinition;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.GetAliasRequest;
import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import co.elastic.clients.elasticsearch.indices.UpdateAliasesRequest;
import co.elastic.clients.elasticsearch.indices.UpdateAliasesResponse;
import co.elastic.clients.elasticsearch.indices.get_alias.IndexAliases;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HighLevelCCDElasticClientTest {

    @Mock
    private ElasticsearchClient elasticClient;

    @Mock
    private CcdElasticSearchProperties config;

    @InjectMocks
    private HighLevelCCDElasticClient highLevelCCDElasticClient;

    @Mock
    private ElasticsearchIndicesClient indicesClient;

    @BeforeEach
    void setup() {
        when(elasticClient.indices()).thenReturn(indicesClient);
        doReturn(indicesClient).when(elasticClient).indices();
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

        doReturn(response).when(indicesClient).create(any(CreateIndexRequest.class));

        GetAliasResponse realAliasResponse = new GetAliasResponse.Builder()
            .aliases(Map.of(alias, createIndexAliases(indexName)))
            .build();
        doReturn(realAliasResponse).when(indicesClient).getAlias(any(GetAliasRequest.class));

        assertThat(highLevelCCDElasticClient.createIndex(indexName, alias)).isTrue();
        verify(indicesClient).create(any(CreateIndexRequest.class));
    }

    @Test
    void aliasExistsReturnsTrueWhenAliasExists() throws IOException {
        final String alias = "existing_alias";
        final String index = "old_index";

        // Build a real GetAliasResponse with the alias present in the map
        GetAliasResponse realResponse = new GetAliasResponse.Builder()
            .aliases(Map.of(alias, createIndexAliases(index)))
            .build();

        // Return the real response when getAlias is called
        doReturn(realResponse).when(indicesClient).getAlias(any(GetAliasRequest.class));

        assertThat(highLevelCCDElasticClient.aliasExists(alias)).isTrue();
        verify(indicesClient).getAlias(any(GetAliasRequest.class));
    }

    @Test
    void aliasExistsReturnsFalseWhenAliasDoesNotExist() throws IOException {
        final String alias = "non_existing_alias";

        // Use the real builder instead of a mock
        GetAliasResponse realResponse = new GetAliasResponse.Builder()
            .aliases(Collections.emptyMap())
            .build();

        doReturn(realResponse).when(indicesClient).getAlias(any(GetAliasRequest.class));

        assertThat(highLevelCCDElasticClient.aliasExists(alias)).isFalse();
        verify(indicesClient).getAlias(any(GetAliasRequest.class));
    }

    @Test
    void updateAliasUpdatesAliasSuccessfully() throws IOException {
        final String alias = "test_alias";
        final String oldIndex = "old_index";
        final String newIndex = "new_index";

        UpdateAliasesResponse response = new UpdateAliasesResponse.Builder()
            .acknowledged(true)
            .build();

        doReturn(response).when(indicesClient).updateAliases(any(UpdateAliasesRequest.class));

        assertThat(highLevelCCDElasticClient.updateAlias(alias, oldIndex, newIndex)).isTrue();
        verify(indicesClient).updateAliases(any(UpdateAliasesRequest.class));
    }

    @Test
    void updateAliasFailsWhenAliasUpdateNotAcknowledged() throws IOException {
        final String alias = "test_alias";
        final String oldIndex = "old_index";
        final String newIndex = "new_index";

        UpdateAliasesResponse response = new UpdateAliasesResponse.Builder()
            .acknowledged(false)
            .build();

        doReturn(response).when(indicesClient).updateAliases(any(UpdateAliasesRequest.class));

        assertThat(highLevelCCDElasticClient.updateAlias(alias, oldIndex, newIndex)).isFalse();
        verify(indicesClient).updateAliases(any(UpdateAliasesRequest.class));
    }

    @Test
    void aliasExistsReturnsFalseOn404ElasticsearchException() throws IOException {
        final String alias = "missing_alias";

        ElasticsearchException ex = new ElasticsearchException("alias not found", RestStatus.NOT_FOUND);
        doThrow(ex).when(indicesClient).getAlias(any(GetAliasRequest.class));

        ElasticsearchException thrown = assertThrows(
            ElasticsearchException.class,
            () -> highLevelCCDElasticClient.aliasExists(alias)
        );
        assertThat(thrown.status()).isEqualTo(RestStatus.NOT_FOUND);
    }

    private IndexAliases createIndexAliases(String index) {
        IndexAliases.Builder builder = new IndexAliases.Builder();
        builder.aliases(index, new AliasDefinition.Builder().build());
        return builder.build();
    }
}
