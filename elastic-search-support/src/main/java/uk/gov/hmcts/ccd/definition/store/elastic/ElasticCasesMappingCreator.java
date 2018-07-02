package uk.gov.hmcts.ccd.definition.store.elastic;

import java.io.IOException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;


@Service
@Slf4j
public class ElasticCasesMappingCreator extends AbstractElasticSearchSupport {

    static final String JSON_PROPERTY_VALUE_PAIR = "\"%s\":%s,";

    public void createMapping(CaseTypeEntity caseType) throws IOException {
        log.info("creating mapping for case type: {}", caseType.getReference());

        StringBuilder propertiesMappings = new StringBuilder("{");

//        Map<String, Object> jsonMap = new HashMap<>();
//        Map<String, Object> properties = new HashMap<>();
//        jsonMap.put("properties", properties);

        for(Map.Entry<String, String> mapping : config.getCaseMappings().entrySet()) {
            String name = mapping.getKey();
            String elasticConfig = mapping.getValue();
            addJsonProperty(propertiesMappings, name, elasticConfig);
        }



        //TODO: test mapping of case without data
        StringBuilder dataMappings = null;
        if(!caseType.getCaseFields().isEmpty()) {
            dataMappings = new StringBuilder("{\"properties\": {");
            for (CaseFieldEntity f : caseType.getCaseFields()) {
                addJsonProperty(dataMappings, f.getReference(), mapping(f));
            }
            dataMappings.append("}");
            closeUP(dataMappings);
        }

        addJsonProperty(propertiesMappings, "data", dataMappings.toString());
        closeUP(propertiesMappings);
        String caseMapping = "{ \"properties\": " + propertiesMappings.toString() + "}";
        log.info("generated mapping: {}", caseMapping);

        PutMappingRequest request = createPutMappingRequest(indexName(caseType), caseMapping);

        PutMappingResponse putMappingResponse = elasticClient.indices().putMapping(request);

        boolean acknowledged = putMappingResponse.isAcknowledged();
        log.info("mapping created: {}", acknowledged);
    }

    private String mapping(CaseFieldEntity caseFieldEntity) {
        if (caseFieldEntity.isComplex()) {
            return config.getTypeMappings().get("Text");
        }

        return config.getTypeMappings().get(caseFieldEntity.getBaseTypeString());
    }

    private PutMappingRequest createPutMappingRequest(String indexName, String mappings) {
        PutMappingRequest request = new PutMappingRequest(indexName);
        request.type(config.getIndexCasesType());
        request.source(mappings, XContentType.JSON);
        return request;
    }

    private void addJsonProperty(StringBuilder json, String name, String value) {
        json.append(String.format(JSON_PROPERTY_VALUE_PAIR, name, value));
    }

    private void closeUP(StringBuilder jsonBlock) {
        jsonBlock.deleteCharAt(jsonBlock.lastIndexOf(","));
        jsonBlock.append("}");
    }
}
