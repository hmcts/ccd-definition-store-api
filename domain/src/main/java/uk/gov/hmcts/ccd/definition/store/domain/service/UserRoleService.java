package uk.gov.hmcts.ccd.definition.store.domain.service;

import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;

public interface UserRoleService {

    UserRole getRole(String role);

    ServiceResponse<UserRole> saveRole(UserRole userRole);
}
