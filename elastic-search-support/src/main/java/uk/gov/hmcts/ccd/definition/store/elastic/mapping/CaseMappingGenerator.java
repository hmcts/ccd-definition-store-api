package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.TypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Component
@Slf4j
public class CaseMappingGenerator extends MappingGenerator {

    public String generateMapping(CaseTypeEntity caseType) {
        log.info("creating mapping for case type: {}", caseType.getReference());

        String mapping = newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("dynamic");
            jw.value(config.getDynamic());
            jw.name(PROPERTIES);
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
        log.info("generating case properties mapping");
        config.getCasePredefinedMappings().forEach(Unchecked.biConsumer((property, mapping) -> {
            jw.name(property);
            jw.jsonValue(mapping);
            log.info("property: {}, mapping: {}", property, mapping);
        }));
    }

    private void dataMapping(JsonWriter jw, CaseTypeEntity caseType) throws IOException {
        log.info("generating case data mapping");
        jw.name(DATA);
        genericDataMapping(jw, caseType, typeMapper -> typeMapper::dataMapping);
    }

    private void dataClassificationMapping(JsonWriter jw, CaseTypeEntity caseType) throws IOException {
        log.info("generating case data classification mapping");
        jw.name(DATA_CLASSIFICATION);
        genericDataMapping(jw, caseType, typeMapper -> typeMapper::dataClassificationMapping);
    }

    private void genericDataMapping(JsonWriter jw, CaseTypeEntity caseType,
                                    Function<TypeMappingGenerator, Function<CaseFieldEntity, String>> mappingMethodSelection) throws IOException {
        jw.beginObject();
        jw.name(PROPERTIES);
        jw.beginObject();
        List<CaseFieldEntity> fields = caseType.getCaseFields().stream().filter(field -> !shouldIgnore(field)).collect(toList());
        for (CaseFieldEntity field : fields) {
            String property = field.getReference();
            TypeMappingGenerator typeMapper = getTypeMapper(field.getBaseTypeString());
            Function<CaseFieldEntity, String> mappingMethod = mappingMethodSelection.apply(typeMapper);
            String mapping = mappingMethod.apply(field);

            jw.name(property);
            jw.jsonValue(mapping);
            log.info("property: {}, mapping: {}", property, mapping);
        }
        jw.endObject();
        jw.endObject();
    }
}
