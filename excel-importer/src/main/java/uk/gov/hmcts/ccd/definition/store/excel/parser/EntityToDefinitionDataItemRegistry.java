package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequestScope
public class EntityToDefinitionDataItemRegistry {

    private Map<Object, DefinitionDataItem> entityToDefinationDataItem = new HashMap<>();

    public void addDefinitionDataItemForEntity(Object entity, DefinitionDataItem definitionDataItem) {
        this.entityToDefinationDataItem.put(entity, definitionDataItem);
    }

    public Optional<DefinitionDataItem> getForEntity(Object entity) {
        return Optional.ofNullable(this.entityToDefinationDataItem.get(entity));
    }


}
