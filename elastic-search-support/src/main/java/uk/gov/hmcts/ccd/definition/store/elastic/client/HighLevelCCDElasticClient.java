package uk.gov.hmcts.ccd.definition.store.elastic.client;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;

import java.io.IOException;

@Component
@Slf4j
public class HighLevelCCDElasticClient implements CCDElasticClient {

    protected CcdElasticSearchProperties config;

    protected RestHighLevelClient elasticClient;

    @Autowired
    public HighLevelCCDElasticClient(CcdElasticSearchProperties config, RestHighLevelClient elasticClient) {
        this.config = config;
        this.elasticClient = elasticClient;
    }

    @Override
    public boolean indexExists(String indexName) throws IOException {
        GetIndexRequest getReq = new GetIndexRequest();
        getReq.indices(indexName);
        boolean exists = elasticClient.indices().exists(getReq);
        log.info("index {} exists: {}", indexName, exists);
        return exists;
    }

    @Override
    public boolean createIndex(String indexName) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(Settings.builder()
                .put("index.number_of_shards", config.getIndexShards())
                .put("index.number_of_replicas", config.getIndexShardsReplicas())
        );
        CreateIndexResponse createIndexResponse = elasticClient.indices().create(request);
        return createIndexResponse.isAcknowledged();
    }

    @Override
    public boolean upsertMapping(String indexName, String caseTypeMapping) throws IOException {
        PutMappingRequest request = new PutMappingRequest(indexName);
        request.type(config.getCasesIndexType());
        request.source(caseTypeMapping, XContentType.JSON);
        PutMappingResponse putMappingResponse = elasticClient.indices().putMapping(request);
        return putMappingResponse.isAcknowledged();
    }
}
