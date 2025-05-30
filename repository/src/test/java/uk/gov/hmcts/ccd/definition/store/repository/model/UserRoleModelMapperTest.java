package uk.gov.hmcts.ccd.definition.store.repository.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


class UserRoleModelMapperTest {

    @Test
    void shouldGetEntity_whenToEntity() {
        final UserRole model = givenModel();
        final AccessProfileEntity accessProfileEntity = UserRoleModelMapper.toEntity(model);

        assertThat(accessProfileEntity.getSecurityClassification(), is(SecurityClassification.PUBLIC));
        assertThat(accessProfileEntity.getId(), is(nullValue()));
        assertThat(accessProfileEntity.getCreatedAt(), is(nullValue()));
    }

    @Test
    void shouldGetModel_whenToModel() {
        final AccessProfileEntity entity = givenEntity();
        final UserRole model = UserRoleModelMapper.toModel(entity);

        assertThat(model.getId(), is(-3));
        assertThat(model.getRole(), is("entity role"));
        assertThat(model.getCreatedAt(), is("2011-06-05T23:59:59"));
        assertThat(model.getSecurityClassification(), is(SecurityClassification.RESTRICTED));
    }

    private UserRole givenModel() {
        UserRole role = new UserRole();

        role.setId(-1);
        role.setRole("role");
        role.setSecurityClassification(SecurityClassification.PUBLIC);
        role.setCreatedAt("2017-02-28");
        return role;
    }

    private AccessProfileEntity givenEntity() {
        AccessProfileEntity entity = mock(AccessProfileEntity.class);

        given(entity.getId()).willReturn(-3);
        given(entity.getCreatedAt()).willReturn(LocalDateTime.of(2011, 6, 5, 23, 59, 59));
        given(entity.getSecurityClassification()).willReturn(SecurityClassification.RESTRICTED);
        given(entity.getReference()).willReturn("entity role");

        return entity;
    }
}
