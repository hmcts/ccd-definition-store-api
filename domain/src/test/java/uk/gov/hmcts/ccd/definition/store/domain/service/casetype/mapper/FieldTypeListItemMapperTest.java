package uk.gov.hmcts.ccd.definition.store.domain.service.casetype.mapper;

import java.util.List;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeListItemBuilder.newFieldTypeListItem;

class FieldTypeListItemMapperTest {

    private FieldTypeListItemMapper fieldTypeListItemMapper = new FieldTypeListItemMapper();

    @Test
    void shouldMapAllFields() {
        List<FieldTypeListItemEntity> fieldTypeListItemEntities = newArrayList(
            newFieldTypeListItem().withOrder(5).withLabel("Label 1").withValue("LABEL1").build(),
            newFieldTypeListItem().withOrder(6).withLabel("Label 2").withValue("LABEL2").build());

        List<FixedListItem> fixedListItems = fieldTypeListItemMapper.entityToModel(fieldTypeListItemEntities);


        assertAll(
            () -> assertThat(fixedListItems, hasItem(allOf(hasProperty("order", is("5")),
                                                           hasProperty("label", is("Label 1")),
                                                           hasProperty("code", is("LABEL1"))))),
            () -> assertThat(fixedListItems, hasItem(allOf(hasProperty("order", is("6")),
                                                           hasProperty("label", is("Label 2")),
                                                           hasProperty("code", is("LABEL2")))))
        );
    }
}
