package uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@FunctionalInterface
public interface ValidationRule {

    /**
     * Perform validation on the given Case Type.
     *
     * @param caseTypeEntity - Case Type being validated
     */
    String validate(CaseTypeEntity caseTypeEntity);
}
