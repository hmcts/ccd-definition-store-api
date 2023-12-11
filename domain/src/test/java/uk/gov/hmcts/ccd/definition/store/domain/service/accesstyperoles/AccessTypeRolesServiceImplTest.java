package uk.gov.hmcts.ccd.definition.store.domain.service.accesstyperoles;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesField;


import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

public class AccessTypeRolesServiceImplTest {

    private AccessTypeRolesServiceImpl classUnderTest;

    @Mock
    private AccessTypeRolesRepository repository;

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new AccessTypeRolesServiceImpl(repository, dtoMapper);
    }

    @Test
    @DisplayName(
        "Should save the passed entities")
    void shouldSaveEntity() {
        AccessTypeRolesEntity accessTypeRolesEntity = mock(AccessTypeRolesEntity.class);
        List<AccessTypeRolesEntity> entitiesToSave = new ArrayList<>();
        entitiesToSave.add(accessTypeRolesEntity);
        classUnderTest.saveAll(entitiesToSave);
        verify(repository, times(1)).saveAll(eq(entitiesToSave));
    }

    @Test
    @DisplayName("Should get AccessType Roles for the passed organisation profile ID")
    void shouldGetRoleToAccessProfilesForValidRoleName() {
        List<AccessTypeRolesEntity> accessTypeRolesEntityEntities = Lists.newArrayList();
        accessTypeRolesEntityEntities.add(createAccessTypeRoles("TestRole1", "judge"));
        accessTypeRolesEntityEntities.add(createAccessTypeRoles("TestRole2", "solicitor"));
        doReturn(accessTypeRolesEntityEntities).when(repository).findByOrganisationProfileIds(anyList());
        List<AccessTypeRolesField> valuesReturned = classUnderTest.findByOrganisationProfileId("solicitor");
        Assert.assertEquals(0, valuesReturned.size());
    }

    private AccessTypeRolesEntity createAccessTypeRoles(String roleName, String organisationProfileId) {
        AccessTypeRolesEntity  accessTypeRolesEntityEntity = new AccessTypeRolesEntity();
        accessTypeRolesEntityEntity.setCaseTypeId(createCaseTypeEntity());
        accessTypeRolesEntityEntity.setOrganisationalRoleName(roleName);
        accessTypeRolesEntityEntity.setOrganisationProfileId(organisationProfileId);
        accessTypeRolesEntityEntity.setCaseAccessGroupIdTemplate("groupTemplate");
        accessTypeRolesEntityEntity.setGroupAccessEnabled(false);
        accessTypeRolesEntityEntity.setAccessTypeId("accessTypeId");
        return accessTypeRolesEntityEntity;
    }

    private CaseTypeEntity createCaseTypeEntity() {
        CaseTypeEntity entity = new CaseTypeEntity();
        entity.setReference("TestCaseTypeRef");
        return entity;
    }
}
