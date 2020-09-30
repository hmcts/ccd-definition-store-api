package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.TypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class StubTypeMappingGenerator extends TypeMappingGenerator {

    private String dataMapping;
    private String dataClassificationMapping;
    private String type;

    public StubTypeMappingGenerator(String type, String dataMapping, String dataClassificationMapping) {
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

    public List<String> getMappedTypes() {
        return newArrayList(type);
    }

    public boolean shouldIgnore(FieldEntity field) {
        return field.getBaseTypeString().equals("Label");
    }

    @Override
    protected String disabled() {
        return "{\"enabled\": false}";
    }
}
