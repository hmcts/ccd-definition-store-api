package uk.gov.hmcts.ccd.definition.store.elastic.mapping.field;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.google.gson.stream.JsonWriter;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@Component
public class ComplexFieldMappingGenerator extends AbstractMappingGenerator implements FieldMappingGenerator {

    @Override
    public String dataMapping(FieldEntity fieldEntity) {
        return dataMapping(complexFields(fieldEntity));
    }

    public String dataMapping(List<ComplexFieldEntity> complexFields) {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("properties");
            jw.beginObject();
            for (FieldEntity f : complexFields) {
                jw.name(f.getReference());
                jw.jsonValue(getMapperForType(f.getBaseTypeString()).dataMapping(f));
            }
            jw.endObject();
        }));
    }

    @Override
    public String dataClassificationMapping(FieldEntity fieldEntity) {
        return dataClassificationMapping(complexFields(fieldEntity));
    }

    public String dataClassificationMapping(List<ComplexFieldEntity> complexFields) {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("properties");
            jw.beginObject();
            jw.name("classification");
            jw.jsonValue(keyword());
            jw.name("value");
            jw.beginObject();
            jw.name("properties");
            jw.beginObject();
            for (FieldEntity f : complexFields) {
                jw.name(f.getReference());
                jw.jsonValue(keyword());
            }
            jw.endObject();
            jw.endObject();
            jw.endObject();
        }));
    }

    @Override
    public List<String> getCcdTypes() {
        return newArrayList("Complex");
    }

    private List<ComplexFieldEntity> complexFields(FieldEntity fieldEntity) {
        return fieldEntity.getFieldType().getComplexFields();
    }
}
