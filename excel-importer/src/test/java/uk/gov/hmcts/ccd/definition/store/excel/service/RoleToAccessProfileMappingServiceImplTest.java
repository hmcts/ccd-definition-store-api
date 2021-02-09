package uk.gov.hmcts.ccd.definition.store.excel.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.RoleToAccessProfileRepository;
import uk.gov.hmcts.ccd.definition.store.repository.accessprofile.GetCaseTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfileEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class RoleToAccessProfileMappingServiceImplTest {

    @Mock
    private GetCaseTypeRolesRepository getCaseTypeRolesRepository;

    @Mock
    private RoleToAccessProfileRepository roleToAccessProfileRepository;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @Mock
    private ApplicationParams applicationParams;

    @InjectMocks
    private RoleToAccessProfileMappingServiceImpl roleToAccessProfileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        roleToAccessProfileService = new RoleToAccessProfileMappingServiceImpl(getCaseTypeRolesRepository,
            caseTypeRepository,
            roleToAccessProfileRepository,
            applicationParams);
    }

    @Test
    @DisplayName("Should not invoke case type validation when role to access profile mapping not enabled")
    void shouldNotInvokeCaseTypeValidation() {
        when(applicationParams.isRoleToAccessProfileMapping()).thenReturn(false);
        roleToAccessProfileService.createAccessProfileMapping(Sets.newHashSet("CaseType_1"));
        verify(caseTypeRepository, times(0)).findCurrentVersionForReference(anyString());
    }

    @Test
    @DisplayName("Should invoke case type validation when role to access profile mapping not enabled")
    void shouldInvokeCaseTypeValidationWhenRoleToAccessProfileMappingEnabled() {
        when(applicationParams.isRoleToAccessProfileMapping()).thenReturn(true);
        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        when(caseTypeRepository.findCurrentVersionForReference(anyString()))
            .thenReturn(Optional.of(caseTypeEntity));
        roleToAccessProfileService.createAccessProfileMapping(Sets.newHashSet("CaseType_1"));
        verify(caseTypeRepository, times(1)).findCurrentVersionForReference(anyString());
    }

    @Test
    @DisplayName("Should throw exception when case type not found")
    void shouldThrowNotFoundExceptionWhenCaseTypeNotFound() {
        when(applicationParams.isRoleToAccessProfileMapping()).thenReturn(true);
        when(caseTypeRepository.findCurrentVersionForReference(anyString())).thenReturn(Optional.empty());
        roleToAccessProfileService.createAccessProfileMapping(Sets.newHashSet("CaseType_1"));
        verify(caseTypeRepository, times(1)).findCurrentVersionForReference(anyString());
        verify(getCaseTypeRolesRepository, times(0)).findCaseTypeRoles(anyInt());
    }

    @Test
    @DisplayName("Should invoke Get Case Type Roles")
    void shouldInvokeGetCaseTypeRolesWhenCaseTypeExists() {
        when(applicationParams.isRoleToAccessProfileMapping()).thenReturn(true);
        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        when(caseTypeRepository.findCurrentVersionForReference(anyString())).thenReturn(Optional.of(caseTypeEntity));
        roleToAccessProfileService.createAccessProfileMapping(Sets.newHashSet("CaseType_1"));
        verify(caseTypeRepository, times(1)).findCurrentVersionForReference(anyString());
        verify(getCaseTypeRolesRepository, times(1)).findCaseTypeRoles(anyInt());
    }

    @Test
    @DisplayName("Should invoke save on role to access profiles when Case Type Roles exists")
    void shouldInvokeSaveRoleToAccessProfilesGetCaseTypeRolesWhenCaseTypeExists() {
        when(applicationParams.isRoleToAccessProfileMapping()).thenReturn(true);
        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        when(caseTypeRepository.findCurrentVersionForReference(anyString())).thenReturn(Optional.of(caseTypeEntity));

        when(getCaseTypeRolesRepository.findCaseTypeRoles(anyInt()))
            .thenReturn(Sets.newHashSet("caseworker-divorce-master",
                "caseworker-divorce-master-1",
                "caseworker-divorce-master-2"));

        RoleToAccessProfileEntity roleToAccessProfileEntity = mock(RoleToAccessProfileEntity.class);
        when(roleToAccessProfileEntity.getRoleName()).thenReturn("idam:caseworker-divorce-master-1");

        RoleToAccessProfileEntity roleToAccessProfileEntity2 = mock(RoleToAccessProfileEntity.class);
        when(roleToAccessProfileEntity2.getRoleName()).thenReturn("idam:caseworker-divorce-master-2");

        when(roleToAccessProfileRepository.findByCaseTypeReference(anyList()))
            .thenReturn(Lists.newArrayList(roleToAccessProfileEntity, roleToAccessProfileEntity2));

        roleToAccessProfileService.createAccessProfileMapping(Sets.newHashSet("CaseType_1"));
        verify(caseTypeRepository, times(1)).findCurrentVersionForReference(anyString());
        verify(getCaseTypeRolesRepository, times(1)).findCaseTypeRoles(anyInt());
        verify(roleToAccessProfileRepository, times(1)).findByCaseTypeReference(anyList());

        ArgumentCaptor<List> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleToAccessProfileRepository, times(1)).deleteAll(deleteCaptor.capture());
        assertEquals(2, deleteCaptor.getValue().size());

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(roleToAccessProfileRepository, times(1)).saveAll(captor.capture());
        assertEquals(3, captor.getValue().size());
    }

    @Test
    @DisplayName("Should not invoke isRoleToAccessProfileMapping when case type set is null")
    void shouldInvokeRoleToAccessProfileMappingOnApplicationParamsNull() {
        invokeRoleMappingAndValidate(null);

    }

    @Test
    @DisplayName("Should not invoke isRoleToAccessProfileMapping when case type set is empty")
    void shouldInvokeRoleToAccessProfileMappingOnApplicationParamsEmpty() {
        invokeRoleMappingAndValidate(Sets.newHashSet());
    }

    private void invokeRoleMappingAndValidate(Set<String> caseTypeIds) {
        when(applicationParams.isRoleToAccessProfileMapping()).thenReturn(true);
        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        when(caseTypeRepository.findCurrentVersionForReference(anyString())).thenReturn(Optional.of(caseTypeEntity));

        CaseTypeEntity entity1 = mock(CaseTypeEntity.class);
        when(entity1.getReference()).thenReturn("CaseType_1");

        CaseTypeEntity entity2 = mock(CaseTypeEntity.class);
        when(entity2.getReference()).thenReturn("CaseType_2");

        when(caseTypeRepository.findAllLatestVersions()).thenReturn(Lists.newArrayList(entity1, entity2));


        when(getCaseTypeRolesRepository.findCaseTypeRoles(anyInt()))
            .thenReturn(Sets.newHashSet("caseworker-divorce-master",
                "caseworker-divorce-master-1",
                "caseworker-divorce-master-2",
                "[DEFENDENT]"));

        RoleToAccessProfileEntity roleToAccessProfileEntity = mock(RoleToAccessProfileEntity.class);
        when(roleToAccessProfileEntity.getRoleName()).thenReturn("idam:caseworker-divorce-master-1");

        RoleToAccessProfileEntity roleToAccessProfileEntity2 = mock(RoleToAccessProfileEntity.class);
        when(roleToAccessProfileEntity2.getRoleName()).thenReturn("idam:caseworker-divorce-master-2");

        RoleToAccessProfileEntity roleToAccessProfileEntity3 = mock(RoleToAccessProfileEntity.class);
        when(roleToAccessProfileEntity3.getRoleName()).thenReturn("[DEFENDENT]");

        when(roleToAccessProfileRepository.findByCaseTypeReference(anyList()))
            .thenReturn(Lists.newArrayList(roleToAccessProfileEntity,
                roleToAccessProfileEntity2,
                roleToAccessProfileEntity3));

        roleToAccessProfileService.createAccessProfileMapping(caseTypeIds);

        verify(caseTypeRepository, times(2)).findCurrentVersionForReference(anyString());
        verify(getCaseTypeRolesRepository, times(2)).findCaseTypeRoles(anyInt());
        verify(roleToAccessProfileRepository, times(2)).findByCaseTypeReference(anyList());

        ArgumentCaptor<List> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleToAccessProfileRepository, times(2)).deleteAll(deleteCaptor.capture());
        assertEquals(3, deleteCaptor.getValue().size());


        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(roleToAccessProfileRepository, times(2)).saveAll(captor.capture());
        assertEquals(4, captor.getValue().size());
    }
}
