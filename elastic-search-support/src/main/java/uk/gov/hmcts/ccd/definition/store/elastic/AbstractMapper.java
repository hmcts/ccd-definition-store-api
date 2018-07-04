package uk.gov.hmcts.ccd.definition.store.elastic;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractMapper implements JsonGenerator {

    protected Map<String, TypeMappingGenerator> typeMappers;

    @Autowired
    protected CcdElasticSearchProperties config;

//    public void setTypeMappers(List<TypeMappingGenerator> mappingGenerators) {
//        for (TypeMappingGenerator mg : mappingGenerators) {
//            for (String type : mg.getTypes()) {
//                typeMappers.put(type, mg);
//            }
//        }
//    }

//    protected String getMapperForType(String type) throws IOException {
//        return getMapperForType(caseFieldEntity.getBaseTypeString()).generateMapping(caseFieldEntity);

//        else if (caseFieldEntity.isCollection()) {
//
//        } else {
//            String ccdType = caseFieldEntity.getBaseTypeString();
//            String configuredMapping = config.getTypeMappings().get(ccdType);
//            if (configuredMapping == null) {
//                throw new RuntimeException(String.format("unknown mapping for ccd type %s", ccdType));
//            }
//            result = configuredMapping;
//        }
//        return result;
//    }

    protected TypeMappingGenerator getMapperForType(String type) {
        return this.typeMappers.get(type);
    }

}
