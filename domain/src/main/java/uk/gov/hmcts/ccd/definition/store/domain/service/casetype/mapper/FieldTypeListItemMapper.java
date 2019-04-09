package uk.gov.hmcts.ccd.definition.store.domain.service.casetype.mapper;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import java.util.ArrayList;
import java.util.List;

public class FieldTypeListItemMapper {
    public List<FixedListItem> entityToModel(final List<FieldTypeListItemEntity>
                                                                    fieldTypeListItemEntities) {
        List<FixedListItem> fixedListItems = new ArrayList<>();
        fieldTypeListItemEntities.stream().forEach(fieldTypeListItemEntity ->
                                                   {
                                                       FixedListItem fixedListItem = new FixedListItem();
                                                       fixedListItem
                                                           .setCode(fieldTypeListItemEntity.getValue());
                                                       fixedListItem
                                                           .setLabel(fieldTypeListItemEntity.getLabel());
                                                       fixedListItems.add(fixedListItem);
                                                   }
                                                  );
        return fixedListItems;
    }


}
