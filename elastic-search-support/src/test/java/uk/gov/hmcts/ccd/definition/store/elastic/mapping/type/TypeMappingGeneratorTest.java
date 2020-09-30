package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.StubTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TypeMappingGeneratorTest extends AbstractMapperTest {

    private TestMappingGenerator typeMappingGenerator = new TestMappingGenerator();
    private CaseFieldEntity field = newTextField("fieldA").build();

    @BeforeEach
    public void setUp() {
        addMappingGenerator(new StubTypeMappingGenerator(
            "Text", "dataMapping", "dataClassificationMapping"));
        typeMappingGenerator.inject(stubTypeMappersManager);
    }

    @Test
    public void shouldThrowErrorWhenNoMapperForType() {
        assertThrows(ElasticSearchInitialisationException.class, () -> typeMappingGenerator
            .getTypeMapper("unkonwnType"));
    }

    @Test
    public void shouldThrowErrorWhenNoConfiguredMapping() {
        assertThrows(ElasticSearchInitialisationException.class, () -> typeMappingGenerator
            .getConfiguredMapping("Unknown"));
    }

    @Test
    public void shouldReturnDataMappingWhenFieldIsSearchable() {
        field.setSearchable(true);

        String result = typeMappingGenerator.doDataMapping(field);

        assertThat(result, equalTo("dataMapping"));
    }

    @Test
    public void shouldReturnDisabledDataMappingWhenFieldIsNotSearchable() {
        field.setSearchable(false);

        String result = typeMappingGenerator.doDataMapping(field);

        assertThat(result, equalTo("disabledMapping"));
    }

    @Test
    public void shouldReturnDataClassificationMappingWhenFieldIsSearchable() {
        field.setSearchable(true);

        String result = typeMappingGenerator.doDataClassificationMapping(field);

        assertThat(result, equalTo("dataClassificationMapping"));
    }

    @Test
    public void shouldReturnDisabledDataClassificationMappingWhenFieldIsNotSearchable() {
        field.setSearchable(false);

        String result = typeMappingGenerator.doDataClassificationMapping(field);

        assertThat(result, equalTo("disabledMapping"));
    }

    private static class TestMappingGenerator extends TypeMappingGenerator {
        @Override
        public String dataMapping(FieldEntity field) {
            return "dataMapping";
        }

        @Override
        public String dataClassificationMapping(FieldEntity field) {
            return "dataClassificationMapping";
        }

        @Override
        public List<String> getMappedTypes() {
            return null;
        }

        @Override
        protected Map<String, String> configuredTypeMappings() {
            return Collections.EMPTY_MAP;
        }

        @Override
        protected String disabled() {
            return "disabledMapping";
        }
    }
}
