package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.FieldMappersManager;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.Injectable;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.JsonGenerator;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.field.FieldMappingGenerator;

public abstract class AbstractMappingGenerator implements JsonGenerator, Injectable {

    @Autowired
    protected CcdElasticSearchProperties config;

    protected Map<String, FieldMappingGenerator> fieldMappers;

    public void inject(FieldMappersManager fieldMappersManager){
        this.fieldMappers = fieldMappersManager.getFieldMappers();
    }

    protected FieldMappingGenerator getMapperForType(String type) {
        return Optional.ofNullable(this.fieldMappers.get(type))
                .orElseThrow(() -> new RuntimeException(String.format("cannot find Mapper for type %s", type)));
    }

    public String disabled() {
        return config.getElasticMappings().get("disabled");
    }
}
