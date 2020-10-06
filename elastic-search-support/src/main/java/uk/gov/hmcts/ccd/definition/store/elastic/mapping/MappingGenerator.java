package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.JsonGenerator;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.Injectable;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection.TypeMappersManager;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.TypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.Map;
import java.util.Optional;

@Slf4j
public abstract class MappingGenerator implements JsonGenerator, Injectable {

    protected static final String DATA_CLASSIFICATION = "data_classification";
    protected static final String DATA = "data";
    protected static final String ALIAS = "alias";
    protected static final String ALIAS_TEXT_SORT = "aliasTextSort";
    protected static final String DEFAULT_TEXT = "defaultText";
    protected static final String PROPERTIES = "properties";
    protected static final String ID = "id";
    protected static final String CLASSIFICATION = "classification";
    protected static final String VALUE = "value";
    protected static final String COLLECTION = "Collection";
    protected static final String COMPLEX = "Complex";

    @Autowired
    protected CcdElasticSearchProperties config;

    protected Map<String, TypeMappingGenerator> typeMappers;

    @Override
    public void inject(TypeMappersManager typeMappersManager) {
        this.typeMappers = typeMappersManager.getTypeMappers();
    }

    public TypeMappingGenerator getTypeMapper(String type) {
        return Optional.ofNullable(this.typeMappers.get(type))
            .orElseThrow(() -> new ElasticSearchInitialisationException(
                String.format("cannot find mapper for type %s", type)));
    }

    public boolean shouldIgnore(FieldEntity field) {
        boolean ignored = config.getCcdIgnoredTypes().contains(field.getFieldType().getReference());
        if (ignored) {
            log.info("field {} of type {} ignored", field.getReference(), field.getBaseTypeString());
        }
        return ignored;
    }
}
