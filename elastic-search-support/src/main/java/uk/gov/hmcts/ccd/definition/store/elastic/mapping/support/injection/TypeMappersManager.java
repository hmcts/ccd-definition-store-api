package uk.gov.hmcts.ccd.definition.store.elastic.mapping.support.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.type.TypeMappingGenerator;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

/**
 * used to inject type mappers, which can't be Autowired because of a circular dependency between them. i.e. a
 * ComplexTypeMappingGenerator needs to be injected with a ComplexTypeMappingGenerator to handle complex types made
 * of nested complex types
 */
@Component
public class TypeMappersManager {

    protected Map<String, TypeMappingGenerator> typeMappers = newHashMap();

    //This line will guarantee the TypeMappersManager will be injected last
    @Autowired
    private Set<Injectable> injectables;

    public Map<String, TypeMappingGenerator> getTypeMappers() {
        return typeMappers;
    }

    @Autowired
    public void setTypeMappers(List<TypeMappingGenerator> mappingGenerators) {
        for (TypeMappingGenerator mapper : mappingGenerators) {
            for (String type : mapper.getMappedTypes()) {
                typeMappers.put(type, mapper);
            }
        }
    }

    //This method will make sure all the injectable classes will get the TypeMappersManager in its steady state,
    //where it's class members are ready to be set
    @PostConstruct
    protected void inject() {
        for (Injectable injectableItem : injectables) {
            injectableItem.inject(this);
        }
    }
}
