package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BaseReferenceFieldTypeValidatorTest {

    private static final JurisdictionEntity JURISDICTION = new JurisdictionEntity();

    @Mock
    private FieldTypeValidationContext context;

    private final FieldTypeEntity globalType = new FieldTypeEntity();
    public static final String GLOBAL_TYPE_REFERENCE = "Text";

    private BaseReferenceFieldTypeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BaseReferenceFieldTypeValidator();
        globalType.setReference(GLOBAL_TYPE_REFERENCE);
    }

    @DisplayName("Should accept global type overriding base type reference")
    @Test
    void shouldAcceptGlobalTypeOverridingBaseTypeReference() {
        final FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(GLOBAL_TYPE_REFERENCE);
        fieldType.setJurisdiction(null); // No jurisdiction --> Global field type

        final ValidationResult result = validator.validate(context, fieldType);

        assertAll(
            () -> assertTrue(result.isValid()),
            () -> assertEquals(0, result.getValidationErrors().size())
        );

        verifyNoInteractions(context);
    }

    @DisplayName("Should reject jurisdiction type overriding base type reference")
    @Test
    void shouldRejectJurisdictionTypeOverridingBaseTypeReference() {
        final FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(GLOBAL_TYPE_REFERENCE);
        fieldType.setJurisdiction(JURISDICTION);

        doReturn(Collections.singleton(globalType)).when(context).getBaseTypes();

        final ValidationResult result = validator.validate(context, fieldType);

        assertAll(
            () -> assertFalse(result.isValid()),
            () -> assertEquals(1, result.getValidationErrors().size()),
            () -> assertTrue(result.getValidationErrors().get(0) instanceof CannotOverrideBaseTypeValidationError),
            () -> assertEquals(fieldType, ((CannotOverrideBaseTypeValidationError) result.getValidationErrors().get(0))
                .getFieldTypeEntity()),
            () -> assertEquals(globalType, ((CannotOverrideBaseTypeValidationError) result.getValidationErrors().get(0))
                .getConflictingFieldTypeEntity()),
            () -> assertEquals("Cannot override base type: " + GLOBAL_TYPE_REFERENCE,
                               (result.getValidationErrors().get(0)).getDefaultMessage())
        );

        verify(context, times(1)).getBaseTypes();
        verify(context, never()).getBaseComplexTypes();
    }

}
