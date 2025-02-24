package uk.gov.hmcts.ccd.definition.store.elastic.client;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static uk.gov.hmcts.ccd.definition.store.elastic.ElasticGlobalSearchListener.GLOBAL_SEARCH;

@Slf4j
@SuppressWarnings("java:S1874")
public class HighLevelCCDElasticClient implements CCDElasticClient {

    private static final String CASES_INDEX_SETTINGS_JSON = "/casesIndexSettings.json";
    private static final String GLOBAL_SEARCH_CASES_INDEX_SETTINGS_JSON = "/globalSearchCasesIndexSettings.json";
    protected CcdElasticSearchProperties config;

    protected RestHighLevelClient elasticClient;

    @Autowired
    public HighLevelCCDElasticClient(CcdElasticSearchProperties config, RestHighLevelClient elasticClient) {
        this.config = config;
        this.elasticClient = elasticClient;
    }

    @Override
    public boolean createIndex(String indexName, String alias) throws IOException {
        log.info("creating index {} with alias {}", indexName, alias);
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.alias(new Alias(alias));
        String file = (alias.equalsIgnoreCase(GLOBAL_SEARCH))
            ? GLOBAL_SEARCH_CASES_INDEX_SETTINGS_JSON : CASES_INDEX_SETTINGS_JSON;
        request.settings(casesIndexSettings(file));
        CreateIndexResponse createIndexResponse = elasticClient.indices().create(request, RequestOptions.DEFAULT);
        log.info("index created: {}", createIndexResponse.isAcknowledged());
        return createIndexResponse.isAcknowledged();
    }

    @Override
    public boolean upsertMapping(String aliasName, String caseTypeMapping) throws IOException {
        log.info("upsert mapping of most recent index for alias {}", aliasName);
        GetAliasesResponse aliasesResponse = getAlias(aliasName);
        String currentIndex = getCurrentAliasIndex(aliasName, aliasesResponse);
        log.info("upsert mapping of index {}", currentIndex);
        PutMappingRequest request = new PutMappingRequest(currentIndex);
        request.type(config.getCasesIndexType());
        request.source(caseTypeMapping, XContentType.JSON);
        AcknowledgedResponse acknowledgedResponse = elasticClient.indices().putMapping(request, RequestOptions.DEFAULT);
        log.info("mapping upserted: {}", acknowledgedResponse.isAcknowledged());
        return acknowledgedResponse.isAcknowledged();
    }

    @Override
    public boolean indexAndMapping(String indexName, String aliasName, String caseTypeMapping) throws IOException {
        //create index and mapping but no alias
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        CreateIndexResponse createIndexResponse = elasticClient.indices().create(request, RequestOptions.DEFAULT);
        log.info("index created: {}", createIndexResponse.isAcknowledged());
        upsertMapping(aliasName, caseTypeMapping);
        return createIndexResponse.isAcknowledged();
    }

    @Override
    public boolean updateAlias(String aliasName, String oldIndex, String newIndex) throws IOException {
        IndicesAliasesRequest aliasRequest = new IndicesAliasesRequest();
        aliasRequest.addAliasAction(IndicesAliasesRequest.AliasActions.remove().index(oldIndex).alias(aliasName));
        aliasRequest.addAliasAction(IndicesAliasesRequest.AliasActions.add().index(newIndex).alias(aliasName));

        AcknowledgedResponse aliasResponse = elasticClient.indices().updateAliases(aliasRequest, RequestOptions.DEFAULT);
        if (aliasResponse.isAcknowledged()) {
            log.info("Alias updated: {} now points to {}", aliasName, newIndex);
        } else {
            log.info("Alias update failed: {} still points to {}", aliasName, oldIndex);
        }
        return aliasResponse.isAcknowledged();
    }

    @Override
    public boolean aliasExists(String alias) throws IOException {
        GetAliasesRequest request = new GetAliasesRequest(alias);
        boolean exists = elasticClient.indices().existsAlias(request, RequestOptions.DEFAULT);
        log.info("alias {} exists: {}", alias, exists);
        return exists;
    }

    @Override
    public void close() {
        try {
            log.info("Closing the ES REST client");
            this.elasticClient.close();
        } catch (IOException ioe) {
            log.error("Problem occurred when closing the ES REST client", ioe);
        }
    }

    public GetAliasesResponse getAlias(String alias) throws IOException {
        GetAliasesRequest request = new GetAliasesRequest(alias);
        return elasticClient.indices().getAlias(request, RequestOptions.DEFAULT);
    }

    private Settings.Builder casesIndexSettings(String file) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(file)) {
            Settings.Builder settings = Settings.builder().loadFromStream(file,
                inputStream, false);
            settings.put("index.number_of_shards", config.getIndexShards());
            settings.put("index.number_of_replicas", config.getIndexShardsReplicas());
            settings.put("index.mapping.total_fields.limit", config.getCasesIndexMappingFieldsLimit());
            return settings;
        }
    }

    private String getCurrentAliasIndex(String indexName, GetAliasesResponse aliasesResponse) {
        ArrayList<String> indices = new ArrayList<>(aliasesResponse.getAliases().keySet());
        Collections.sort(indices);
        log.info("found following indexes for alias {}: {}", indexName, indices);
        return Iterables.getLast(indices);
    }

    public CompletableFuture<Boolean> reindexData(String oldIndex, String newIndex) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            ReindexRequest request = new ReindexRequest();
            request.setSourceIndices(oldIndex);
            request.setDestIndex(newIndex);
            log.info("Reindexing initiating");
            log.info("Reindexing from {} to {}", oldIndex, newIndex);

            elasticClient.reindexAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {

                @Override
                public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                    log.info("Reindexing Completed");
                    log.info("Reindexing Summary: {}", bulkByScrollResponse.getStatus());
                    future.complete(true);
                }

                @Override
                public void onFailure(Exception e) {
                    log.error("Reindexing failed", e);
                    future.complete(false);
                }
            });
        } catch (Exception e) {
            log.error("Error initiating reindexing", e);
            future.complete(false);
        }
        return future;
    }
}
