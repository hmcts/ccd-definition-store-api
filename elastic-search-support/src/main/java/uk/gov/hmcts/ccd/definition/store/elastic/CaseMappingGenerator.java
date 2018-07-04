package uk.gov.hmcts.ccd.definition.store.elastic;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

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
                for(Entry<String, String> mapping : propertiesMapping().entrySet()) {
                    jw.name(mapping.getKey());
                    jw.jsonValue(mapping.getValue());
                }
                //TODO handle case with no properties
                jw.name("data");
                jw.beginObject();
                    jw.name("properties");
                    jw.beginObject();
                        for(Entry<String, String> mapping : dataMapping(caseType).entrySet()) {
                            jw.name(mapping.getKey());
                            jw.jsonValue(mapping.getValue());
                        }
                    jw.endObject();
                jw.endObject();
            jw.endObject();
        }));

        log.info("generated mapping: {}", caseMapping);
        return caseMapping;
    }

    private Map<String, String> propertiesMapping() {
        Map<String, String> result = newHashMap();
        for(Entry<String, String> mapping : config.getCaseMappings().entrySet()) {
            String name = mapping.getKey();
            String elasticMapping = mapping.getValue();
            result.put(name, elasticMapping);
        }
        return result;
    }

    private Map<String, String> dataMapping(CaseTypeEntity caseType) throws IOException {
        Map<String, String> result = newHashMap();
        for (CaseFieldEntity f : caseType.getCaseFields()) {
            if (!config.getTypeMappingsIgnored().contains(f.getBaseTypeString())) {
                String generatedMapping = getMapperForType(f.getBaseTypeString()).generateMapping(f);
                result.put(f.getReference(), generatedMapping);
            }
        }
        return result;
    }
}
