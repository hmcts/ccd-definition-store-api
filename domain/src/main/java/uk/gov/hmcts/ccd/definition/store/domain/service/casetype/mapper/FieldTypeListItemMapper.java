package uk.gov.hmcts.ccd.definition.store.domain.service.casetype.mapper;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
                fixedListItem
                    .setOrder(fieldTypeListItemEntity.getOrder());
                fixedListItems.add(fixedListItem);
            }
        );
        if (hasADisplayOrder(fixedListItems)) {
            return applyDisplayOrder(fixedListItems);
        }
        return fixedListItems;
    }


    private List<FixedListItem> applyDisplayOrder(final List<FixedListItem> fixedListItems) {

        return fixedListItems.stream()
            .sorted(Comparator.comparing(FixedListItem::getOrder)).collect(Collectors.toList());
    }

    private boolean hasADisplayOrder(final List<FixedListItem> fixedListItems) {

        final long result = fixedListItems.stream().filter(fixedListItem -> fixedListItem.getOrder() != null).count();
        //if all elements has an order, it means that the order count has to the same as the list size.
        return result == fixedListItems.size();
    }
}
