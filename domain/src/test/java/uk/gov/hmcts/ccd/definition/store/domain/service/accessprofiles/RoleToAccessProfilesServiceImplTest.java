package uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.RoleToAccessProfilesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleToAccessProfiles;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RoleToAccessProfilesServiceImplTest {

    private RoleToAccessProfilesServiceImpl classUnderTest;

    @Mock
    private RoleToAccessProfilesRepository repository;

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new RoleToAccessProfilesServiceImpl(repository, dtoMapper);
    }

    @Test
    @DisplayName(
        "Should get Role To Access Profile for the passed case type references")
    void shouldGetRoleToAccessProfilesForValidCaseTypeId() {
        List<RoleToAccessProfilesEntity> roleToAccessProfileEntities = Lists.newArrayList();
        roleToAccessProfileEntities.add(createRoleToAccessProfile("TestRole1", "judge"));
        roleToAccessProfileEntities.add(createRoleToAccessProfile("TestRole2", "solicitor"));
        doReturn(roleToAccessProfileEntities).when(repository).findByCaseTypeReference(anyList());
        List<String> references = Lists.newArrayList("Test", "Divorce");
        List<RoleToAccessProfiles> valuesReturned = classUnderTest.findByCaseTypeReferences(references);
        Assert.assertEquals(2, valuesReturned.size());
    }

    @Test
    @DisplayName(
        "Should get Role To Access Profile for the passed role name")
    void shouldGetRoleToAccessProfilesForValidRoleName() {
        List<RoleToAccessProfilesEntity> roleToAccessProfileEntities = Lists.newArrayList();
        roleToAccessProfileEntities.add(createRoleToAccessProfile("TestRole1", "judge"));
        roleToAccessProfileEntities.add(createRoleToAccessProfile("TestRole2", "solicitor"));
        doReturn(roleToAccessProfileEntities).when(repository).findByRoleNme(anyString());
        List<RoleToAccessProfiles> valuesReturned = classUnderTest.findByRoleName("Divorce");
        Assert.assertEquals(2, valuesReturned.size());
    }

    @Test
    @DisplayName(
        "Should get empty Role To Access Profiles list for the passed empty case type references")
    void shouldGetEmptyRoleToAccessProfilesListForEmptyCaseType() {
        List<RoleToAccessProfilesEntity> roleToAccessProfileEntities = Lists.newArrayList();
        doReturn(roleToAccessProfileEntities).when(repository).findByCaseTypeReference(anyList());
        List<RoleToAccessProfiles> valuesReturned = classUnderTest.findByCaseTypeReferences(Lists.newArrayList());
        Assert.assertEquals(0, valuesReturned.size());
    }

    @Test
    @DisplayName(
        "Should save the passed entities")
    void shouldSaveEntity() {
        RoleToAccessProfilesEntity roleToAccessProfilesEntity = mock(RoleToAccessProfilesEntity.class);
        List<RoleToAccessProfilesEntity> entitiesToSave = new ArrayList<>();
        entitiesToSave.add(roleToAccessProfilesEntity);
        classUnderTest.saveAll(entitiesToSave);
        verify(repository, times(1)).saveAll(eq(entitiesToSave));
    }

    private RoleToAccessProfilesEntity createRoleToAccessProfile(String roleName, String accessProfiles) {
        RoleToAccessProfilesEntity roleToAccessProfilesEntity = new RoleToAccessProfilesEntity();
        roleToAccessProfilesEntity.setCaseType(createCaseTypeEntity());
        roleToAccessProfilesEntity.setRoleName(roleName);
        roleToAccessProfilesEntity.setAccessProfiles(accessProfiles);
        roleToAccessProfilesEntity.setDisabled(false);
        roleToAccessProfilesEntity.setReadOnly(false);
        roleToAccessProfilesEntity.setAuthorisation("");
        return roleToAccessProfilesEntity;
    }

    private CaseTypeEntity createCaseTypeEntity() {
        CaseTypeEntity entity = new CaseTypeEntity();
        entity.setReference("TestCaseTypeRef");
        return entity;
    }
}
