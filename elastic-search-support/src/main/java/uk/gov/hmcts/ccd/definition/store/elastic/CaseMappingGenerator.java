package uk.gov.hmcts.ccd.definition.store.elastic;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@Component
@Slf4j
public class CaseMappingGenerator extends AbstractElasticSearchSupport {

    private static final String JSON_PROPERTY_VALUE_PAIR = "\"%s\":%s,";

    public String generate(CaseTypeEntity caseType) {
        log.info("creating mapping for case type: {}", caseType.getReference());

        String dataMappings = dataMappings(caseType);
        StringBuilder propertiesMappings = getPropertiesMapping();
        addJsonProperty(propertiesMappings, "data", dataMappings);
        finalise(propertiesMappings);
        String caseMapping = "{ \"properties\": " + propertiesMappings.toString() + "}";

        log.info("generated mapping: {}", caseMapping);
//        log.debug("generated mapping: {}", caseMapping);
        return caseMapping;
    }

    private StringBuilder getPropertiesMapping() {
        StringBuilder propertiesMappings = new StringBuilder("{");

        for(Map.Entry<String, String> mapping : config.getCaseMappings().entrySet()) {
            String name = mapping.getKey();
            String elasticConfig = mapping.getValue();
            addJsonProperty(propertiesMappings, name, elasticConfig);
        }
        return propertiesMappings;
    }

    private String dataMappings(CaseTypeEntity caseType) {
        //TODO: test mapping of case without data
        StringBuilder dataMappings = null;
        if(!caseType.getCaseFields().isEmpty()) {
            dataMappings = new StringBuilder("{\"properties\": {");
            for (CaseFieldEntity f : caseType.getCaseFields()) {
                if (!config.getTypeMappingsIgnored().contains(f.getBaseTypeString())) {
                    addJsonProperty(dataMappings, f.getReference(), mapping(f));
                }
            }
            dataMappings.append("}");
            finalise(dataMappings);
        }
        return dataMappings.toString();
    }

    private String mapping(FieldEntity caseFieldEntity) {
        String result = null;
        if (caseFieldEntity.isComplex()) {
            result = mappingForComplexField(caseFieldEntity.getFieldType().getComplexFields());
        } else if (caseFieldEntity.isCollection()) {
            if (caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields() != null) {
                result = mappingForComplexField(caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields());
            } else {
                result = null;
            }
        } else {
            String ccdType = caseFieldEntity.getBaseTypeString();
            String configuredMapping = config.getTypeMappings().get(ccdType);
            if (configuredMapping == null) {
                throw new RuntimeException(String.format("unknown mapping for ccd type %s", ccdType));
            }
            result = configuredMapping;
        }
        return result;
    }

    private String mappingForComplexField(List<ComplexFieldEntity> fields) {
        StringBuilder complexMapping = new StringBuilder("{\"properties\": {");
        for (ComplexFieldEntity f : fields) {
            addJsonProperty(complexMapping, f.getReference(), mapping(f));
        }
        complexMapping.append("}");
        finalise(complexMapping);
        return complexMapping.toString();
    }

    private void addJsonProperty(StringBuilder json, String name, String value) {
        json.append(String.format(JSON_PROPERTY_VALUE_PAIR, name, value));
    }

    private void finalise(StringBuilder jsonBlock) {
        jsonBlock.deleteCharAt(jsonBlock.lastIndexOf(","));
        jsonBlock.append("}");
    }
}
