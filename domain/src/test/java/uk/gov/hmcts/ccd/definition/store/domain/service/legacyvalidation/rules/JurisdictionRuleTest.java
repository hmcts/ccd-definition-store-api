package uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JurisdictionRuleTest {

    private JurisdictionRule rule;

    @Before
    public void setUp() {
        rule = new JurisdictionRule();
    }

    @Test
    public void validCaseTest() {
        // Given - a Case Type which abides by all Jurisdiction rules
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setName("JURISDICTION_NAME");
        jurisdiction.setLiveFrom(new Date(100));

        caseTypeEntity.setJurisdiction(jurisdiction);

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation passes
        assertNull("Expected validation to pass", result);
    }

    @Test
    public void nullJurisdictionTest() {
        // Given - a Case Type with no Jurisdiction Id
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation fails with the expected message
        assertEquals("Unexpected error message", "A Case Type must have a Jurisdiction", result);
    }

    @Test
    public void nullJurisdictionNameTest() {
        // Given - a Case Type with a Jurisdiction which has no name
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setLiveFrom(new Date(100));
        caseTypeEntity.setJurisdiction(jurisdiction);

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation fails with the expected message
        assertEquals("Unexpected error message", "A Jurisdiction must have a name", result);
    }

    @Test
    public void nullJurisdictionLiveFromTest() {
        // Given - a Case Type with a Jurisdiction with no Live From date
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setName("JURISDICTION_NAME");

        caseTypeEntity.setJurisdiction(jurisdiction);

        // When - performing validation on the Case Type
        String result = rule.validate(caseTypeEntity);

        // Then - assert that validation fails with the expected message
        assertEquals("Unexpected error message", "A Jurisdiction must have a Live From date", result);
    }

    @Test
    public void liveFromAfterLiveUntilTest() {

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
        assertEquals("Unexpected error message", "The Live From date must be before the Live Until date", result);
    }
}
