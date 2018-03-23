package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.junit.Assert.assertEquals;

public class CannotOverrideBaseTypeValidationErrorTest {

    private CannotOverrideBaseTypeValidationError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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
