package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@Component
public class BaseTypeMappingGenerator extends TypeMappingGenerator {

    @Override
    public String dataMapping(FieldEntity field) {
        return conditionalMapping(field, () -> getConfiguredMapping(field.getBaseTypeString()));
    }

    @Override
    public String dataClassificationMapping(FieldEntity field) {
        return conditionalMapping(field, this::securityClassificationMapping);
    }

    @Override
    public List<String> getMappedTypes() {
        return newArrayList(configuredTypeMappings().keySet());
    }
}
