package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@Component
public class ConfiguredTypeMappingGenerator extends TypeMappingGenerator {

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
    public List<String> getCcdTypes() {
        return newArrayList(typeMappings().keySet());
    }
}
