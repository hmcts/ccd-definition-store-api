package uk.gov.hmcts.ccd.definition.store.event;

import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

public class RoleImportedEvent extends ImportEvent<UserRoleEntity> {

    public RoleImportedEvent(UserRoleEntity role) {
        super(role);
    }
}
