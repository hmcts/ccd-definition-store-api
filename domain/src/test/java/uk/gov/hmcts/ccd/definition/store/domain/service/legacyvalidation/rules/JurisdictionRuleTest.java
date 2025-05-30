package uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules;


import org.junit.jupiter.api.Test;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;

class JurisdictionRuleTest {

    private JurisdictionRule rule;

    @BeforeEach
    void setUp() {
        rule = new JurisdictionRule();
    }

    @Test
    void validCaseTest() {
        // Given - a Case Type which abides by all Jurisdiction rules
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setName("JURISDICTION_NAME");
        jurisdiction.setLiveFrom(new Date(100));

        caseTypeEntity.setJurisdiction(jurisdiction);

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation passes
        assertNull(result, "Expected validation to pass");
    }

    @Test
    void nullJurisdictionTest() {
        // Given - a Case Type with no Jurisdiction Id
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation fails with the expected message
        assertEquals("A Case Type must have a Jurisdiction", result, "Unexpected error message");
    }

    @Test
    void nullJurisdictionNameTest() {
        // Given - a Case Type with a Jurisdiction which has no name
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setLiveFrom(new Date(100));
        caseTypeEntity.setJurisdiction(jurisdiction);

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation fails with the expected message
        assertEquals("A Jurisdiction must have a name", result, "Unexpected error message");
    }

    @Test
    void nullJurisdictionLiveFromTest() {
        // Given - a Case Type with a Jurisdiction with no Live From date
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setName("JURISDICTION_NAME");

        caseTypeEntity.setJurisdiction(jurisdiction);

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation fails with the expected message
        assertEquals("A Jurisdiction must have a Live From date", result, "Unexpected error message");
    }

    @Test
    void liveFromAfterLiveUntilTest() {

        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setName("JURISDICTION_NAME");
        jurisdiction.setLiveFrom(new Date(100));
        jurisdiction.setLiveTo(new Date(50));
        // Given - a Case Type with a Jurisdiction whose Live From date is after the Live Until date
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setJurisdiction(jurisdiction);

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation fails with the expected message
        assertEquals("The Live From date must be before the Live Until date", result, "Unexpected error message");
    }
}
