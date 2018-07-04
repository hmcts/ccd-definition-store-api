package uk.gov.hmcts.ccd.definition.store.elastic;

import static com.google.common.collect.Maps.newHashMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MappersManager {

    protected Map<String, TypeMappingGenerator> typeMappers = newHashMap();

    public Map<String, TypeMappingGenerator> getTypeMappers() {
        return typeMappers;
    }

    @Autowired
    public void setTypeMappers(List<TypeMappingGenerator> mappingGenerators) {
        for (TypeMappingGenerator mg : mappingGenerators) {
            for (String type : mg.getTypes()) {
                typeMappers.put(type, mg);
            }
        }
    }

    //This line will guarantee the BeansManager class will be injected last
    @Autowired
    private Set<Injectable> injectables = new HashSet();

    //This method will make sure all the injectable classes will get the BeansManager in its steady state,
    //where it's class members are ready to be set
    @PostConstruct
    private void inject() {
        for (Injectable injectableItem : injectables) {
            injectableItem.inject(this);
        }
    }
}
