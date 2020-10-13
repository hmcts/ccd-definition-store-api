package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class FieldTypeValidationContextFactoryTest {

    private static final FieldTypeEntity BASE_TYPE_1 = new FieldTypeEntity();
    private static final FieldTypeEntity BASE_TYPE_2 = new FieldTypeEntity();

    private FieldTypeRepository typeRepository;
    private FieldTypeValidationContextFactory factory;

    @Before
    public void setUp() {
        typeRepository = mock(FieldTypeRepository.class);

        factory = new FieldTypeValidationContextFactory(typeRepository);
    }

    @Test
    public void shouldCreateContextWithBaseTypes() {
        doReturn(Arrays.asList(BASE_TYPE_1, BASE_TYPE_2))
            .when(typeRepository)
            .findCurrentBaseTypes();

        final FieldTypeValidationContext context = factory.create();

        assertThat(context.getBaseTypes(), hasSize(2));
        assertThat(context.getBaseTypes(), hasItem(BASE_TYPE_1));
        assertThat(context.getBaseTypes(), hasItem(BASE_TYPE_2));
    }

}
