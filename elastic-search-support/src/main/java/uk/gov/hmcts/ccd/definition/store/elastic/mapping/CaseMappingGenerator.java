package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.field.FieldMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Component
@Slf4j
public class CaseMappingGenerator extends AbstractMappingGenerator {

    public String generate(CaseTypeEntity caseType) {
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
        log.info("generating case properties mappings");
        config.getCaseMappings().forEach(Unchecked.biConsumer((property, mapping) -> {
            jw.name(property);
            jw.jsonValue(mapping);
            log.info("property: {}, mapping: {}", property, mapping);
        }));
    }

    private void dataMapping(JsonWriter jw, CaseTypeEntity caseType) throws IOException {
        //TODO handle case with no properties
        log.info("generating case data mappings");
        jw.name("data");
        dataAndClassificationMapping(jw, caseType, fieldMappingGenerator -> field -> fieldMappingGenerator.dataMapping(field));
    }

    private void dataClassificationMapping(JsonWriter jw, CaseTypeEntity caseType) throws IOException {
        //TODO handle case with no properties
        log.info("generating case data classification mappings");
        jw.name("data_classification");
        dataAndClassificationMapping(jw, caseType, fieldMappingGenerator -> field -> fieldMappingGenerator.dataClassificationMapping(field));
    }

    private void dataAndClassificationMapping(JsonWriter jw, CaseTypeEntity caseType, Function<FieldMappingGenerator, Function<CaseFieldEntity, String>> gen) throws IOException {
        //TODO handle case with no properties
        jw.beginObject();
        jw.name("properties");
        jw.beginObject();
        List<CaseFieldEntity> fields = caseType.getCaseFields().stream().filter(f -> !shouldIgnore(f)).collect(toList());
        for (CaseFieldEntity f : fields) {
            String property = f.getReference();
            String mapping = gen.apply(getMapperForType(f.getBaseTypeString())).apply(f);
            jw.name(property);
            jw.jsonValue(mapping);
            log.info("property: {}, mapping: {}", property, mapping);
        }
        jw.endObject();
        jw.endObject();
    }

    private boolean shouldIgnore(CaseFieldEntity caseFieldEntity) {
        boolean ignored = config.getCcdIgnoredTypes().contains(caseFieldEntity.getFieldType().getReference());
        if (ignored) {
            log.info("field {} of type {} ignored", caseFieldEntity.getReference(), caseFieldEntity.getBaseTypeString());
        }
        return ignored;
    }
}
