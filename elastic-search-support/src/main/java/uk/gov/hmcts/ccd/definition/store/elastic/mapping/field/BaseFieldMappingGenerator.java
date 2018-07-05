package uk.gov.hmcts.ccd.definition.store.elastic.mapping.field;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@Component
public class BaseFieldMappingGenerator extends AbstractMappingGenerator implements FieldMappingGenerator {

    @Override
    public String generateMapping(FieldEntity fieldEntity) {
        String ccdType = fieldEntity.getBaseTypeString();
        String configuredMapping = config.getTypeMappings().get(ccdType);
        if (configuredMapping == null) {
            throw new RuntimeException(String.format("unknown mapping for ccd type %s", ccdType));
        }
        return configuredMapping;
    }

    @Override
    public List<String> getCcdTypes() {
        return newArrayList(config.getTypeMappings().keySet());
    }
}
