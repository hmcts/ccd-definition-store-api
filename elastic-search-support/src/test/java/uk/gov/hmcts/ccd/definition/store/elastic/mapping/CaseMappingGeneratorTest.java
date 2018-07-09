package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.TypeMappersManager;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.TypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldEntityBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

@RunWith(MockitoJUnitRunner.class)
public class CaseMappingGeneratorTest implements TestUtils {

    @Mock
    private CcdElasticSearchProperties config;

    @InjectMocks
    private CaseMappingGenerator mappingGenerator;

    @Before
    public void setup() {
        TypeMappersManager typeMappersManager = new TypeMappersManager();
        typeMappersManager.setTypeMappers(
                newArrayList(stubTypeMappingGenerator("Text", "dataMapping",
                        "dataClassificationMapping")));
        mappingGenerator.inject(typeMappersManager);
    }

    CaseTypeBuilder caseType = new CaseTypeBuilder().withJurisdiction("jur").withReference("caseTypeA");

    @Test
    public void shouldCrateMappingForPredefinedProperties() {
        Map<String, String> predefinedMappings = new HashMap<>();
        predefinedMappings.put("testPropA", "valuePropA");
        predefinedMappings.put("testPropB", "valuePropB");
        when(config.getCasePredefinedMappings()).thenReturn(predefinedMappings);

        String result = mappingGenerator.generateMapping(caseType.build());

        assertThat(result, equalToJSONInFile(
                readFileFromClasspath("json/case_mapping_generator_predefined_properties.json")));
    }

    @Test
    public void shouldCrateMappingForDataAndDataClassification() {
        when(config.getCasePredefinedMappings()).thenReturn(Collections.emptyMap());

        CaseFieldEntity fieldA = new CaseFieldEntityBuilder().withReference("fieldA").withBaseType("Text").build();
        CaseFieldEntity fieldB = new CaseFieldEntityBuilder().withReference("fieldB").withBaseType("Text").build();
        caseType.withField(fieldA).withField(fieldB);

        String result = mappingGenerator.generateMapping(caseType.build());

        assertThat(result, equalToJSONInFile(readFileFromClasspath("json/case_mapping_generator_data.json")));
    }

    @Test
    public void shouldThrowErrorWhenNoMapperForType() {
        when(config.getCasePredefinedMappings()).thenReturn(Collections.emptyMap());

        CaseFieldEntity fieldA = new CaseFieldEntityBuilder().withReference("fieldA").withBaseType("TypeWithNoMapper").build();
        caseType.withField(fieldA);

        String result = mappingGenerator.generateMapping(caseType.build());

        assertThat(result, equalToJSONInFile(readFileFromClasspath("json/case_mapping_generator_data.json")));
    }

    private TypeMappingGenerator stubTypeMappingGenerator(String type, String dataMapping,
                                                          String dataClassificationMapping) {
        return new TypeMappingGenerator() {

            @Override
            public String dataMapping(FieldEntity field) {
                return dataMapping;
            }

            @Override
            public String dataClassificationMapping(FieldEntity field) {
                return dataClassificationMapping;
            }

            @Override
            public List<String> getCcdTypes() {
                return newArrayList(type);
            }
        };
    }
}