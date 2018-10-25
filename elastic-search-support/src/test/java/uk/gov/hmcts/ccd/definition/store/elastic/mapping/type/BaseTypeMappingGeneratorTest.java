package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BaseTypeMappingGeneratorTest extends AbstractMapperTest {

    @InjectMocks
    private BaseTypeMappingGenerator typeMappingGenerator;

    private CaseFieldEntity field = newTextField("fieldA").build();

    @BeforeEach
    public void setUp() {
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
