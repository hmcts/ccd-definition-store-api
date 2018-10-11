package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.StubTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TypeMappingGeneratorTest extends AbstractMapperTest {

    private TestMappingGenerator typeMappingGenerator = new TestMappingGenerator();

    @BeforeEach
    public void setUp() {
        addMappingGenerator(new StubTypeMappingGenerator("Text", "dataMapping","dataClassificationMapping"));
        typeMappingGenerator.inject(stubTypeMappersManager);
    }

    @Test
    public void shouldThrowErrorWhenNoMapperForType() {
        assertThrows(ElasticSearchInitialisationException.class, () -> typeMappingGenerator.getTypeMapper("unkonwnType"));
    }

    @Test
    public void shouldThrowErrorWhenNoConfiguredMapping() {
        assertThrows(ElasticSearchInitialisationException.class, () -> typeMappingGenerator.getConfiguredMapping("Unknown"));
    }

    private static class TestMappingGenerator extends TypeMappingGenerator {
        @Override
        public String dataMapping(FieldEntity field) {
            return null;
        }

        @Override
        public String dataClassificationMapping(FieldEntity field) {
            return null;
        }

        @Override
        public List<String> getMappedTypes() {
            return null;
        }

        @Override
        protected Map<String, String> configuredTypeMappings() {
            return Collections.EMPTY_MAP;
        }
    }
}
