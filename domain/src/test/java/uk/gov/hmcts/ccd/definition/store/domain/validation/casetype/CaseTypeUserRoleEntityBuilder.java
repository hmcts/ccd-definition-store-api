package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeUserRoleEntity;

class CaseTypeUserRoleEntityBuilder {

    static CaseTypeUserRoleEntity buildCaseTypeUserRoleEntity(final String crud) {
        final CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("case_type");

        final CaseTypeUserRoleEntity entity = new CaseTypeUserRoleEntity();
        entity.setCrudAsString(crud);
        entity.setCaseType(caseTypeEntity);

        return entity;
    }
}
