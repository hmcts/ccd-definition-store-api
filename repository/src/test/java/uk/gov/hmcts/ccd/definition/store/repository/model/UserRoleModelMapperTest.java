package uk.gov.hmcts.ccd.definition.store.repository.model;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


public class UserRoleModelMapperTest {

    @Test
    public void shouldGetEntity_whenToEntity() {
        final UserRole model = givenModel();
        final UserRoleEntity userRoleEntity = UserRoleModelMapper.toEntity(model);
        assertThat(userRoleEntity.getSecurityClassification(), is(SecurityClassification.PUBLIC));
        assertThat(userRoleEntity.getId(), is(nullValue()));
        assertThat(userRoleEntity.getCreatedAt(), is(nullValue()));
        assertThat(userRoleEntity.getLiveFrom(), is(LocalDate.parse(model.getLiveFrom())));
        assertThat(userRoleEntity.getLiveTo(), is(LocalDate.parse(model.getLiveTo())));

    }

    @Test
    public void shouldGetModel_whenToModel() {
        final UserRoleEntity entity = givenEntity();
        final UserRole model = UserRoleModelMapper.toModel(entity);

        assertThat(model.getId(), is(-3));
        assertThat(model.getRole(), is("entity role"));
        assertThat(model.getCreatedAt(), is("2011-06-05T23:59:59"));
        assertThat(model.getSecurityClassification(), is(SecurityClassification.RESTRICTED));
        assertThat(model.getLiveFrom(), is("2018-04-03"));
        assertThat(model.getLiveTo(), is("4000-02-29"));
    }

    private UserRole givenModel() {
        UserRole role = new UserRole();
        role.setId(-1);
        role.setRole("role");
        role.setSecurityClassification(SecurityClassification.PUBLIC);
        role.setLiveFrom("2018-02-28");
        role.setLiveTo("2024-02-29");
        role.setCreatedAt("2017-02-28");
        return role;
    }

    private UserRoleEntity givenEntity() {

        UserRoleEntity entity = mock(UserRoleEntity.class);

        given(entity.getId()).willReturn(-3);
        given(entity.getCreatedAt()).willReturn(LocalDateTime.of(2011, 6, 5, 23, 59, 59));
        given(entity.getSecurityClassification()).willReturn(SecurityClassification.RESTRICTED);
        given(entity.getRole()).willReturn("entity role");
        given(entity.getLiveFrom()).willReturn(LocalDate.of(2018, 4, 3));
        given(entity.getLiveTo()).willReturn(LocalDate.of(4000, 2, 29));

        return entity;
    }

}
