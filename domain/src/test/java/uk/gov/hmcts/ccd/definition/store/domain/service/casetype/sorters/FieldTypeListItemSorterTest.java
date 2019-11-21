package uk.gov.hmcts.ccd.definition.store.domain.service.casetype.sorters;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class FieldTypeListItemSorterTest {

    private final FieldTypeListItemSorter fieldTypeListItemSorter = new FieldTypeListItemSorter();


    @Test
    public void testWithoutDisplayOrder() {

        final List<FixedListItem> inputList = new ArrayList<>();
        final FixedListItem item1 = new FixedListItem();
        final FixedListItem item2 = new FixedListItem();
        final FixedListItem item3 = new FixedListItem();

        item1.setCode("1");
        item1.setLabel("ONE");

        item2.setCode("2");
        item2.setLabel("TWO");

        item3.setCode("3");
        item3.setLabel("THREE");

        inputList.add(item1);
        inputList.add(item2);
        inputList.add(item3);

        final List<FixedListItem> result = fieldTypeListItemSorter.sortFixedListItems(inputList);

        assertThat(result.size(), equalTo(3));

        assertThat(result.get(0).getCode(), equalTo("1"));
        assertThat(result.get(0).getLabel(), equalTo("ONE"));

        assertThat(result.get(1).getCode(), equalTo("2"));
        assertThat(result.get(1).getLabel(), equalTo("TWO"));

        assertThat(result.get(2).getCode(), equalTo("3"));
        assertThat(result.get(2).getLabel(), equalTo("THREE"));
    }


    @Test
    public void testWithDisplayOrder() {

        final List<FixedListItem> inputList = new ArrayList<>();
        final FixedListItem item1 = new FixedListItem();
        final FixedListItem item2 = new FixedListItem();
        final FixedListItem item3 = new FixedListItem();

        item1.setCode("1");
        item1.setLabel("ONE");
        item1.setOrder(3);

        item2.setCode("2");
        item2.setLabel("TWO");
        item2.setOrder(2);

        item3.setCode("3");
        item3.setLabel("THREE");
        item3.setOrder(1);

        inputList.add(item1);
        inputList.add(item2);
        inputList.add(item3);

        final List<FixedListItem> result = fieldTypeListItemSorter.sortFixedListItems(inputList);

        assertThat(result.size(), equalTo(3));

        assertThat(result.get(0).getCode(), equalTo("3"));
        assertThat(result.get(0).getLabel(), equalTo("THREE"));

        assertThat(result.get(1).getCode(), equalTo("2"));
        assertThat(result.get(1).getLabel(), equalTo("TWO"));

        assertThat(result.get(2).getCode(), equalTo("1"));
        assertThat(result.get(2).getLabel(), equalTo("ONE"));
    }



    @Test
    public void testWithAndWithoutDisplayOrder() {

        final List<FixedListItem> inputList = new ArrayList<>();
        final FixedListItem item1 = new FixedListItem();
        final FixedListItem item2 = new FixedListItem();
        final FixedListItem item3 = new FixedListItem();
        final FixedListItem item4 = new FixedListItem();

        item1.setCode("1");
        item1.setLabel("ONE");

        item2.setCode("2");
        item2.setLabel("TWO");

        item3.setCode("3");
        item3.setLabel("THREE");
        item3.setOrder(1);

        item4.setCode("4");
        item4.setLabel("FOUR");
        item4.setOrder(2);

        inputList.add(item1);
        inputList.add(item2);
        inputList.add(item3);
        inputList.add(item4);

        final List<FixedListItem> result = fieldTypeListItemSorter.sortFixedListItems(inputList);

        assertThat(result.size(), equalTo(4));

        assertThat(result.get(0).getCode(), equalTo("3"));
        assertThat(result.get(0).getLabel(), equalTo("THREE"));

        assertThat(result.get(1).getCode(), equalTo("4"));
        assertThat(result.get(1).getLabel(), equalTo("FOUR"));

        assertThat(result.get(2).getCode(), equalTo("1"));
        assertThat(result.get(2).getLabel(), equalTo("ONE"));

        assertThat(result.get(3).getCode(), equalTo("2"));
        assertThat(result.get(3).getLabel(), equalTo("TWO"));
    }
}
