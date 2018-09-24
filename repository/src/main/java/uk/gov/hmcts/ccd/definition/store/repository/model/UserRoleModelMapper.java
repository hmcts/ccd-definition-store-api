package uk.gov.hmcts.ccd.definition.store.repository.model;

import javax.validation.constraints.NotNull;

import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

public class UserRoleModelMapper {

    private UserRoleModelMapper() {
        // blank line intended
    }

    public static UserRoleEntity toEntity(@NotNull UserRole model) {
        final UserRoleEntity entity = new UserRoleEntity();
//        if (null != model.getLiveFrom()) {
//            entity.setLiveFrom(LocalDate.parse(model.getLiveFrom()));
//        }
//        if (null != model.getLiveTo()) {
//            entity.setLiveTo(LocalDate.parse(model.getLiveTo()));
//        }
        entity.setReference(model.getRole());
        entity.setSecurityClassification(model.getSecurityClassification());
        return entity;
    }

    public static UserRole toModel(@NotNull UserRoleEntity entity) {
        final UserRole model = new UserRole();
        model.setCreatedAt(entity.getCreatedAt().toString());
        model.setId(entity.getId());
//        model.setLiveFrom(entity.getLiveFrom() == null ? null : entity.getLiveFrom().toString());
//        model.setLiveTo(entity.getLiveTo() == null ? null : entity.getLiveTo().toString());
        model.setRole(entity.getReference());
        model.setSecurityClassification(entity.getSecurityClassification());
        return model;
    }

}
