package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;

class CaseTypeUserRoleEntityBuilder {

    static CaseTypeACLEntity buildCaseTypeUserRoleEntity(final String crud) {
        final CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("case_type");

        final CaseTypeACLEntity entity = new CaseTypeACLEntity();
        entity.setCrudAsString(crud);
        entity.setCaseType(caseTypeEntity);

        return entity;
    }
}
