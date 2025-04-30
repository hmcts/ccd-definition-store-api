package uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules;


import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;

import java.time.LocalDate;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LiveDatesRuleTest {

    private LiveDatesRule rule;

    @BeforeEach
    public void setUp() {
        rule = new LiveDatesRule();
    }

    @Test
    public void validCaseTest() {
        // Given - a Case Type which abides by all Versioning rules
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        caseTypeEntity.setLiveFrom(LocalDate.ofEpochDay(new Date(100).getTime()));

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation passes
        assertNull(result, "Expected validation to pass");
    }

    @Test
    public void nullLiveFromTest() {
        // Given - a Case Type which has no Live From date
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        caseTypeEntity.setVersion(1);

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation fails with the expected message
        assertEquals("A Case Type must have a Live From date", result, "Unexpected error message");
    }

    @Test
    public void liveFromAfterLiveUntilTest() {
        // Given - a Case Type which has a Live From date after the Live Until date
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        CaseType caseType = new CaseType();
        caseType.setJurisdiction(new Jurisdiction());
        caseType.setId("CASE_TYPE_ID");

        caseTypeEntity.setVersion(69);
        caseTypeEntity.setLiveFrom(LocalDate.ofEpochDay(new Date(100).getTime()));
        caseTypeEntity.setLiveTo(LocalDate.ofEpochDay(new Date(50).getTime()));

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation fails with the expected message
        assertEquals("The Live From date must be before the Live Until date", result, "Unexpected error message");
    }
}
