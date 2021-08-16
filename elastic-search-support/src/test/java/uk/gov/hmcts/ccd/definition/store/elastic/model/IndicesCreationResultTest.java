package uk.gov.hmcts.ccd.definition.store.elastic.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;

class IndicesCreationResultTest {

    @Test
    void shouldCreateResult() {
        List<CaseTypeEntity> caseTypes = new ArrayList<>();
        caseTypes.add(createCaseType("CT1", "J1"));
        caseTypes.add(createCaseType("CT2", "J1"));
        caseTypes.add(createCaseType("CT3", "J2"));
        caseTypes.add(createCaseType("CT4", "J2"));
        caseTypes.add(createCaseType("CT5", "J2"));
        caseTypes.add(createCaseType("CT6", "J3"));

        IndicesCreationResult result = new IndicesCreationResult(caseTypes);

        assertAll(
            () -> assertThat(result.getTotal(), is(6)),
            () -> assertThat(result.getCaseTypesByJurisdiction().keySet().size(), is(3)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J1").size(), is(2)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J2").size(), is(3)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J3").size(), is(1)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J1"), hasItem("CT1")),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J1"), hasItem("CT2")),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J2"), hasItem("CT3")),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J2"), hasItem("CT4")),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J2"), hasItem("CT5")),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J3"), hasItem("CT6"))
        );
    }

    private CaseTypeEntity createCaseType(String reference, String jurisdictionReference) {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(reference);
        caseType.setJurisdiction(createJurisdiction(jurisdictionReference));
        caseType.setSecurityClassification(PUBLIC);
        return caseType;
    }

    private JurisdictionEntity createJurisdiction(String reference) {
        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference(reference);
        return jurisdiction;
    }
}
