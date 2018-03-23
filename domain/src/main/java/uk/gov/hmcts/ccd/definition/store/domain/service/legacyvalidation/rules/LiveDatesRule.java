package uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.ValidationRule;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Service
public class LiveDatesRule implements ValidationRule {

    private static final String NULL_LIVE_FROM = "A Case Type must have a Live From date";
    private static final String FROM_AFTER_UNTIL = "The Live From date must be before the Live Until date";

    /**
     * Validate the the Case Type's version information is valid
     *
     * @param caseTypeEntity - Case Type being validated
     */
    @Override
    public String validate(CaseTypeEntity caseTypeEntity) {

        if (caseTypeEntity.getLiveFrom() == null)
            return NULL_LIVE_FROM;

        if (caseTypeEntity.getLiveTo() != null && !caseTypeEntity.getLiveFrom().isBefore(caseTypeEntity.getLiveTo()))
            return FROM_AFTER_UNTIL;

        return null;
    }
}
