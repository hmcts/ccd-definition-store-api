package uk.gov.hmcts.ccd.definition.store.elastic.client;

import java.io.IOException;

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

@Component
@Slf4j
public class HighLevelCCDElasticClient implements CCDElasticClient {

    @Autowired
    CcdElasticSearchProperties config;

    @Autowired
    protected RestHighLevelClient elasticClient;

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
    public boolean upsertMapping(String indexName, String caseMapping) throws IOException {
        PutMappingRequest request = new PutMappingRequest(indexName);
        request.type(config.getCasesIndexType());
        request.source(caseMapping, XContentType.JSON);
        PutMappingResponse putMappingResponse = elasticClient.indices().putMapping(request);
        return putMappingResponse.isAcknowledged();
    }
}
