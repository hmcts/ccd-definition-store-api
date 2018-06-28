package uk.gov.hmcts.ccd.definition.store.elastic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;


@Service
@Slf4j
public class ElasticMappingCreator extends AbstractElasticSearchSupport {

    @Value("${ccd.elasticsearch.index.cases.name}")
    private String typeName;

    public void createMapping(String indexName, CaseTypeEntity caseType) throws IOException {
        log.info("creating mapping for case type {}", caseType.getReference());


        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        message.put("type", "text");
        Map<String, Object> properties = new HashMap<>();
        properties.put("message", message);
        jsonMap.put("properties", properties);

        PutMappingRequest request = createPutMappingRequest(indexName, jsonMap);

        PutMappingResponse putMappingResponse = elasticClient.indices().putMapping(request);

        boolean acknowledged = putMappingResponse.isAcknowledged();
        log.info("mapping created: {}", acknowledged);
    }

    private PutMappingRequest createPutMappingRequest(String indexName, Map<String, Object> mappings) {
        PutMappingRequest request = new PutMappingRequest(indexName);
        request.type(typeName);
        request.source(mappings);
        return request;
    }
}
