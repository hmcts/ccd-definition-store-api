package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.UserRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class UserRoleServiceImplTest {

    private UserRoleRepository repository;
    private UserRoleService service;

    private UserRoleEntity entity = mock(UserRoleEntity.class);
    private UserRole mockUserRole = mock(UserRole.class);

    @Before
    public void setUp() {
        repository = mock(UserRoleRepository.class);
        service = new UserRoleServiceImpl(repository);
    }

    @Test
    public void getRole_shouldGetRole() {

        final String role = "role";
        givenEntityWithRole(role);

        doReturn(Optional.of(entity)).when(repository).findTopByRole(role);

        final UserRole userRole = service.getRole(role);

        assertThat(userRole.getId(), is(-3));
        assertThat(userRole.getRole(), is(role));
        assertThat(userRole.getSecurityClassification(), is(SecurityClassification.RESTRICTED));
    }

    @Test(expected = NotFoundException.class)
    public void getRole_NotFound() {

        final String role = "roleX";

        doReturn(Optional.empty()).when(repository).findTopByRole(role);

        try {
            service.getRole(role);
        } catch (NotFoundException ex) {
            assertThat(ex.getMessage(), is("Role 'roleX' is not found"));
            throw ex;
        }
    }

    @Test
    public void shouldCreate_whenSaveRole() {

        final String role = "create";
        final ArgumentCaptor<UserRoleEntity> argumentCaptor = ArgumentCaptor.forClass(UserRoleEntity.class);

        givenUserRole(role, SecurityClassification.RESTRICTED);
        givenEntityWithRole(role);

        doReturn(Optional.empty()).when(repository).findTopByRole(role);
        doReturn(entity).when(repository).save(argumentCaptor.capture());

        final UserRole saved = service.saveRole(mockUserRole).getResponseBody();
        final UserRoleEntity captured = argumentCaptor.getValue();

        verify(repository).save(any(UserRoleEntity.class));
        assertThat(captured.getRole(), is(role));
        assertThat(captured.getSecurityClassification(), is(SecurityClassification.RESTRICTED));


        verify(entity, never()).setSecurityClassification(any());
        verify(entity, never()).setRole(anyString());

        assertThat(saved.getRole(), is(role));
        assertThat(saved.getSecurityClassification(), is(SecurityClassification.RESTRICTED));

    }

    @Test
    public void shouldUpdate_whenSaveRole() {

        final String role = "update";
        final ArgumentCaptor<UserRoleEntity> argumentCaptor = ArgumentCaptor.forClass(UserRoleEntity.class);
        final UserRoleEntity savedEntity = mock(UserRoleEntity.class);

        givenUserRole(role, SecurityClassification.PUBLIC);
        givenEntityWithRole(role);
        givenEntityWithRole(role, SecurityClassification.PUBLIC, savedEntity);

        doReturn(Optional.of(entity)).when(repository).findTopByRole(role);
        doReturn(savedEntity).when(repository).save(argumentCaptor.capture());

        final UserRole saved = service.saveRole(mockUserRole).getResponseBody();
        final UserRoleEntity captured = argumentCaptor.getValue();

        verify(repository).save(any(UserRoleEntity.class));
        assertThat(captured.getRole(), is(role));

        verify(entity).setSecurityClassification(SecurityClassification.PUBLIC);
        verify(entity, never()).setRole(anyString());

        assertThat(saved.getRole(), is(role));
        assertThat(saved.getSecurityClassification(), is(SecurityClassification.PUBLIC));
    }

    private void givenEntityWithRole(final String role) {
        givenEntityWithRole(role, SecurityClassification.RESTRICTED, entity);
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
