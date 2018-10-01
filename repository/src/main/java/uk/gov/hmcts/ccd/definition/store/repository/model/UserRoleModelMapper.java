package uk.gov.hmcts.ccd.definition.store.repository.model;

import javax.validation.constraints.NotNull;

import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

public class UserRoleModelMapper {

    private UserRoleModelMapper() {
        // blank line intended
    }

    public static UserRoleEntity toEntity(@NotNull UserRole model) {
        final UserRoleEntity entity = new UserRoleEntity();
        entity.setReference(model.getRole());
        entity.setSecurityClassification(model.getSecurityClassification());
        return entity;
    }

    public static UserRole toModel(@NotNull UserRoleEntity entity) {
        final UserRole model = new UserRole();
        model.setCreatedAt(entity.getCreatedAt().toString());
        model.setId(entity.getId());
        model.setRole(entity.getReference());
        model.setSecurityClassification(entity.getSecurityClassification());
        return model;
    }
}
