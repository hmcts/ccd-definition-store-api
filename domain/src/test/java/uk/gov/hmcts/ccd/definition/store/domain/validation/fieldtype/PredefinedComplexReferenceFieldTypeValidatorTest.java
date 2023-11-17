package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PredefinedComplexReferenceFieldTypeValidatorTest {
    private static final JurisdictionEntity JURISDICTION = new JurisdictionEntity();

    @Mock
    private FieldTypeValidationContext context;

    private final FieldTypeEntity globalComplexType = new FieldTypeEntity();
    public static final String PREDEFINED_COMPLEX_TYPE_REFERENCE = "OrderSummary";

    private PredefinedComplexReferenceFieldTypeValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new PredefinedComplexReferenceFieldTypeValidator();
        globalComplexType.setReference(PREDEFINED_COMPLEX_TYPE_REFERENCE);
    }

    @DisplayName("Should accept global type overriding base complex type reference")
    @Test
    public void shouldAcceptGlobalTypeOverridingBaseComplexTypeReference() {
        final FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(PREDEFINED_COMPLEX_TYPE_REFERENCE);
        fieldType.setJurisdiction(null); // No jurisdiction --> Global complex field type

        final ValidationResult result = validator.validate(context, fieldType);

        assertAll(
            () -> assertTrue(result.isValid()),
            () -> assertEquals(0, result.getValidationErrors().size())
        );

        verifyNoInteractions(context);
    }

    @DisplayName("Should reject jurisdiction type overriding base type reference")
    @Test
    public void shouldRejectJurisdictionTypeOverridingBaseTypeReference() {
        final FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(PREDEFINED_COMPLEX_TYPE_REFERENCE);
        fieldType.setJurisdiction(JURISDICTION);

        doReturn(Collections.singleton(globalComplexType)).when(context).getBaseComplexTypes();

        final ValidationResult result = validator.validate(context, fieldType);

        assertAll(
            () -> assertFalse(result.isValid()),
            () -> assertEquals(1, result.getValidationErrors().size()),
            () -> assertTrue(result.getValidationErrors().get(0)
                                 instanceof CannotOverridePredefinedComplexTypeValidationError),
            () -> assertEquals(fieldType, ((CannotOverridePredefinedComplexTypeValidationError)
                result.getValidationErrors().get(0)).getFieldTypeEntity()),
            () -> assertEquals(globalComplexType, ((CannotOverridePredefinedComplexTypeValidationError)
                result.getValidationErrors().get(0)).getConflictingFieldTypeEntity()),
            () -> assertEquals("Cannot override predefined complex type: " + PREDEFINED_COMPLEX_TYPE_REFERENCE,
                               (result.getValidationErrors().get(0)).getDefaultMessage())
        );

        verify(context, times(1)).getBaseComplexTypes();
        verify(context, times(0)).getBaseTypes();
    }

}
