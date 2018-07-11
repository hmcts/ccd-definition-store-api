package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ConfiguredTypeMappingGeneratorTest extends AbstractMapperTest {

    @InjectMocks
    private ConfiguredTypeMappingGenerator typeMappingGenerator;

    private CaseFieldEntity field = new CaseFieldBuilder().withReference("fieldA").withFieldTypeReference("Text").buildBaseType();

    @Before
    public void setup() {
        super.setup();

        typeMappings.put("Text", "textMapping");
    }

    @Test
    public void shouldReturnConfiguredMapping() {
        String result = typeMappingGenerator.dataMapping(field);

        assertThat(result, equalTo("textMapping"));
    }

    @Test
    public void shouldReturnSecurityClassificationMapping() {
        when(config.getSecurityClassificationMapping()).thenReturn("someMapping");

        String result = typeMappingGenerator.dataClassificationMapping(field);

        assertThat(result, equalTo("someMapping"));
    }
}