package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.junit.Assert.assertEquals;

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
