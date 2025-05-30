package uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation;


import org.junit.jupiter.api.Test;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LegacyCaseTypeValidatorTest {

    private LegacyCaseTypeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new LegacyCaseTypeValidator(new ArrayList<>());
    }

    @Test
    void validateCaseTypeTest() {
        // Given - A Case Type which abides by all ValidationRule's
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        // When - Validating the Case Type
        // Then - Assert that the validation passes
        // (fail the test if an exception is thrown)
        try {
            validator.validateCaseType(caseTypeEntity);
        } catch (CaseTypeValidationException e) {
            System.out.println(e);
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
    }

    @Test
    void validateInvalidCaseTypeEntityTest() {
        // Given - a Case Type entity with an empty Case Type
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        // Create mock ValidationRule
        ValidationRule rule = mock(ValidationRule.class);
        when(rule.validate(caseTypeEntity)).thenReturn("Test Error Message");
        List<ValidationRule> rules = new ArrayList<>();
        rules.add(rule);
        validator = new LegacyCaseTypeValidator(rules);

        // When - Validating the Case Type
        // Then - assert that validation fails
        CaseTypeValidationException  e = assertThrows(CaseTypeValidationException.class, () -> {
            validator.validateCaseType(caseTypeEntity);
        });
        
        assertEquals(1, e.getErrors().size(), "One error expected");
        assertTrue(e.getErrors().contains("Test Error Message"), "Unexpected error message");
    }

}
