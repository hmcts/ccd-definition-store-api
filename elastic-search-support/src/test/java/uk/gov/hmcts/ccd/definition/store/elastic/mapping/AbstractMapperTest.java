package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import org.mockito.Mock;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.TypeMappersManager;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.TypeMappingGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.mockito.Mockito.when;

public abstract class AbstractMapperTest {

    @Mock
    protected CcdElasticSearchProperties config;

    protected TypeMappersManager stubTypeMappersManager = new TypeMappersManager(Set.of());
    protected Map<String, String> typeMappings = newHashMap();
    protected Map<String, String> elasticMappings = newHashMap();
    protected Map<String, String> predefinedMappings = new HashMap<>();
    protected List<String> ignoredTypes = newArrayList("Label");
    protected String disabledMapping = "{\"enabled\": false}";
    protected int batchSize = 100;

    protected void setup() {
        elasticMappings.put("disabled", disabledMapping);
        when(config.getTypeMappings()).thenReturn(typeMappings);
        when(config.getElasticMappings()).thenReturn(elasticMappings);
        when(config.getCasePredefinedMappings()).thenReturn(predefinedMappings);
        when(config.getCcdIgnoredTypes()).thenReturn(ignoredTypes);
        when(config.getBatchSize()).thenReturn(batchSize);
    }

    protected void addMappingGenerator(TypeMappingGenerator stubTypeMappingGenerator) {
        stubTypeMappersManager.getTypeMappers().put(
            stubTypeMappingGenerator.getMappedTypes().get(0), stubTypeMappingGenerator);
    }
}
