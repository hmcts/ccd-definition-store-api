package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

@RunWith(MockitoJUnitRunner.class)
public class CaseMappingGeneratorTest implements TestUtils {

    @Mock
    private CcdElasticSearchProperties config;

    @InjectMocks
    private CaseMappingGenerator mappingGenerator;

    @Test
    public void shouldWriteContentInANewJsonObject() {
        Map<String, String> predefinedMappings = new HashMap<>();
        predefinedMappings.put("testPropA", "valuePropA");
        predefinedMappings.put("testPropB", "valuePropB");
        Mockito.when(config.getCasePredefinedMappings()).thenReturn(predefinedMappings);
        CaseTypeEntity caseType = new CaseTypeBuilder().withJurisdiction("jur").withReference("caseTypeA").build();

        String result = mappingGenerator.generateMapping(caseType);

        assertThat(result, equalToJSONInFile(readFileFromClasspath("json/case_mapping_generator_test.json")));
    }
}