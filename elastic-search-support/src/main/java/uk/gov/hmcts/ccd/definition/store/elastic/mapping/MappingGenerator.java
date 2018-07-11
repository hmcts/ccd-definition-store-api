package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.JsonGenerator;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.Injectable;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.TypeMappersManager;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.TypeMappingGenerator;

public abstract class MappingGenerator implements JsonGenerator, Injectable {

    @Autowired
    protected CcdElasticSearchProperties config;

    protected Map<String, TypeMappingGenerator> typeMappers;

    public void inject(TypeMappersManager typeMappersManager){
        this.typeMappers = typeMappersManager.getTypeMappers();
    }

    public TypeMappingGenerator getTypeMapper(String type) {
        return Optional.ofNullable(this.typeMappers.get(type))
                .orElseThrow(() -> new RuntimeException(String.format("cannot find mapper for type %s", type)));
    }
}
