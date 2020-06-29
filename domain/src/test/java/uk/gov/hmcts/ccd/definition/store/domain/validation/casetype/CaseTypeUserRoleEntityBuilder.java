package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

class CaseTypeUserRoleEntityBuilder {

    private CaseTypeUserRoleEntityBuilder() {
        // Hide Utility Class Constructor : Utility classes should not have a public or default constructor (squid:S1118)
    }

    public static CaseTypeACLEntity buildCaseTypeUserRoleEntity(final String crud) {
        final CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("case_type");

        final CaseTypeACLEntity entity = new CaseTypeACLEntity();
        entity.setCrudAsString(crud);
        entity.setCaseType(caseTypeEntity);

        return entity;
    }
}
