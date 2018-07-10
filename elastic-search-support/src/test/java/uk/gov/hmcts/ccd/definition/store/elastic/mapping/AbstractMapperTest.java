package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import static com.google.common.collect.Maps.newHashMap;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.mockito.Mock;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.TypeMappersManager;

public class AbstractMapperTest {

    @Mock
    protected CcdElasticSearchProperties config;

    protected TypeMappersManager stubTypeMappersManager = new TypeMappersManager();
    protected Map<String, String> typeMappings = newHashMap();
    protected Map<String, String> elasticMappings = newHashMap();


    protected void setup() {
        when(config.getTypeMappings()).thenReturn(typeMappings);
        when(config.getElasticMappings()).thenReturn(elasticMappings);
    }

    protected void stubMappingGenerator(String type, String dataMapping, String dataClassificationMapping) {
        StubTypeMappingGenerator stubTypeMappingGenerator = new StubTypeMappingGenerator(type,
                dataMapping,dataClassificationMapping);
        stubTypeMappersManager.getTypeMappers().put(type, stubTypeMappingGenerator);
    }


}
