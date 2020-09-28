package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import com.google.gson.stream.JsonWriter;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

@Component
public class CollectionTypeMappingGenerator extends TypeMappingGenerator {

    @Override
    public String dataMapping(FieldEntity collectionField) {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name(PROPERTIES);
            jw.beginObject();
            jw.name(ID);
            jw.jsonValue(disabled());
            jw.name(VALUE);
            jw.jsonValue(collectionTypeDataMapping(collectionField));
            jw.endObject();
        }));
    }

    @Override
    public String dataClassificationMapping(FieldEntity collectionField) {
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
            if (!collectionField.isCollectionOfComplex()) {
                jw.name(CLASSIFICATION);
                jw.jsonValue(securityClassificationMapping());
            } else {
                jw.name(VALUE);
                jw.beginObject();
                jw.name(PROPERTIES);
                jw.beginObject();
                List<ComplexFieldEntity> complexFields = collectionField.getFieldType()
                    .getCollectionFieldType().getComplexFields();
                List<ComplexFieldEntity> notIgnoredFields = complexFields.stream()
                    .filter(f -> !shouldIgnore(f)).collect(toList());

                for (ComplexFieldEntity complexField : notIgnoredFields) {
                    TypeMappingGenerator typeMapper = getTypeMapper(complexField.getBaseTypeString());
                    String mapping = typeMapper.doDataClassificationMapping(complexField);
                    jw.name(complexField.getReference());
                    jw.jsonValue(mapping);
                }
                jw.endObject();
                jw.endObject();
            }
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
}
