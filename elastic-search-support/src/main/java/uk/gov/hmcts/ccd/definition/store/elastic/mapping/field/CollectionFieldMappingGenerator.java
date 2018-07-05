package uk.gov.hmcts.ccd.definition.store.elastic.mapping.field;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;

import com.google.gson.stream.JsonWriter;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@Component
public class CollectionFieldMappingGenerator extends AbstractMappingGenerator implements FieldMappingGenerator {

    @Override
    public String generateMapping(FieldEntity fieldEntity) throws IOException {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("id");
            jw.jsonValue(disabled());
            jw.name("value");
            jw.jsonValue(collectionTypeMapping(fieldEntity));
        }));
    }

    @Override
    public List<String> getCcdTypes() {
        return newArrayList("Collection");
    }

    private boolean isCollectionOfComplex(FieldEntity fieldEntity) {
        return fieldEntity.getFieldType().getCollectionFieldType().getComplexFields() != null;
    }

    private String collectionTypeMapping(FieldEntity fieldEntity) throws IOException {
        if (isCollectionOfComplex(fieldEntity)) {
            ComplexFieldMappingGenerator mapper = (ComplexFieldMappingGenerator) getMapperForType("Complex");
            return mapper.generateMapping(fieldEntity.getFieldType().getCollectionFieldType().getComplexFields());
        } else {
            return getMapperForType(fieldEntity.getBaseTypeString()).generateMapping(fieldEntity);
        }
    }
}
