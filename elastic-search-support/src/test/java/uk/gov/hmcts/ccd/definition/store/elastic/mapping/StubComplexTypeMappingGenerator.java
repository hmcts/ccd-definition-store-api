package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.ComplexTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.collect.Lists.newArrayList;

public class StubComplexTypeMappingGenerator extends ComplexTypeMappingGenerator {

    private String dataMapping;
    private String dataClassificationMapping;
    private String type;

    @Autowired
    public StubComplexTypeMappingGenerator(CcdElasticSearchProperties config, 
            String type, String dataMapping, String dataClassificationMapping) {
        super(config);
        this.type = type;
        this.dataMapping = dataMapping;
        this.dataClassificationMapping = dataClassificationMapping;
    }

    public String disabled() {
        return "\"disabled\"";
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

    protected String securityClassificationMapping() {
        return "securityClassificationMapping";
    }
}
