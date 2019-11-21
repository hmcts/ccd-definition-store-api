package uk.gov.hmcts.ccd.definition.store.domain.service.casetype.sorters;

import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FieldTypeListItemSorter {


    public List<FixedListItem> sortFixedListItems(final List<FixedListItem> fixedListItems) {
        // if there is not a defined value for displayOrder it will return the values as they come.
        if (isItNotASortedFixedList(fixedListItems)) {
            return fixedListItems;
        }
        //if there is a defined display order it has to be applied.
        return applyDisplayOrderLogic(fixedListItems);
    }

    private List<FixedListItem> applyDisplayOrderLogic(final List<FixedListItem> fixedListItems) {
        // if all elements have a defined a display order it will order all elements using display order.
        if (hasAllElementGotAnDisplayOrder(fixedListItems)) {
            return fixedListItems.stream()
                .sorted(Comparator.comparing(FixedListItem::getOrder)).collect(Collectors.toList());
        } else {
            // if only some elements has a display order it will sort them only and put the others with order
            // at the top of the list.
            return sortFixedListItemWithMixedOrderCriteria(fixedListItems);
        }
    }

    private List<FixedListItem> sortFixedListItemWithMixedOrderCriteria(final List<FixedListItem> fixedListItems) {

        final List<FixedListItem> elementsWithDisplayOrder = fixedListItems.stream()
            .filter(fixedListItem -> fixedListItem.getOrder() != null).collect(Collectors.toList());

        final List<FixedListItem> elementsWithoutDisplayOrder = fixedListItems.stream()
            .filter(fixedListItem -> fixedListItem.getOrder() == null).collect(Collectors.toList());

        return Stream.concat(
            applyDisplayOrderLogic(elementsWithDisplayOrder).stream(),
            elementsWithoutDisplayOrder.stream()).collect(Collectors.toList()
        );
    }

    private boolean isItNotASortedFixedList(final List<FixedListItem> fixedListItems) {
        return fixedListItems.stream().allMatch(fixedListItem -> fixedListItem.getOrder() == null);
    }


    private boolean hasAllElementGotAnDisplayOrder(final List<FixedListItem> fixedListItems) {
        return fixedListItems.stream().allMatch(fixedListItem -> fixedListItem.getOrder() != null);
    }

}
