package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CannotOverrideBaseTypeValidationErrorTest {

    private CannotOverrideBaseTypeValidationError classUnderTest;

    @BeforeEach
    public void setUp() {
        classUnderTest = new CannotOverrideBaseTypeValidationError(
            fieldTypeEntityWithReference("TeXt"),
            fieldTypeEntityWithReference("Text")
        );
    }

    @Test
    public void testDefaultMessage() {
        assertEquals(
            "Cannot override base type: Text",
            classUnderTest.getDefaultMessage()
        );
    }

    private FieldTypeEntity fieldTypeEntityWithReference(String reference) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        return fieldTypeEntity;
    }

}
