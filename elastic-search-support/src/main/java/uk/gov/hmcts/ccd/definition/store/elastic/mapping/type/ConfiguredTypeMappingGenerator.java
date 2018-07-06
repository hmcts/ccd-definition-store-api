package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@Component
public class ConfiguredTypeMappingGenerator extends AbstractMappingGenerator implements TypeMappingGenerator {

    @Override
    public String dataMapping(FieldEntity field) {
        String ccdType = field.getBaseTypeString();
        String configuredMapping = config.getTypeMappings().get(ccdType);
        if (configuredMapping == null) {
            throw new RuntimeException(String.format("no configured mapping for ccd type %s", ccdType));
        }
        return configuredMapping;
    }

    @Override
    public String dataClassificationMapping(FieldEntity field) {
        return keyword();
    }

    @Override
    public List<String> getCcdTypes() {
        return newArrayList(config.getTypeMappings().keySet());
    }
}
