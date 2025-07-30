package uk.gov.hmcts.ccd.definition.store.elastic.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.indices.get_alias.IndexAliases;
import co.elastic.clients.util.ObjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.Mock;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HighLevelCCDElasticClientTest {

    @Mock
    private ElasticsearchClient elasticClient;

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
        highLevelCCDElasticClient = Mockito.spy(new HighLevelCCDElasticClient(config, elasticClient));
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
        verify(indicesClient)
            .getAlias(Mockito.<java.util.function.Function<GetAliasRequest.Builder,
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
        verify(indicesClient)
            .updateAliases(Mockito.<java.util.function.Function<UpdateAliasesRequest.Builder,
                co.elastic.clients.util.ObjectBuilder<UpdateAliasesRequest>>>any());
    }

    @Test
    void aliasExistsReturnsFalseOn404ElasticsearchException() throws IOException {
        final String alias = "missing_alias";

        assertThat(highLevelCCDElasticClient.aliasExists(alias)).isFalse();
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
