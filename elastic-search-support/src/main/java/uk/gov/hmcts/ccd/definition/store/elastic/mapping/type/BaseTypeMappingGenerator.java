package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.List;

import org.springframework.stereotype.Component;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class BaseTypeMappingGenerator extends TypeMappingGenerator {

    public BaseTypeMappingGenerator(CcdElasticSearchProperties config) {
        super(config);
    }

    @Override
    public String dataMapping(FieldEntity field) {
        String ccdType = field.getBaseTypeString();
        return getConfiguredMapping(ccdType);
    }

    @Override
    public String dataClassificationMapping(FieldEntity field) {
        return securityClassificationMapping();
    }

    @Override
    public List<String> getMappedTypes() {
        return newArrayList(configuredTypeMappings().keySet());
    }
}
