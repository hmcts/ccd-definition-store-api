package uk.gov.hmcts.ccd.definition.store.domain.service.casetype.mapper;

import java.util.List;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeListItemBuilder.newFieldTypeListItem;

class FieldTypeListItemMapperTest {

    private FieldTypeListItemMapper fieldTypeListItemMapper = new FieldTypeListItemMapper();

    @Test
    void shouldMapWithoutDisplayOrder() {
        List<FieldTypeListItemEntity> fieldTypeListItemEntities = newArrayList(
            newFieldTypeListItem().withLabel("ONE").withValue("1").build(),
            newFieldTypeListItem().withLabel("TWO").withValue("2").build(),
            newFieldTypeListItem().withLabel("THREE").withValue("3").build());


        List<FixedListItem> fixedListItems = fieldTypeListItemMapper.entityToModel(fieldTypeListItemEntities);

        assertAll(
            () -> assertThat(fixedListItems, contains(allOf(hasProperty("label", is("ONE")),
                                                            hasProperty("code", is("1"))),
                                                      allOf(hasProperty("label", is("TWO")),
                                                            hasProperty("code", is("2"))),
                                                      allOf(hasProperty("label", is("THREE")),
                                                            hasProperty("code", is("3")))))
        );
    }

    @Test
    void shouldMapWithDisplayOrder() {
        List<FieldTypeListItemEntity> fieldTypeListItemEntities = newArrayList(
            newFieldTypeListItem().withOrder(3).withLabel("ONE").withValue("1").build(),
            newFieldTypeListItem().withOrder(2).withLabel("TWO").withValue("2").build(),
            newFieldTypeListItem().withOrder(1).withLabel("THREE").withValue("3").build());


        List<FixedListItem> fixedListItems = fieldTypeListItemMapper.entityToModel(fieldTypeListItemEntities);

        assertAll(
            () -> assertThat(fixedListItems, contains(allOf(hasProperty("order", is(1)),
                                                            hasProperty("label", is("THREE")),
                                                            hasProperty("code", is("3"))),
                                                      allOf(hasProperty("order", is(2)),
                                                            hasProperty("label", is("TWO")),
                                                            hasProperty("code", is("2"))),
                                                      allOf(hasProperty("order", is(3)),
                                                            hasProperty("label", is("ONE")),
                                                            hasProperty("code", is("1")))))
        );
    }

    @Test
    void shouldMapAllFields() {
        List<FieldTypeListItemEntity> fieldTypeListItemEntities = newArrayList(
            newFieldTypeListItem().withOrder(5).withLabel("Label 1").withValue("LABEL1").build(),
            newFieldTypeListItem().withLabel("Label 2").withValue("LABEL2").build(),
            newFieldTypeListItem().withLabel("Label 5").withValue("LABEL5").build(),
            newFieldTypeListItem().withOrder(2).withLabel("Label 3").withValue("LABEL3").build(),
            newFieldTypeListItem().withOrder(9).withLabel("Label 4").withValue("LABEL4").build());

        List<FixedListItem> fixedListItems = fieldTypeListItemMapper.entityToModel(fieldTypeListItemEntities);

        assertAll(
            () -> assertThat(fixedListItems, contains(allOf(hasProperty("order", is(2)),
                                                            hasProperty("label", is("Label 3")),
                                                            hasProperty("code", is("LABEL3"))),
                                                      allOf(hasProperty("order", is(5)),
                                                            hasProperty("label", is("Label 1")),
                                                            hasProperty("code", is("LABEL1"))),
                                                      allOf(hasProperty("order", is(9)),
                                                            hasProperty("label", is("Label 4")),
                                                            hasProperty("code", is("LABEL4"))),
                                                      allOf(hasProperty("order", is(nullValue())),
                                                            hasProperty("label", is("Label 2")),
                                                            hasProperty("code", is("LABEL2"))),
                                                      allOf(hasProperty("order", is(nullValue())),
                                                            hasProperty("label", is("Label 5")),
                                                            hasProperty("code", is("LABEL5")))))
        );
    }
}
