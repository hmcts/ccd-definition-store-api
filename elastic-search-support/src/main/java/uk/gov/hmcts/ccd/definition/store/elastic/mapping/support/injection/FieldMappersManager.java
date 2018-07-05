package uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection;

import static com.google.common.collect.Maps.newHashMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.field.FieldMappingGenerator;

/**
 * used to break circular dependency between field mappers which doesn't allow them to be autowired
 */
@Component
public class FieldMappersManager {

    protected Map<String, FieldMappingGenerator> fieldMappers = newHashMap();

    public Map<String, FieldMappingGenerator> getFieldMappers() {
        return fieldMappers;
    }

    @Autowired
    public void setFieldMappers(List<FieldMappingGenerator> mappingGenerators) {
        for (FieldMappingGenerator mg : mappingGenerators) {
            for (String type : mg.getCcdTypes()) {
                fieldMappers.put(type, mg);
            }
        }
    }

    //This line will guarantee the MappersManager will be injected last
    @Autowired
    private Set<Injectable> injectables = new HashSet();

    //This method will make sure all the injectable classes will get the MappersManager in its steady state,
    //where it's class members are ready to be set
    @PostConstruct
    private void inject() {
        for (Injectable injectableItem : injectables) {
            injectableItem.inject(this);
        }
    }
}
