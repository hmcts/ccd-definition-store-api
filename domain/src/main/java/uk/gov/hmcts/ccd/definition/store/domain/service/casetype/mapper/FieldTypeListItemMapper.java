package uk.gov.hmcts.ccd.definition.store.domain.service.casetype.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import static uk.gov.hmcts.ccd.definition.store.repository.model.Comparators.NULLS_LAST_COMPARATOR;

public class FieldTypeListItemMapper {

    public List<FixedListItem> entityToModel(final List<FieldTypeListItemEntity>
                                                 fieldTypeListItemEntities) {
        List<FixedListItem> fixedListItems = new ArrayList<>();
        fieldTypeListItemEntities.forEach(fieldTypeListItemEntity ->
                                          {
                                              FixedListItem fixedListItem = new FixedListItem();
                                              fixedListItem
                                                  .setCode(fieldTypeListItemEntity.getValue());
                                              fixedListItem
                                                  .setLabel(fieldTypeListItemEntity.getLabel());
                                              fixedListItem
                                                  .setOrder(fieldTypeListItemEntity.getOrder());
                                              fixedListItems.add(fixedListItem);
                                          }
        );

        Collections.sort(fixedListItems, NULLS_LAST_COMPARATOR);
        return fixedListItems;
    }


}
