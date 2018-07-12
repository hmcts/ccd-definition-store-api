package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.StubTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

@RunWith(MockitoJUnitRunner.class)
public class TypeMappingGeneratorTest extends AbstractMapperTest {

    private TestMappingGenerator typeMappingGenerator = new TestMappingGenerator();

    @Before
    public void setup() {
        addMappingGenerator(new StubTypeMappingGenerator("Text", "dataMapping","dataClassificationMapping"));
        typeMappingGenerator.inject(stubTypeMappersManager);
    }

    @Test
    public void shouldThrowErrorWhenNoMapperForType() {
        assertThrows(RuntimeException.class, () -> typeMappingGenerator.getTypeMapper("unkonwnType"));
    }

    @Test
    public void shouldThrowErrorWhenNoConfiguredMapping() {
        assertThrows(RuntimeException.class, () -> typeMappingGenerator.getConfiguredMapping("Unknown"));
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
        public List<String> getCcdTypes() {
            return null;
        }
    }
}