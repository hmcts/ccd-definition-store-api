package uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.ValidationRule;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

@Service
public class JurisdictionRule implements ValidationRule {

    private static final String NULL_JURISDICTION = "A Case Type must have a Jurisdiction";
    private static final String NULL_JURISDICTION_NAME = "A Jurisdiction must have a name";
    private static final String NULL_LIVE_FROM = "A Jurisdiction must have a Live From date";
    private static final String FROM_AFTER_UNTIL = "The Live From date must be before the Live Until date";

    /**
     * Validate that a valid Jurisdiction has been provided.
     * Should  with appropriate message when validation fails.
     *
     * @param caseTypeEntity - Case Type being validated
     */
    @Override
    public String validate(CaseTypeEntity caseTypeEntity) {
        JurisdictionEntity jurisdiction = caseTypeEntity.getJurisdiction();
        if (jurisdiction == null) {
            return NULL_JURISDICTION;
        }
        if (jurisdiction.getName() == null) {
            return NULL_JURISDICTION_NAME;
        }

        if (jurisdiction.getLiveFrom() == null) {
            return NULL_LIVE_FROM;
        }

        if (jurisdiction.getLiveFrom() != null
            && jurisdiction.getLiveTo() != null
            && !jurisdiction.getLiveFrom().before(jurisdiction.getLiveTo())) {
            return FROM_AFTER_UNTIL;
        }

        return null;
    }
}
