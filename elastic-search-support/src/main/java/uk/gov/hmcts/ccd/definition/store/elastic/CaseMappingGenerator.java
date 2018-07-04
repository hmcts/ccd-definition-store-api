package uk.gov.hmcts.ccd.definition.store.elastic;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@Component
@Slf4j
public class CaseMappingGenerator extends AbstractElasticSearchSupport {

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
                        for(Entry<String, String> mapping : dataMappings(caseType).entrySet()) {
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

    private Map<String, String> dataMappings(CaseTypeEntity caseType) throws IOException {
        Map<String, String> result = newHashMap();
        for (CaseFieldEntity f : caseType.getCaseFields()) {
            if (!config.getTypeMappingsIgnored().contains(f.getBaseTypeString())) {
                result.put(f.getReference(), mapping(f));
            }
        }
        return result;
    }

    private Map<String, String> propertiesMapping() {
        Map<String, String> result = newHashMap();
        for(Entry<String, String> mapping : config.getCaseMappings().entrySet()) {
            String name = mapping.getKey();
            String elasticConfig = mapping.getValue();
            result.put(name, elasticConfig);
        }
        return result;
    }

    private String mapping(FieldEntity caseFieldEntity) throws IOException {
        String result = null;
        if (caseFieldEntity.isComplex()) {
            result = mappingForComplexField(caseFieldEntity.getFieldType().getComplexFields());
        } else if (caseFieldEntity.isCollection()) {
            if (caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields() != null) {
                result = mappingForComplexField(caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields());
            } else {
                result = null;
            }
        } else {
            String ccdType = caseFieldEntity.getBaseTypeString();
            String configuredMapping = config.getTypeMappings().get(ccdType);
            if (configuredMapping == null) {
                throw new RuntimeException(String.format("unknown mapping for ccd type %s", ccdType));
            }
            result = configuredMapping;
        }
        return result;
    }

    private String mappingForComplexField(List<ComplexFieldEntity> fields) throws IOException {
        return newJson(Unchecked.consumer((JsonWriter jw) -> {
            jw.name("properties");
            jw.beginObject();
                for (ComplexFieldEntity f : fields) {
                    jw.name(f.getReference());
                    jw.jsonValue(mapping(f));
                }
            jw.endObject();
        }));
    }

    /**
     * returns any content written by the consumer within curly brackets
     */
    private String newJson(Consumer<JsonWriter> jsonWriterConsumer) throws IOException {
        StringWriter out = new StringWriter();
        JsonWriter jw = new JsonWriter(out);
        jw.beginObject();

        jsonWriterConsumer.accept(jw);

        jw.endObject();
        return out.toString();
    }
}
