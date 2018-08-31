package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import java.util.List;
import java.util.Map;

import uk.gov.hmcts.ccd.definition.store.elastic.mapping.MappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

public abstract class TypeMappingGenerator extends MappingGenerator {

    public abstract String dataMapping(FieldEntity field);

    public abstract String dataClassificationMapping(FieldEntity field);

    public abstract List<String> getMappedTypes();

    protected Map<String, String> typeMappings() {
        return config.getTypeMappings();
    }

    protected String disabled() {
        return config.getElasticMappings().get("disabled");
    }

    protected String getConfiguredMapping(String ccdType) {
        String configuredMapping = typeMappings().get(ccdType);
        if (configuredMapping == null) {
            throw new RuntimeException(String.format("no configured mapping for ccd type %s", ccdType));
        }
        return configuredMapping;
    }

    protected String securityClassificationMapping() {
        return config.getSecurityClassificationMapping();
    }
}
