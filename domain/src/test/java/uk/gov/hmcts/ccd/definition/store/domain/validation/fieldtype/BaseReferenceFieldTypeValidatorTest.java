package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class BaseReferenceFieldTypeValidatorTest {

    private static final JurisdictionEntity JURISDICTION = new JurisdictionEntity();

    @Mock
    private FieldTypeValidationContext context;

    final FieldTypeEntity globalType = new FieldTypeEntity();

    private BaseReferenceFieldTypeValidator validator;

    public static final String GLOBAL_TYPE_REFERENCE = "Text";

    @Before
    public void setUp() {
        validator = new BaseReferenceFieldTypeValidator();

        globalType.setReference(GLOBAL_TYPE_REFERENCE);

        doReturn(Collections.singleton(globalType)).when(context).getBaseTypes();
    }

    @Test
    public void shouldAcceptGlobalTypeOverridingBaseTypeReference() {
        final FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(GLOBAL_TYPE_REFERENCE);
        fieldType.setJurisdiction(null); // No jurisdiction --> Global field type

        final ValidationResult result = validator.validate(context, fieldType);

        assertThat(result.isValid(), is(true));
        assertThat(result.getValidationErrors(), hasSize(0));
    }

    @Test
    public void shouldRejectJurisdictionTypeOverridingBaseTypeReference() {
        final FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(GLOBAL_TYPE_REFERENCE);
        fieldType.setJurisdiction(JURISDICTION);

        final ValidationResult result = validator.validate(context, fieldType);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertTrue(result.getValidationErrors().get(0)
            instanceof CannotOverrideBaseTypeValidationError);
        assertEquals(
            fieldType,
            ((CannotOverrideBaseTypeValidationError) result.getValidationErrors().get(0)).getFieldTypeEntity()
        );
        assertEquals(
            globalType,
            ((CannotOverrideBaseTypeValidationError) result.getValidationErrors().get(0))
                .getConflictingFieldTypeEntity()
        );

    }

}
