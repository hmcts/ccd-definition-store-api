package uk.gov.hmcts.ccd.definition.store.domain.service.casetype.mapper;

import java.util.List;
import java.util.stream.Collectors;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import static uk.gov.hmcts.ccd.definition.store.repository.model.Comparators.NULLS_LAST_ORDER_COMPARATOR;

public class FieldTypeListItemMapper {

    public List<FixedListItem> entityToModel(final List<FieldTypeListItemEntity>
                                                 fieldTypeListItemEntities) {
        return fieldTypeListItemEntities.stream()
                                 .map(fieldTypeListItemEntity -> {
                                     FixedListItem fixedListItem = new FixedListItem();
                                     fixedListItem.setCode(fieldTypeListItemEntity.getValue());
                                     fixedListItem.setLabel(fieldTypeListItemEntity.getLabel());
                                     fixedListItem.setOrder(fieldTypeListItemEntity.getOrder());
                                     return fixedListItem;
                                 })
                                 .sorted(NULLS_LAST_ORDER_COMPARATOR)
                                 .collect(Collectors.toList());
    }

}

