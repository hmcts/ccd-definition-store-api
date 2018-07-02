package uk.gov.hmcts.ccd.definition.store.elastic;

import java.io.IOException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;


@Service
@Slf4j
public class ElasticCasesMappingCreator extends AbstractElasticSearchSupport {

    public void createMapping(CaseTypeEntity caseType) throws IOException {
        log.info("creating mapping for case type: {}", caseType.getReference());

        StringBuilder mps = new StringBuilder();
        mps.append("{");

//        Map<String, Object> jsonMap = new HashMap<>();
//        Map<String, Object> properties = new HashMap<>();
//        jsonMap.put("properties", properties);

        for(Map.Entry<String, String> mapping : config.getCaseMappings().entrySet()) {
            String name = mapping.getKey();
            String elasticConfig = mapping.getValue();
            mps.append("\"").append(name).append("\":").append(elasticConfig).append(",");
        }

        mps.deleteCharAt(mps.lastIndexOf(","));
        mps.append("}");
        String fieldsMappings = "{ \"properties\": " + mps.toString() + "}";
        log.info("generated mapping: {}", fieldsMappings);


//        Map<String, Object> data = new HashMap<>();
//        properties.put("data", data);
        caseType.getCaseFields().forEach(f -> {


        });


        PutMappingRequest request = createPutMappingRequest(indexName(caseType), fieldsMappings);

        PutMappingResponse putMappingResponse = elasticClient.indices().putMapping(request);

        boolean acknowledged = putMappingResponse.isAcknowledged();
        log.info("mapping created: {}", acknowledged);
    }

    private PutMappingRequest createPutMappingRequest(String indexName, String mappings) {
        PutMappingRequest request = new PutMappingRequest(indexName);
        request.type(config.getIndexCasesType());
        request.source(mappings, XContentType.JSON);
        return request;
    }
}
