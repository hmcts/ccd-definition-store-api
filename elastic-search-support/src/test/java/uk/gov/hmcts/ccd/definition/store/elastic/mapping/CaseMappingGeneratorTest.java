package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;

@RunWith(MockitoJUnitRunner.class)
public class CaseMappingGeneratorTest extends AbstractMapperTest implements TestUtils {

    @InjectMocks
    private CaseMappingGenerator mappingGenerator;

    private CaseTypeBuilder caseType = new CaseTypeBuilder().withJurisdiction("jur").withReference("caseTypeA");

    @Before
    public void setup() {
        super.setup();
        when(config.getDynamic()).thenReturn("dynamicConfig");
        addMappingGenerator(new StubTypeMappingGenerator("Text", "dataMapping","dataClassificationMapping"));
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
        CaseFieldEntity fieldA = newTextField("fieldA").build();
        CaseFieldEntity fieldB = newTextField("fieldB").build();
        caseType.withField(fieldA).withField(fieldB);

        String result = mappingGenerator.generateMapping(caseType.build());

        assertThat(result, equalToJSONInFile(readFileFromClasspath("json/case_mapping_generator_data.json")));
    }
}