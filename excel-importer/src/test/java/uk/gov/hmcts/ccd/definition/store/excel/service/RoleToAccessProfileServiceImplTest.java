package uk.gov.hmcts.ccd.definition.store.excel.service;

import com.google.common.collect.Sets;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.RoleToAccessProfileRepository;
import uk.gov.hmcts.ccd.definition.store.repository.accessprofile.GetCaseTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class RoleToAccessProfileServiceImplTest {

    @Mock
    private GetCaseTypeRolesRepository getCaseTypeRolesRepository;

    @Mock
    private RoleToAccessProfileRepository roleToAccessProfileRepository;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @Mock
    private ApplicationParams applicationParams;

    @InjectMocks
    private RoleToAccessProfileServiceImpl roleToAccessProfileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        roleToAccessProfileService = new RoleToAccessProfileServiceImpl(getCaseTypeRolesRepository,
            caseTypeRepository,
            roleToAccessProfileRepository,
            applicationParams);
    }

    @Test
    @DisplayName("Should not invoke case type validation when role to access profile mapping not enabled")
    void shouldNotInvokeCaseTypeValidation() {
        when(applicationParams.isRoleToAccessProfileMapping()).thenReturn(false);
        roleToAccessProfileService.createAccessProfileMapping("CaseType_1");
        verify(caseTypeRepository, times(0)).findCurrentVersionForReference(anyString());
    }

    @Test
    @DisplayName("Should invoke case type validation when role to access profile mapping not enabled")
    void shouldInvokeCaseTypeValidationWhenRoleToAccessProfileMappingEnabled() {
        when(applicationParams.isRoleToAccessProfileMapping()).thenReturn(true);
        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        when(caseTypeRepository.findCurrentVersionForReference(anyString()))
            .thenReturn(Optional.of(caseTypeEntity));
        roleToAccessProfileService.createAccessProfileMapping("CaseType_1");
        verify(caseTypeRepository, times(1)).findCurrentVersionForReference(anyString());
    }

    @Test
    @DisplayName("Should throw exception when case type not found")
    void shouldThrowNotFoundExceptionWhenCaseTypeNotFound() {
        when(applicationParams.isRoleToAccessProfileMapping()).thenReturn(true);
        when(caseTypeRepository.findCurrentVersionForReference(anyString())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> roleToAccessProfileService.createAccessProfileMapping("CaseType_1"));
        verify(caseTypeRepository, times(1)).findCurrentVersionForReference(anyString());
    }

    @Test
    @DisplayName("Should invoke Get Case Type Roles")
    void shouldInvokeGetCaseTypeRolesWhenCaseTypeExists() {
        when(applicationParams.isRoleToAccessProfileMapping()).thenReturn(true);
        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        when(caseTypeRepository.findCurrentVersionForReference(anyString())).thenReturn(Optional.of(caseTypeEntity));
        roleToAccessProfileService.createAccessProfileMapping("CaseType_1");
        verify(caseTypeRepository, times(1)).findCurrentVersionForReference(anyString());
        verify(getCaseTypeRolesRepository, times(1)).findCaseTypeRoles(anyString());
    }

    @Test
    @DisplayName("Should invoke save on role to access profiles when Case Type Roles exists")
    void shouldInvokeSaveRoleToAccessProfilesGetCaseTypeRolesWhenCaseTypeExists() {
        when(applicationParams.isRoleToAccessProfileMapping()).thenReturn(true);
        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        when(caseTypeRepository.findCurrentVersionForReference(anyString())).thenReturn(Optional.of(caseTypeEntity));

        when(getCaseTypeRolesRepository.findCaseTypeRoles(anyString()))
            .thenReturn(Sets.newHashSet("caseworker-divorce-master"));
        roleToAccessProfileService.createAccessProfileMapping("CaseType_1");
        verify(caseTypeRepository, times(1)).findCurrentVersionForReference(anyString());
        verify(getCaseTypeRolesRepository, times(1)).findCaseTypeRoles(anyString());
        verify(roleToAccessProfileRepository, times(1)).saveAll(anyIterable());
    }
}
