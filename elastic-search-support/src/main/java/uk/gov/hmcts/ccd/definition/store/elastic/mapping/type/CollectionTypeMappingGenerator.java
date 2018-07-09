package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.google.gson.stream.JsonWriter;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

@Component
public class CollectionTypeMappingGenerator extends TypeMappingGenerator {

    @Override
    public String dataMapping(FieldEntity field) {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("properties");
            jw.beginObject();
            jw.name("id");
            jw.jsonValue(disabled());
            jw.name("value");
            jw.jsonValue(collectionTypeDataMapping(field));
            jw.endObject();
        }));
    }

    @Override
    public String dataClassificationMapping(FieldEntity field) {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("properties");
            jw.beginObject();
            jw.name("classification");
            jw.jsonValue(securityClassificationMapping());
            jw.name("value");
            jw.beginObject();
            jw.name("properties");
            jw.beginObject();
            jw.name("id");
            jw.jsonValue(disabled());
            jw.name("classification");
            jw.jsonValue(collectionTypeDataClassificationMapping(field));
            jw.endObject();
            jw.endObject();
            jw.endObject();
        }));
    }

    @Override
    public List<String> getCcdTypes() {
        return newArrayList("Collection");
    }

    private boolean isCollectionOfComplex(FieldEntity field) {
        return !field.getFieldType().getCollectionFieldType().getComplexFields().isEmpty();
    }

    private String collectionTypeDataMapping(FieldEntity field) {
        FieldTypeEntity collectionFieldType = field.getFieldType().getCollectionFieldType();
        if (isCollectionOfComplex(field)) {
            ComplexTypeMappingGenerator mapper = (ComplexTypeMappingGenerator) getTypeMapper("Complex");
            return mapper.dataMapping(collectionFieldType.getComplexFields());
        } else {
            return typeMappings().get(collectionFieldType.getReference());
        }
        //TODO collection of collection?
    }

    private String collectionTypeDataClassificationMapping(FieldEntity field) {
        if (isCollectionOfComplex(field)) {
            ComplexTypeMappingGenerator mapper = (ComplexTypeMappingGenerator) getTypeMapper("Complex");
            return mapper.dataClassificationMapping(field.getFieldType().getCollectionFieldType().getComplexFields());
        } else {
            return securityClassificationMapping();
        }
    }
}
