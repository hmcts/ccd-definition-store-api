package uk.gov.hmcts.ccd.definition.store.elastic.client;

import javax.annotation.PreDestroy;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

@Component
@Slf4j
public class HighLevelCCDElasticClient implements CCDElasticClient {

    private static final String CASES_INDEX_SETTINGS_JSON = "/casesIndexSettings.json";
    protected CcdElasticSearchProperties config;

    protected RestHighLevelClient elasticClient;

    @Autowired
    public HighLevelCCDElasticClient(CcdElasticSearchProperties config, RestHighLevelClient elasticClient) {
        this.config = config;
        this.elasticClient = elasticClient;
    }

    @Override
    public boolean indexExists(String indexName) throws IOException {
        RestClient lowLevelClient = elasticClient.getLowLevelClient();
        Request request = new Request("HEAD", "/" + indexName + "?allow_no_indices=false");
        Response response = lowLevelClient.performRequest(request);
        boolean exists = response.getStatusLine().getStatusCode() == 200;
        log.info("index {} exists: {}", indexName, exists);
        return exists;
    }

    @Override
    public boolean createIndex(String indexName, String alias) throws IOException {
        log.info("creating index {} with alias {}", indexName, alias);
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.alias(new Alias(alias));
        request.settings(casesIndexSettings());
        CreateIndexResponse createIndexResponse = elasticClient.indices().create(request, RequestOptions.DEFAULT);
        log.info("index created: {}", createIndexResponse.isAcknowledged());
        return createIndexResponse.isAcknowledged();
    }

    @Override
    public boolean upsertMapping(String indexName, String caseTypeMapping) throws IOException {
        log.info("upsert mapping of most recent index for alias {}", indexName);
        GetAliasesResponse aliasesResponse = getAlias(indexName);
        String currentIndex = getCurrentAliasIndex(indexName, aliasesResponse);
        log.info("upsert mapping of index {}", currentIndex);
        PutMappingRequest request = new PutMappingRequest(currentIndex);
        request.type(config.getCasesIndexType());
        request.source(caseTypeMapping, XContentType.JSON);
        PutMappingResponse putMappingResponse = elasticClient.indices().putMapping(request, RequestOptions.DEFAULT);
        log.info("mapping upserted: {}", putMappingResponse.isAcknowledged());
        return putMappingResponse.isAcknowledged();
    }

    @Override
    public boolean aliasExists(String alias) throws IOException {
        GetAliasesRequest request = new GetAliasesRequest(alias);
        boolean exists = elasticClient.indices().existsAlias(request, RequestOptions.DEFAULT);
        log.info("alias {} exists: {}", alias, exists);
        return exists;
    }

    @PreDestroy
    public void cleanup() {
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

    private Settings.Builder casesIndexSettings() throws IOException {
        Settings.Builder settings = Settings.builder().loadFromStream(CASES_INDEX_SETTINGS_JSON,
            getClass().getResourceAsStream(CASES_INDEX_SETTINGS_JSON), false);
        settings.put("index.number_of_shards", config.getIndexShards());
        settings.put("index.number_of_replicas", config.getIndexShardsReplicas());
        return settings;
    }

    private String getCurrentAliasIndex(String indexName, GetAliasesResponse aliasesResponse) {
        ArrayList<String> indices = new ArrayList<>(aliasesResponse.getAliases().keySet());
        Collections.sort(indices);
        log.info("found following indexes for alias {}: {}", indexName, indices);
        return Iterables.getLast(indices);
    }
}
