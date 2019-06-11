package uk.gov.hmcts.ccd.definition.store.domain.service.casetype.mapper;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

class FieldTypeListItemMapperTest {

    private FieldTypeListItemMapper fieldTypeListItemMapper = new FieldTypeListItemMapper();

    @BeforeEach
    void setUp() {

    }

    @Test
    void shouldMapAllFields() {

        List<FieldTypeListItemEntity> fieldTypeListItemEntities = Lists.newArrayList(newFieldTypeListItemEntity());


        fieldTypeListItemMapper.entityToModel(fieldTypeListItemEntities);

    }
}
