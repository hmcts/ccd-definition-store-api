package uk.gov.hmcts.ccd.definition.store.elastic.client;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

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
        Response response = lowLevelClient.performRequest("HEAD", "/" + indexName + "?allow_no_indices=false");
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
        CreateIndexResponse createIndexResponse = elasticClient.indices().create(request);
        log.info("index created: {}", createIndexResponse.isAcknowledged());
        return createIndexResponse.isAcknowledged();
    }

    @Override
    public boolean upsertMapping(String indexName, String caseTypeMapping) throws IOException {
        log.info("upsert mapping for alias {}", indexName);
        GetAliasesRequest requestWithAlias = new GetAliasesRequest(indexName);
        GetAliasesResponse alias = elasticClient.indices().getAlias(requestWithAlias, RequestOptions.DEFAULT);
        ArrayList<String> indices = new ArrayList<>(alias.getAliases().keySet());
        Collections.sort(indices);
        log.info("found following indexes for alias {}: {}", indexName, indices);
        String currentIndex = Iterables.getLast(indices);
        log.info("upsert mapping to index {}", currentIndex);
        PutMappingRequest request = new PutMappingRequest(currentIndex);
        request.type(config.getCasesIndexType());
        request.source(caseTypeMapping, XContentType.JSON);
        PutMappingResponse putMappingResponse = elasticClient.indices().putMapping(request);
        log.info("mapping upserted: {}", putMappingResponse.isAcknowledged());
        return putMappingResponse.isAcknowledged();
    }

    private Settings.Builder casesIndexSettings() throws IOException {
        Settings.Builder settings = Settings.builder().loadFromStream(CASES_INDEX_SETTINGS_JSON,
            getClass().getResourceAsStream(CASES_INDEX_SETTINGS_JSON), false);
        settings.put("index.number_of_shards", config.getIndexShards());
        settings.put("index.number_of_replicas", config.getIndexShardsReplicas());
        return settings;
    }
}
