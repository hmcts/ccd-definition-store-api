package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static com.google.common.collect.Maps.newHashMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ConfiguredTypeMappingGeneratorTest {

    @Mock
    private CcdElasticSearchProperties config;

    @InjectMocks
    private ConfiguredTypeMappingGenerator typeMappingGenerator;

    private CaseFieldEntity field = new CaseFieldBuilder().withReference("fieldA").withFieldTypeReference("Text").buildBaseType();

    @Before
    public void setup() {
        Map<String, String> typeMappings = newHashMap();
        typeMappings.put("Text", "textMapping");
        when(config.getTypeMappings()).thenReturn(typeMappings);
    }

    @Test
    public void sholdReturnConfiguredMapping() {
        String result = typeMappingGenerator.dataMapping(field);

        assertThat(result, equalTo("textMapping"));
    }

    @Test
    public void sholdReturnSecurityClassificationMapping() {
        when(config.getSecurityClassificationMapping()).thenReturn("someMapping");

        String result = typeMappingGenerator.dataClassificationMapping(field);

        assertThat(result, equalTo("someMapping"));
    }

    @Test
    public void shouldThrowErrorWhenNoConfiguredMapping() {

        CaseFieldEntity field = new CaseFieldBuilder().withReference("fieldA").withFieldTypeReference("Unknown").buildBaseType();

        assertThrows(RuntimeException.class, () -> typeMappingGenerator.dataMapping(field));
    }
}