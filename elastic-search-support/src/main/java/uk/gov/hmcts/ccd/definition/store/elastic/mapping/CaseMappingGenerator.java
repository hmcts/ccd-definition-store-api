package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.TypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Component
@Slf4j
public class CaseMappingGenerator extends MappingGenerator {

    public String generateMapping(CaseTypeEntity caseType) {
        log.debug("creating mapping for case type: {}", caseType.getReference());

        String mapping = newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("dynamic");
            jw.value(config.getDynamic());
            jw.name("properties");
            jw.beginObject();
                propertiesMapping(jw);
                dataMapping(jw, caseType);
                dataClassificationMapping(jw, caseType);
            jw.endObject();
        }));

        log.debug("generated mapping for case type {}: {}", caseType.getReference(), mapping);
        return mapping;
    }

    private void propertiesMapping(JsonWriter jw) {
        log.debug("generating case properties mapping");
        config.getCasePredefinedMappings().forEach(Unchecked.biConsumer((property, mapping) -> {
            jw.name(property);
            jw.jsonValue(mapping);
            log.debug("property: {}, mapping: {}", property, mapping);
        }));
    }

    private void dataMapping(JsonWriter jw, CaseTypeEntity caseType) throws IOException {
        log.debug("generating case data mapping");
        jw.name("data");
        genericDataMapping(jw, caseType, typeMapper -> field -> typeMapper.dataMapping(field));
    }

    private void dataClassificationMapping(JsonWriter jw, CaseTypeEntity caseType) throws IOException {
        log.debug("generating case data classification mapping");
        jw.name("data_classification");
        genericDataMapping(jw, caseType, typeMapper -> field -> typeMapper.dataClassificationMapping(field));
    }

    private void genericDataMapping(JsonWriter jw, CaseTypeEntity caseType,
                                    Function<TypeMappingGenerator, Function<CaseFieldEntity, String>> typeMappingFunctionProducer) throws IOException {
        jw.beginObject();
            jw.name("properties");
            jw.beginObject();
                List<CaseFieldEntity> fields = caseType.getCaseFields().stream().filter(field -> !shouldIgnore(field)).collect(toList());
                for (CaseFieldEntity field : fields) {

                    String property = field.getReference();
                    TypeMappingGenerator typeMapper = getTypeMapper(field.getBaseTypeString());
                    Function<CaseFieldEntity, String> typeMappingFunction = typeMappingFunctionProducer.apply(typeMapper);
                    String mapping = typeMappingFunction.apply(field);

                    jw.name(property);
                    jw.jsonValue(mapping);
                    log.debug("property: {}, mapping: {}", property, mapping);
                }
            jw.endObject();
        jw.endObject();
    }

    private boolean shouldIgnore(CaseFieldEntity field) {
        boolean ignored = config.getCcdIgnoredTypes().contains(field.getFieldType().getReference());
        if (ignored) {
            log.debug("field {} of type {} ignored", field.getReference(), field.getBaseTypeString());
        }
        return ignored;
    }
}
