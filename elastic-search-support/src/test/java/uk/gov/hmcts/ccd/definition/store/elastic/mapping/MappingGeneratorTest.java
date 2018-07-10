package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;

@RunWith(MockitoJUnitRunner.class)
public class MappingGeneratorTest extends AbstractMapperTest {

    @Mock
    private CcdElasticSearchProperties config;

    private TestMappingGenerator mappingGenerator = new TestMappingGenerator();

    @Before
    public void setup() {
        stubMappingGenerator("Text", "dataMapping","dataClassificationMapping");
        mappingGenerator.inject(stubTypeMappersManager);
    }

    @Test
    public void shouldThrowErrorWhenNoMapperForType() {
        assertThrows(RuntimeException.class, () -> mappingGenerator.getTypeMapper("unkonwnType"));
    }

    private static class TestMappingGenerator extends MappingGenerator {}
}