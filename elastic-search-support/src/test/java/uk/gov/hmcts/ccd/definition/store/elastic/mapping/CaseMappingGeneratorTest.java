package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CaseMappingGeneratorTest extends AbstractMapperTest implements TestUtils {

    @InjectMocks
    private CaseMappingGenerator mappingGenerator;

    private CaseTypeBuilder caseType = new CaseTypeBuilder().withJurisdiction("jur").withReference("caseTypeA");

    @BeforeEach
    public void setUp() {
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