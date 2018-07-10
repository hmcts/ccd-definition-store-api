package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.TypeMappersManager;

public class AbstractMapperTest {

    protected TypeMappersManager stubTypeMappersManager = new TypeMappersManager();

    protected void stubMappingGenerator(String type, String dataMapping, String dataClassificationMapping) {
        StubTypeMappingGenerator stubTypeMappingGenerator = new StubTypeMappingGenerator(type,
                dataMapping,dataClassificationMapping);
        stubTypeMappersManager.getTypeMappers().put(type, stubTypeMappingGenerator);
    }
}
