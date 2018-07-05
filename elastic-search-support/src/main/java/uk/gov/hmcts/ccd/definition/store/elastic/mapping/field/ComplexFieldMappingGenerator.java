package uk.gov.hmcts.ccd.definition.store.elastic.mapping.field;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
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
    public String generateMapping(FieldEntity fieldEntity) throws IOException {
        List<ComplexFieldEntity> complexFields = fieldEntity.getFieldType().getComplexFields();
        return generateMapping(complexFields);
    }

    public String generateMapping(List<ComplexFieldEntity> complexFields) throws IOException {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("properties");
            jw.beginObject();
            for (FieldEntity f : complexFields) {
                jw.name(f.getReference());
                jw.jsonValue(getMapperForType(f.getBaseTypeString()).generateMapping(f));
            }
            jw.endObject();
        }));
    }

    @Override
    public List<String> getCcdTypes() {
        return newArrayList("Complex");
    }
}
