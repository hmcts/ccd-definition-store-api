package uk.gov.hmcts.ccd.definition.store.elastic;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@Component
public class CollectionFieldMappingGenerator extends AbstractMapper implements TypeMappingGenerator, Injectable {

    @Override
    public String generateMapping(FieldEntity fieldEntity) throws IOException {
        String result = null;
        if (fieldEntity.getFieldType().getCollectionFieldType().getComplexFields() != null) {
                result = ((ComplexFieldMappingGenerator) getMapperForType("Complex")).generateMapping(fieldEntity.getFieldType().getCollectionFieldType().getComplexFields());
            } else {
                result = null;
            }
        return result;
    }

    @Override
    public List<String> getTypes() {
        return newArrayList("Collection");
    }

    public void inject(MappersManager mappersManager){
        this.typeMappers = mappersManager.getTypeMappers();
    }
}
