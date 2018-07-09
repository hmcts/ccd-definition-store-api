package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.TypeMappersManager;

@RunWith(MockitoJUnitRunner.class)
public class MappingGeneratorTest {

    @Mock
    private CcdElasticSearchProperties config;

    private TestMappingGenerator mappingGenerator = new TestMappingGenerator();

    @Before
    public void setup() {
        TypeMappersManager typeMappersManager = new TypeMappersManager();
        StubTypeMappingGenerator stubTypeMappingGenerator = new StubTypeMappingGenerator("Text",
                "dataMapping","dataClassificationMapping");
        typeMappersManager.setTypeMappers(newArrayList(stubTypeMappingGenerator));
        mappingGenerator.inject(typeMappersManager);
    }

    @Test
    public void shouldThrowErrorWhenNoMapperForType() {
        assertThrows(RuntimeException.class, () -> {
            mappingGenerator.getTypeMapper("unkonwnType");
        });
    }

    private static class TestMappingGenerator extends MappingGenerator {}
}