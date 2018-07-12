package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.ComplexTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

public class StubComplexTypeMappingGenerator extends ComplexTypeMappingGenerator {

    private String dataMapping;
    private String dataClassificationMapping;
    private String type;

    public StubComplexTypeMappingGenerator(String type, String dataMapping, String dataClassificationMapping) {
        this.type = type;
        this.dataMapping = dataMapping;
        this.dataClassificationMapping = dataClassificationMapping;
    }

    public String dataMapping(FieldEntity field) {
        return dataMapping;
    }

    public String dataClassificationMapping(FieldEntity field) {
        return dataClassificationMapping;
    }

    public List<String> getCcdTypes() {
        return newArrayList(type);
    }

    protected String securityClassificationMapping() {
        return "securityClassificationMapping";
    }
}
