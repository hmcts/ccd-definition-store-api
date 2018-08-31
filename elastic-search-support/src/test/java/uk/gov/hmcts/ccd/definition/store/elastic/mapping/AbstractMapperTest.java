package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static com.google.common.collect.Maps.newHashMap;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.TypeMappersManager;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.TypeMappingGenerator;

public class AbstractMapperTest {

    @Mock
    protected CcdElasticSearchProperties config;

    protected TypeMappersManager stubTypeMappersManager = new TypeMappersManager();
    protected Map<String, String> typeMappings = newHashMap();
    protected Map<String, String> elasticMappings = newHashMap();
    protected Map<String, String> predefinedMappings = new HashMap<>();

    protected void setup() {
        when(config.getTypeMappings()).thenReturn(typeMappings);
        when(config.getElasticMappings()).thenReturn(elasticMappings);
        when(config.getCasePredefinedMappings()).thenReturn(predefinedMappings);
    }

    protected void addMappingGenerator(TypeMappingGenerator stubTypeMappingGenerator) {
        stubTypeMappersManager.getTypeMappers().put(stubTypeMappingGenerator.getMappedTypes().get(0), stubTypeMappingGenerator);
    }
}
