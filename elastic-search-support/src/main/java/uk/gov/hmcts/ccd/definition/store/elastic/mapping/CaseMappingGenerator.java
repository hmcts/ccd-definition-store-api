package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;

import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Component
@Slf4j
public class CaseMappingGenerator extends AbstractMapper {

    public String generate(CaseTypeEntity caseType) throws IOException {
        log.info("creating mapping for case type: {}", caseType.getReference());

        String caseMapping = newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("properties");
            jw.beginObject();
                for(FieldMapping mapping : casePropertiesMapping()) {
                    jw.name(mapping.getFieldName());
                    jw.jsonValue(mapping.getMapping());
                }
                //TODO handle case with no properties
                jw.name("data");
                jw.beginObject();
                    jw.name("properties");
                    jw.beginObject();
                        for(FieldMapping mapping : caseDataMapping(caseType)) {
                            jw.name(mapping.getFieldName());
                            jw.jsonValue(mapping.getMapping());
                        }
                    jw.endObject();
                jw.endObject();
            jw.endObject();
        }));

        log.info("generated mapping: {}", caseMapping);
        return caseMapping;
    }

    private List<FieldMapping> casePropertiesMapping() {
        return config.getCaseMappings().entrySet().stream().map(e ->
                new FieldMapping(e.getKey(), e.getValue()))
                .collect(toList());
    }

    private List<FieldMapping> caseDataMapping(CaseTypeEntity caseType) {
        List<CaseFieldEntity> fields = caseType.getCaseFields().stream().filter(f -> !shouldIgnore(f)).collect(toList());
        return fields.stream().map(Unchecked.function(f -> {
            String generatedMapping = getMapperForType(f.getBaseTypeString()).generateMapping(f);
            return new FieldMapping(f.getReference(), generatedMapping);
        })).collect(toList());
    }

    private boolean shouldIgnore(CaseFieldEntity caseFieldEntity) {
        return config.getTypeMappingsIgnored().contains(caseFieldEntity.getBaseTypeString());
    }
}
