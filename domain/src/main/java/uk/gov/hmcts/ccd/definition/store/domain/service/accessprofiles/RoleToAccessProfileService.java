package uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles;

import java.util.List;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleToAccessProfiles;

public interface RoleToAccessProfileService {

    void saveAll(List<RoleToAccessProfilesEntity> entityList);

    List<RoleToAccessProfiles> findByRoleName(String roleName);

    List<RoleToAccessProfiles> findByCaseTypeReferences(List<String> caseTypeReferences);
}
