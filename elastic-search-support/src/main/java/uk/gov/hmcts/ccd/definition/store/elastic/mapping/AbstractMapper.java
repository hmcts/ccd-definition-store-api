package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.definition.store.elastic.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.Injectable;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.JsonGenerator;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.TypeMappersManager;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.TypeMappingGenerator;

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
