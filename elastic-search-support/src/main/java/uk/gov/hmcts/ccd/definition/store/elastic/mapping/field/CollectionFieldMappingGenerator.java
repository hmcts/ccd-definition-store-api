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
    public String dataMapping(FieldEntity fieldEntity) {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("properties");
            jw.beginObject();
            jw.name("id");
            jw.jsonValue(disabled());
            jw.name("value");
            jw.jsonValue(collectionTypeDataMapping(fieldEntity));
            jw.endObject();
        }));
    }

    @Override
    public String dataClassificationMapping(FieldEntity fieldEntity) {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("properties");
            jw.beginObject();
            jw.name("classification");
            jw.jsonValue(keyword());
            jw.name("value");
            jw.beginObject();
            jw.name("properties");
            jw.beginObject();
            jw.name("id");
            jw.jsonValue(disabled());
            jw.name("classification");
            jw.jsonValue(collectionTypeDataClassificationMapping(fieldEntity));
            jw.endObject();
            jw.endObject();
            jw.endObject();
        }));
    }

    @Override
    public List<String> getCcdTypes() {
        return newArrayList("Collection");
    }

    private boolean isCollectionOfComplex(FieldEntity fieldEntity) {
        return !fieldEntity.getFieldType().getCollectionFieldType().getComplexFields().isEmpty();
    }

    private String collectionTypeDataMapping(FieldEntity fieldEntity) {
        if (isCollectionOfComplex(fieldEntity)) {
            ComplexFieldMappingGenerator mapper = (ComplexFieldMappingGenerator) getMapperForType("Complex");
            return mapper.dataMapping(fieldEntity.getFieldType().getCollectionFieldType().getComplexFields());
        } else {
            return config.getTypeMappings().get(fieldEntity.getFieldType().getCollectionFieldType().getReference());
        }
    }

    private String collectionTypeDataClassificationMapping(FieldEntity fieldEntity) {
        if (isCollectionOfComplex(fieldEntity)) {
            ComplexFieldMappingGenerator mapper = (ComplexFieldMappingGenerator) getMapperForType("Complex");
            return mapper.dataClassificationMapping(fieldEntity.getFieldType().getCollectionFieldType().getComplexFields());
        } else {
            return keyword();
        }
    }
}
