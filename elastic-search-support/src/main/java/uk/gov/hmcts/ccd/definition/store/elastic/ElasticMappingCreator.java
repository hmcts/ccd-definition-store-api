package uk.gov.hmcts.ccd.definition.store.elastic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;


@Service
@Slf4j
public class ElasticMappingCreator extends AbstractElasticSearchSupport {

    public void createMapping(String indexName, CaseTypeEntity caseType) throws IOException {
        log.info("creating mapping for case type {}", caseType.getReference());

        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        jsonMap.put("properties", properties);

        for(Map.Entry<String, String> mapping : config.getCaseMappings().entrySet()) {
            String name = mapping.getKey();
            String type = mapping.getValue();
            Map<String, Object> conf = new HashMap<>();
            conf.put("type", type);
            properties.put(name, conf);
        }

        Map<String, Object> data = new HashMap<>();
        properties.put("data", data);
        caseType.getCaseFields().forEach(f -> {


        });



        PutMappingRequest request = createPutMappingRequest(indexName, jsonMap);

        PutMappingResponse putMappingResponse = elasticClient.indices().putMapping(request);

        boolean acknowledged = putMappingResponse.isAcknowledged();
        log.info("mapping created: {}", acknowledged);
    }

    private PutMappingRequest createPutMappingRequest(String indexName, Map<String, Object> mappings) {
        PutMappingRequest request = new PutMappingRequest(indexName);
        request.type(config.getIndexCasesType());
        request.source(mappings);
        return request;
    }
}
