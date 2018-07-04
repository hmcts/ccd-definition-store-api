package uk.gov.hmcts.ccd.definition.store.elastic;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractMapper implements JsonGenerator, Injectable {

    @Autowired
    protected CcdElasticSearchProperties config;

    protected Map<String, TypeMappingGenerator> typeMappers;

    public void inject(TypeMappersManager typeMappersManager){
        this.typeMappers = typeMappersManager.getTypeMappers();
    }

    protected TypeMappingGenerator getMapperForType(String type) {
        return this.typeMappers.get(type);
    }
}
