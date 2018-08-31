package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

@RunWith(MockitoJUnitRunner.class)
public class BaseTypeMappingGeneratorTest extends AbstractMapperTest {

    @InjectMocks
    private BaseTypeMappingGenerator typeMappingGenerator;

    private CaseFieldEntity field = newTextField("fieldA").build();

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