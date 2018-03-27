package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.UserRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.RESTRICTED;

class UserRoleServiceImplTest {

    private UserRoleRepository repository;
    private UserRoleService service;

    private UserRoleEntity entity = mock(UserRoleEntity.class);
    private UserRole mockUserRole = mock(UserRole.class);

    @BeforeEach
    void setUp() {
        repository = mock(UserRoleRepository.class);
        service = new UserRoleServiceImpl(repository);
    }

    @Test
    @DisplayName("should return role if defined")
    void getRole_shouldGetRole() {

        final String role = "role";
        givenEntityWithRole(role);

        doReturn(Optional.of(entity)).when(repository).findTopByRole(role);

        final UserRole userRole = service.getRole(role);

        assertThat(userRole.getId(), is(-3));
        assertThat(userRole.getRole(), is(role));
        assertThat(userRole.getSecurityClassification(), is(RESTRICTED));
    }

    @Test
    @DisplayName("should throw NotFoundException when role is not found")
    void getRole_NotFound() {

        final String role = "roleX";

        doReturn(Optional.empty()).when(repository).findTopByRole(role);

        Throwable thrown = assertThrows(NotFoundException.class, () -> service.getRole(role));
        assertEquals("Role 'roleX' is not found", thrown.getMessage());
    }

    @Test
    @DisplayName("should create role is saved")
    void shouldCreate_whenSaveRole() {

        final String role = "create";
        final ArgumentCaptor<UserRoleEntity> argumentCaptor = ArgumentCaptor.forClass(UserRoleEntity.class);

        givenUserRole(role, RESTRICTED);
        givenEntityWithRole(role);

        doReturn(Optional.empty()).when(repository).findTopByRole(role);
        doReturn(entity).when(repository).save(argumentCaptor.capture());

        final UserRole saved = service.saveRole(mockUserRole).getResponseBody();
        final UserRoleEntity captured = argumentCaptor.getValue();

        verify(repository).save(any(UserRoleEntity.class));
        assertThat(captured.getRole(), is(role));
        assertThat(captured.getSecurityClassification(), is(RESTRICTED));


        verify(entity, never()).setSecurityClassification(any());
        verify(entity, never()).setRole(anyString());

        assertThat(saved.getRole(), is(role));
        assertThat(saved.getSecurityClassification(), is(RESTRICTED));

    }

    @Test
    @DisplayName("should update role is saved")
    void shouldUpdate_whenSaveRole() {

        final String role = "update";
        final ArgumentCaptor<UserRoleEntity> argumentCaptor = ArgumentCaptor.forClass(UserRoleEntity.class);
        final UserRoleEntity savedEntity = mock(UserRoleEntity.class);

        givenUserRole(role, PUBLIC);
        givenEntityWithRole(role);
        givenEntityWithRole(role, PUBLIC, savedEntity);

        doReturn(Optional.of(entity)).when(repository).findTopByRole(role);
        doReturn(savedEntity).when(repository).save(argumentCaptor.capture());

        final UserRole saved = service.saveRole(mockUserRole).getResponseBody();
        final UserRoleEntity captured = argumentCaptor.getValue();

        verify(repository).save(any(UserRoleEntity.class));
        assertThat(captured.getRole(), is(role));

        verify(entity).setSecurityClassification(PUBLIC);
        verify(entity, never()).setRole(anyString());

        assertThat(saved.getRole(), is(role));
        assertThat(saved.getSecurityClassification(), is(PUBLIC));
    }

    @Test
    @DisplayName("should return userRoles if defined")
    void getRoles() {
        String[] roleNames = {"role1", "role2", "role3"};
        UserRoleEntity entity2 = mock(UserRoleEntity.class);
        givenUserRole(roleNames[0], PUBLIC);
        givenEntityWithRole(roleNames[0], RESTRICTED, entity);
        givenUserRole(roleNames[2], PUBLIC);
        givenEntityWithRole(roleNames[2], PUBLIC, entity2);

        doReturn(Arrays.asList(entity,entity2)).when(repository).findByRoleIn(Arrays.asList(roleNames));

        List<UserRole> userRoles = service.getRoles(Arrays.asList(roleNames));

        assertAll(
            () -> assertThat(userRoles.get(0).getId(), is(-3)),
            () -> assertThat(userRoles.get(0).getRole(), is(roleNames[0])),
            () -> assertThat(userRoles.get(0).getSecurityClassification(), is(RESTRICTED)),
            () -> assertThat(userRoles.get(1).getId(), is(-3)),
            () -> assertThat(userRoles.get(1).getRole(), is(roleNames[2])),
            () -> assertThat(userRoles.get(1).getSecurityClassification(), is(PUBLIC))
        );
    }

    @Test
    @DisplayName("should return null userRoles if no roles defined")
    void getNonExistentRoles() {
        String[] roleNames = {"role1", "role2", "role3"};
        doReturn(Collections.EMPTY_LIST).when(repository).findByRoleIn(Arrays.asList(roleNames));

        List<UserRole> userRoles = service.getRoles(Arrays.asList(roleNames));

        assertThat(userRoles.size(), is(0));
    }

    private void givenEntityWithRole(final String role) {
        givenEntityWithRole(role, RESTRICTED, entity);
    }

    private void givenEntityWithRole(final String role, SecurityClassification sc, UserRoleEntity e) {
        given(e.getId()).willReturn(-3);
        given(e.getRole()).willReturn(role);
        given(e.getSecurityClassification()).willReturn(sc);
        given(e.getCreatedAt()).willReturn(LocalDateTime.of(2011, 6, 5, 23, 59, 59));
    }

    private void givenUserRole(final String role, SecurityClassification securityClassification) {
        given(mockUserRole.getRole()).willReturn(role);
        given(mockUserRole.getSecurityClassification()).willReturn(securityClassification);
    }
}
