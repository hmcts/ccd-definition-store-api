package uk.gov.hmcts.ccd.definition.store.elastic.client;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.definition.store.elastic.ReindexHelper;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;

import static uk.gov.hmcts.ccd.definition.store.elastic.ElasticGlobalSearchListener.GLOBAL_SEARCH;

@Slf4j
@SuppressWarnings("java:S1874")
public class HighLevelCCDElasticClient implements CCDElasticClient, AutoCloseable {

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
        request.source(caseTypeMapping, XContentType.JSON);
        AcknowledgedResponse acknowledgedResponse = elasticClient.indices().putMapping(request, RequestOptions.DEFAULT);
        log.info("mapping upserted: {}", acknowledgedResponse.isAcknowledged());
        return acknowledgedResponse.isAcknowledged();
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

    public void setIndexReadOnly(String indexName, boolean readOnly) throws IOException {
        UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest(indexName);
        Settings settings = Settings.builder()
            .put("index.blocks.read_only", readOnly)
            .build();
        updateSettingsRequest.settings(settings);
        elasticClient.indices().putSettings(updateSettingsRequest, RequestOptions.DEFAULT);
    }

    public boolean createIndexAndMapping(String indexName, String caseTypeMapping) throws IOException {
        //create new index
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(casesIndexSettings(CASES_INDEX_SETTINGS_JSON));
        CreateIndexResponse createIndexResponse = elasticClient.indices().create(request, RequestOptions.DEFAULT);
        log.info("index created: {}", createIndexResponse.isAcknowledged());

        //upsert mapping to new index
        PutMappingRequest putRequest = new PutMappingRequest(indexName);
        putRequest.source(caseTypeMapping, XContentType.JSON);

        AcknowledgedResponse acknowledgedResponse = elasticClient.indices()
            .putMapping(putRequest, RequestOptions.DEFAULT);
        log.info("mapping upserted: {}", acknowledgedResponse.isAcknowledged());

        return createIndexResponse.isAcknowledged();
    }

    public boolean updateAlias(String aliasName, String oldIndex, String newIndex) throws IOException {
        IndicesAliasesRequest aliasRequest = new IndicesAliasesRequest();
        aliasRequest.addAliasAction(IndicesAliasesRequest.AliasActions.remove().index(oldIndex).alias(aliasName));
        aliasRequest.addAliasAction(IndicesAliasesRequest.AliasActions.add().index(newIndex).alias(aliasName));

        AcknowledgedResponse aliasResponse = elasticClient.indices()
            .updateAliases(aliasRequest, RequestOptions.DEFAULT);
        if (aliasResponse.isAcknowledged()) {
            log.info("alias successfully updated: {} now points to {}", oldIndex, newIndex);
        } else {
            log.info("alias update failed: {} still points to {}", oldIndex, oldIndex);
        }
        return aliasResponse.isAcknowledged();
    }

    public boolean removeIndex(String indexName) throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
        AcknowledgedResponse deleteResponse = elasticClient.indices()
            .delete(deleteIndexRequest, RequestOptions.DEFAULT);
        if (deleteResponse.isAcknowledged()) {
            log.info("successfully deleted index: {}", indexName);
        } else {
            log.info("failed to delete index: {}", indexName);
        }
        return deleteResponse.isAcknowledged();
    }

    public String reindexData(String oldIndex, String newIndex,
                            ReindexListener listener) {
        ReindexHelper helper = new ReindexHelper(elasticClient);
        try {
            return helper.reindexIndex(oldIndex, newIndex, 5000, listener);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void refresh(String... indexes) throws IOException {
        elasticClient.indices().refresh(new RefreshRequest(indexes),  RequestOptions.DEFAULT);
    }

    public long countDocuments(String indexName) throws IOException {
        CountRequest countRequest = new CountRequest(indexName);
        CountResponse countResponse = elasticClient.count(countRequest, RequestOptions.DEFAULT);
        return countResponse.getCount();
    }
}
