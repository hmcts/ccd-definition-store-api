package uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles;

import java.util.List;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleToAccessProfile;

public interface RoleToAccessProfileService {

    void saveAll(List<RoleToAccessProfileEntity> entity);

    List<RoleToAccessProfile> findByRoleName(String roleName);

    List<RoleToAccessProfile> findByCaseTypeReferences(List<String> caseTypeReferences);
}
