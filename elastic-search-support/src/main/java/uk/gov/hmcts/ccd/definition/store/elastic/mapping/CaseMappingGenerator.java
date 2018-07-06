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
public class CaseMappingGenerator extends AbstractMappingGenerator {

    public String generateMapping(CaseTypeEntity caseType) {
        log.info("creating mapping for case type: {}", caseType.getReference());

        String mapping = newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("properties");
            jw.beginObject();
                propertiesMapping(jw);
                dataMapping(jw, caseType);
                dataClassificationMapping(jw, caseType);
            jw.endObject();
        }));

        log.info("generated mapping for case type {}: {}", caseType.getReference(), mapping);
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
        //TODO handle case with no properties
        log.info("generating case data mapping");
        jw.name("data");
        genericDataMapping(jw, caseType, typeMapper -> field -> typeMapper.dataMapping(field));
    }

    private void dataClassificationMapping(JsonWriter jw, CaseTypeEntity caseType) throws IOException {
        //TODO handle case with no properties
        log.info("generating case data classification mapping");
        jw.name("data_classification");
        genericDataMapping(jw, caseType, typeMapper -> field -> typeMapper.dataClassificationMapping(field));
    }

    private void genericDataMapping(JsonWriter jw, CaseTypeEntity caseType,
                                    Function<TypeMappingGenerator, Function<CaseFieldEntity, String>> mappingProducer) throws IOException {
        //TODO handle case with no properties
        jw.beginObject();
        jw.name("properties");
        jw.beginObject();
        List<CaseFieldEntity> fields = caseType.getCaseFields().stream().filter(f -> !shouldIgnore(f)).collect(toList());
        for (CaseFieldEntity field : fields) {
            String property = field.getReference();
            Function<CaseFieldEntity, String> typeMapper = mappingProducer.apply(getTypeMapper(field.getBaseTypeString()));
            String mapping = typeMapper.apply(field);
            jw.name(property);
            jw.jsonValue(mapping);
            log.info("property: {}, mapping: {}", property, mapping);
        }
        jw.endObject();
        jw.endObject();
    }

    private boolean shouldIgnore(CaseFieldEntity field) {
        boolean ignored = config.getCcdIgnoredTypes().contains(field.getFieldType().getReference());
        if (ignored) {
            log.info("field {} of type {} ignored", field.getReference(), field.getBaseTypeString());
        }
        return ignored;
    }
}
