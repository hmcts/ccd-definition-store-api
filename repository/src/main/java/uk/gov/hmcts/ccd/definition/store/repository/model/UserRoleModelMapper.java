package uk.gov.hmcts.ccd.definition.store.repository.model;

import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;

import jakarta.validation.constraints.NotNull;

public class UserRoleModelMapper {

    private UserRoleModelMapper() {
        // blank line intended
    }

    public static AccessProfileEntity toEntity(@NotNull UserRole model) {
        final AccessProfileEntity entity = new AccessProfileEntity();
        entity.setReference(model.getRole());
        entity.setName(model.getRole());
        entity.setSecurityClassification(model.getSecurityClassification());
        return entity;
    }

    public static UserRole toModel(@NotNull AccessProfileEntity entity) {
        final UserRole model = new UserRole();
        model.setCreatedAt(entity.getCreatedAt().toString());
        model.setId(entity.getId());
        model.setRole(entity.getReference());
        model.setSecurityClassification(entity.getSecurityClassification());
        return model;
    }
}
