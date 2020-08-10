package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@Component
@Slf4j
public class ComplexTypeMappingGenerator extends TypeMappingGenerator {

    @Override
    public String dataMapping(FieldEntity field) {
        return conditionalMapping(field, () -> dataMapping(complexFields(field)));
    }

    public String dataMapping(List<ComplexFieldEntity> complexFields) {
        List<FieldEntity> fields = complexFields.stream().filter(field -> !shouldIgnore(field)).collect(toList());

        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name(PROPERTIES);
            jw.beginObject();
            for (FieldEntity field : fields) {
                String property = field.getReference();
                jw.name(property);
                String mapping = conditionalMapping(field, () -> {
                    TypeMappingGenerator typeMapper = getTypeMapper(field.getBaseTypeString());
                    return typeMapper.dataMapping(field);
                });
                jw.jsonValue(mapping);
                log.info("property: {}, mapping: {}", property, mapping);
            }
            jw.endObject();
        }));
    }

    @Override
    public String dataClassificationMapping(FieldEntity field) {
        return conditionalMapping(field, () -> dataClassificationMapping(complexFields(field)));
    }

    public String dataClassificationMapping(List<ComplexFieldEntity> complexFields) {
        List<FieldEntity> fields = complexFields.stream().filter(field -> !shouldIgnore(field)).collect(toList());

        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name(PROPERTIES);
            jw.beginObject();
            jw.name(CLASSIFICATION);
            jw.jsonValue(securityClassificationMapping());
            jw.name(VALUE);
            jw.beginObject();
            jw.name(PROPERTIES);
            jw.beginObject();
            for (FieldEntity field : fields) {
                String property = field.getReference();
                jw.name(property);
                String mapping = conditionalMapping(field, () -> {
                        TypeMappingGenerator typeMapper = getTypeMapper(field.getBaseTypeString());
                        return typeMapper.dataClassificationMapping(field);
                });
                jw.jsonValue(mapping);
                log.info("property: {}, mapping: {}", property, mapping);
            }
            jw.endObject();
            jw.endObject();
            jw.endObject();
        }));
    }

    @Override
    public List<String> getMappedTypes() {
        return newArrayList(COMPLEX);
    }

    private List<ComplexFieldEntity> complexFields(FieldEntity field) {
        return field.getFieldType().getComplexFields();
    }
}
