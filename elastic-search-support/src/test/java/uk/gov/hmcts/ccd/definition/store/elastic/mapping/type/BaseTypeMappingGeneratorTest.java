package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BaseTypeMappingGeneratorTest extends AbstractMapperTest {

    private static final String TEXT_MAPPING = "textMapping";

    @InjectMocks
    private BaseTypeMappingGenerator typeMappingGenerator;

    private CaseFieldEntity field = newTextField("fieldA").build();

    @BeforeEach
    void setUp() {
        super.setup();

        typeMappings.put("Text", TEXT_MAPPING);
    }

    @Test
    void shouldReturnConfiguredMapping() {
        String result = typeMappingGenerator.doDataMapping(field);

        assertThat(result, equalTo(TEXT_MAPPING));
    }

    @Test
    void shouldReturnSecurityClassificationMapping() {
        when(config.getSecurityClassificationMapping()).thenReturn("someMapping");

        String result = typeMappingGenerator.doDataClassificationMapping(field);

        assertThat(result, equalTo("someMapping"));
    }

    @Test
    void shouldReturnTypeMappingWhenFieldIsSearchable() {
        field.setSearchable(true);

        String result = typeMappingGenerator.doDataMapping(field);

        assertThat(result, equalTo(TEXT_MAPPING));
    }

    @Test
    void shouldReturnDisabledDataMappingWhenFieldIsNonSearchable() {
        field.setSearchable(false);

        String result = typeMappingGenerator.doDataMapping(field);

        assertThat(result, equalTo(disabledMapping));
    }

    @Test
    void shouldReturnDisabledDataClassificationMappingWhenFieldIsNonSearchable() {
        field.setSearchable(false);

        String result = typeMappingGenerator.doDataClassificationMapping(field);

        assertThat(result, equalTo(disabledMapping));
    }
}
