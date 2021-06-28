package uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles;

import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleAssignment;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleToAccessProfiles;

import java.util.List;

public interface RoleToAccessProfileService {

    void saveAll(List<RoleToAccessProfilesEntity> entityList);

    List<RoleToAccessProfiles> findByRoleName(String roleName);

    List<RoleToAccessProfiles> findByCaseTypeReferences(List<String> caseTypeReferences);

    List<RoleAssignment> findByCaseTypeId(String id);
}
