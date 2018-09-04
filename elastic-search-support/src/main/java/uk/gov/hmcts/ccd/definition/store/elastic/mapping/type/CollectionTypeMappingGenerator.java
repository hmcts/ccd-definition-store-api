package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import com.google.gson.stream.JsonWriter;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class CollectionTypeMappingGenerator extends TypeMappingGenerator {

    @Override
    public String dataMapping(FieldEntity field) {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name(PROPERTIES);
            jw.beginObject();
            jw.name(ID);
            jw.jsonValue(disabled());
            jw.name(VALUE);
            jw.jsonValue(collectionTypeDataMapping(field));
            jw.endObject();
        }));
    }

    @Override
    public String dataClassificationMapping(FieldEntity field) {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name(PROPERTIES);
            jw.beginObject();
            jw.name(CLASSIFICATION);
            jw.jsonValue(securityClassificationMapping());
            jw.name(VALUE);
            jw.beginObject();
            jw.name(PROPERTIES);
            jw.beginObject();
            jw.name(ID);
            jw.jsonValue(disabled());
            jw.name(CLASSIFICATION);
            jw.jsonValue(collectionTypeDataClassificationMapping(field));
            jw.endObject();
            jw.endObject();
            jw.endObject();
        }));
    }

    @Override
    public List<String> getMappedTypes() {
        return newArrayList(COLLECTION);
    }

    private String collectionTypeDataMapping(FieldEntity field) {
        FieldTypeEntity collectionFieldType = field.getFieldType().getCollectionFieldType();
        if (field.isCollectionOfComplex()) {
            ComplexTypeMappingGenerator mapper = (ComplexTypeMappingGenerator) getTypeMapper(COMPLEX);
            return mapper.dataMapping(collectionFieldType.getComplexFields());
        } else {
            return getConfiguredMapping(collectionFieldType.getReference());
        }
    }

    private String collectionTypeDataClassificationMapping(FieldEntity field) {
        if (field.isCollectionOfComplex()) {
            ComplexTypeMappingGenerator mapper = (ComplexTypeMappingGenerator) getTypeMapper(COMPLEX);
            return mapper.dataClassificationMapping(field.getFieldType().getCollectionFieldType().getComplexFields());
        } else {
            return securityClassificationMapping();
        }
    }
}
