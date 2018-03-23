package uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LegacyCaseTypeValidatorTest {

    private LegacyCaseTypeValidator validator;

    @Before
    public void setUp() {
        validator = new LegacyCaseTypeValidator(new ArrayList<>());
    }

    @Test
    public void validateCaseTypeTest() {
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

    @Test(expected = CaseTypeValidationException.class)
    public void validateInvalidCaseTypeEntityTest() {
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
        try {
            validator.validateCaseType(caseTypeEntity);
        } catch (CaseTypeValidationException e) {
            assertEquals("One error expected", 1, e.getErrors().size());
            assertTrue("Unexpected error message", e.getErrors().contains("Test Error Message"));
            throw e;
        }

        // Fail if expected exception is not thrown
        fail("Expected exception not thrown");
    }

}
