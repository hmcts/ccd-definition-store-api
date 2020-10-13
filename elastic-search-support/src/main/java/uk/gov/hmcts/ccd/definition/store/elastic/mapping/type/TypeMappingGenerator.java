package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.MappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class TypeMappingGenerator extends MappingGenerator {

    public String doDataMapping(FieldEntity field) {
        return field.isSearchable() ? dataMapping(field) : disabled();
    }

    public String doDataClassificationMapping(FieldEntity field) {
        return field.isSearchable() ? dataClassificationMapping(field) : disabled();
    }

    protected abstract String dataMapping(FieldEntity field);

    protected abstract String dataClassificationMapping(FieldEntity field);

    public abstract List<String> getMappedTypes();

    protected Map<String, String> configuredTypeMappings() {
        return config.getTypeMappings();
    }

    protected String disabled() {
        return config.getElasticMappings().get("disabled");
    }

    protected String getConfiguredMapping(String ccdType) {
        return Optional.ofNullable(configuredTypeMappings().get(ccdType))
            .orElseThrow(() -> new ElasticSearchInitialisationException(
                String.format("no configured mapping for ccd type %s", ccdType)));
    }

    protected String securityClassificationMapping() {
        return config.getSecurityClassificationMapping();
    }
}
