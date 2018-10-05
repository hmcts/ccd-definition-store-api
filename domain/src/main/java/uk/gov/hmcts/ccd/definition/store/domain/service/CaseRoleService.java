package uk.gov.hmcts.ccd.definition.store.domain.service;

import uk.gov.hmcts.ccd.definition.store.repository.model.CaseRole;

import java.util.List;

public interface CaseRoleService {
    List<CaseRole> findByCaseTypeId(String id);
}
