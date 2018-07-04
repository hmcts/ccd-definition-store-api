package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapper;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@Component
public class CollectionTypeMappingGenerator extends AbstractMapper implements TypeMappingGenerator {

    @Override
    public String generateMapping(FieldEntity fieldEntity) throws IOException {
        String result = null;
        if (fieldEntity.getFieldType().getCollectionFieldType().getComplexFields() != null) {
                result = ((ComplexTypeMappingGenerator) getMapperForType("Complex")).generateMapping(fieldEntity.getFieldType().getCollectionFieldType().getComplexFields());
            } else {
                result = null;
            }
        return result;
    }

    @Override
    public List<String> getTypes() {
        return newArrayList("Collection");
    }
}
