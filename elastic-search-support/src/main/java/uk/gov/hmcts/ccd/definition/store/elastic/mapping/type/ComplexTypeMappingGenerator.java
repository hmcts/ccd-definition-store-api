package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.List;
import java.util.Set;

import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

@Component
@Slf4j
public class ComplexTypeMappingGenerator extends TypeMappingGenerator {

    @Autowired
    public ComplexTypeMappingGenerator(CcdElasticSearchProperties config) {
        super(config);
    }

    @Override
    public String dataMapping(FieldEntity field) {
        return dataMapping(complexFields(field));
    }

    public String dataMapping(Set<ComplexFieldEntity> complexFields) {
        List<FieldEntity> fields = complexFields.stream().filter(field -> !shouldIgnore(field)).collect(toList());

        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name(PROPERTIES);
            jw.beginObject();
            for (FieldEntity field : fields) {
                String property = field.getReference();
                jw.name(property);
                TypeMappingGenerator typeMapper = getTypeMapper(field.getBaseTypeString());
                String mapping = typeMapper.doDataMapping(field);
                jw.jsonValue(mapping);
                log.info("property: {}, mapping: {}", property, mapping);
            }
            jw.endObject();
        }));
    }

    @Override
    public String dataClassificationMapping(FieldEntity field) {
        return dataClassificationMapping(complexFields(field));
    }

    public String dataClassificationMapping(Set<ComplexFieldEntity> complexFields) {
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
                TypeMappingGenerator typeMapper = getTypeMapper(field.getBaseTypeString());
                String mapping = typeMapper.doDataClassificationMapping(field);
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

    private Set<ComplexFieldEntity> complexFields(FieldEntity field) {
        return field.getFieldType().getComplexFields();
    }
}
