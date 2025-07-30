package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CannotOverridePredefinedComplexTypeValidationErrorTest {

    private CannotOverridePredefinedComplexTypeValidationError classUnderTest;

    @BeforeEach
    void setUp() {
        classUnderTest = new CannotOverridePredefinedComplexTypeValidationError(
            fieldTypeEntityWithReference("OrderSuMMary"),
            fieldTypeEntityWithReference("OrderSummary")
        );
    }

    @Test
    void testDefaultMessage() {
        assertEquals(
            "Cannot override predefined complex type: OrderSummary",
            classUnderTest.getDefaultMessage()
        );
    }

    private FieldTypeEntity fieldTypeEntityWithReference(String reference) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        return fieldTypeEntity;
    }
}
