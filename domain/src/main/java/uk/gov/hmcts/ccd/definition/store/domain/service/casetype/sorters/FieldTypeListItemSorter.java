package uk.gov.hmcts.ccd.definition.store.domain.service.casetype.sorters;

import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class FieldTypeListItemSorter {

    public List<FixedListItem> sortFixedListItems(final List<FixedListItem> fixedListItems) {

        if (noDisplayOrder(fixedListItems)) {
            return fixedListItems;
        }
        return sortFixedList(fixedListItems);
    }

    private List<FixedListItem> sortFixedList(final List<FixedListItem> fixedListItems) {
        if (allDisplayOrder(fixedListItems)) {
            return fixedListItems.stream()
                .sorted(Comparator.comparing(FixedListItem::getOrder)).collect(Collectors.toList());
        } else {
            return sortFixedListItemWithMixedOrderCriteria(fixedListItems);
        }
    }

    private List<FixedListItem> sortFixedListItemWithMixedOrderCriteria(final List<FixedListItem> fixedListItems) {

        final List<FixedListItem> elementsWithDisplayOrder = getItemsWithDisplayOrder(fixedListItems);
        final List<FixedListItem> elementsWithoutDisplayOrder = getItemsWithoutDisplayOrder(fixedListItems);

        return Stream.concat(
            sortFixedList(elementsWithDisplayOrder).stream(),
            elementsWithoutDisplayOrder.stream())
            .collect(Collectors.toList()
            );
    }

    private boolean noDisplayOrder(final List<FixedListItem> fixedListItems) {
        return getItemsWithDisplayOrder(fixedListItems).isEmpty();
    }

    private boolean allDisplayOrder(final List<FixedListItem> fixedListItems) {
        return getItemsWithDisplayOrder(fixedListItems).size() == fixedListItems.size();
    }

    private List<FixedListItem> getItemsWithDisplayOrder(List<FixedListItem> fixedListItems) {
        return fixedListItems.stream()
            .filter(fixedListItem -> fixedListItem.getOrder() != null).collect(Collectors.toList());
    }

    private List<FixedListItem> getItemsWithoutDisplayOrder(List<FixedListItem> fixedListItems) {
        return fixedListItems.stream()
            .filter(fixedListItem -> fixedListItem.getOrder() == null).collect(Collectors.toList());
    }
}
