package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

@RunWith(MockitoJUnitRunner.class)
public class CaseMappingGeneratorTest extends AbstractMapperTest implements TestUtils {

    @InjectMocks
    private CaseMappingGenerator mappingGenerator;

    private CaseTypeBuilder caseType = new CaseTypeBuilder().withJurisdiction("jur").withReference("caseTypeA");

    @Before
    public void setup() {
        super.setup();
        when(config.getDynamic()).thenReturn("dynamicConfig");
        stubMappingGenerator("Text", "dataMapping","dataClassificationMapping");
        mappingGenerator.inject(stubTypeMappersManager);
    }

    @Test
    public void shouldCrateMappingForPredefinedProperties() {
        predefinedMappings.put("testPropA", "valuePropA");
        predefinedMappings.put("testPropB", "valuePropB");

        String result = mappingGenerator.generateMapping(caseType.build());

        assertThat(result, equalToJSONInFile(
                readFileFromClasspath("json/case_mapping_generator_predefined_properties.json")));
    }

    @Test
    public void shouldCrateMappingForDataAndDataClassification() {
        CaseFieldEntity fieldA = new CaseFieldBuilder().withReference("fieldA").withFieldTypeReference("Text").buildBaseType();
        CaseFieldEntity fieldB = new CaseFieldBuilder().withReference("fieldB").withFieldTypeReference("Text").buildBaseType();
        caseType.withField(fieldA).withField(fieldB);

        String result = mappingGenerator.generateMapping(caseType.build());

        assertThat(result, equalToJSONInFile(readFileFromClasspath("json/case_mapping_generator_data.json")));
    }
}