package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;

import java.util.Collections;
import java.util.HashMap;
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
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldEntityBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

@RunWith(MockitoJUnitRunner.class)
public class CaseMappingGeneratorTest implements TestUtils {

    @Mock
    private CcdElasticSearchProperties config;

    @InjectMocks
    private CaseMappingGenerator mappingGenerator;

    private CaseTypeBuilder caseType = new CaseTypeBuilder().withJurisdiction("jur").withReference("caseTypeA");

    @Before
    public void setup() {
        TypeMappersManager typeMappersManager = new TypeMappersManager();
        StubTypeMappingGenerator stubTypeMappingGenerator = new StubTypeMappingGenerator("Text",
                "dataMapping","dataClassificationMapping");
        typeMappersManager.setTypeMappers(newArrayList(stubTypeMappingGenerator));
        mappingGenerator.inject(typeMappersManager);
    }

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
}