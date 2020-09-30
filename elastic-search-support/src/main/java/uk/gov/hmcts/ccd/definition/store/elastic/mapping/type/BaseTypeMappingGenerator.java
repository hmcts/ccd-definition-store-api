package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class BaseTypeMappingGenerator extends TypeMappingGenerator {

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
