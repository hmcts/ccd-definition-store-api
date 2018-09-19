package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.repository.CaseRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseRole;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;

class CaseRoleServiceImplTest {
    private final String CASE_TYPE_REFERENCE = "Case Type 1";
    @Mock
    EntityToResponseDTOMapper dtoMapper;
    @Mock
    private CaseRoleRepository caseRoleRepository;

    private CaseRoleServiceImpl classUnderTest;
    private CaseRoleEntity caseRoleEntity1 = new CaseRoleEntity();
    private CaseRoleEntity caseRoleEntity2 = new CaseRoleEntity();
    private CaseRoleEntity caseRoleEntity3 = new CaseRoleEntity();
    private CaseRole caseRole1 =  new CaseRole();
    private CaseRole caseRole2 = new CaseRole();
    private CaseRole caseRole3 = new CaseRole();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new CaseRoleServiceImpl(caseRoleRepository, dtoMapper);
        caseRoleEntity1.setReference("ref1");
        caseRole1.setId(caseRoleEntity1.getReference());
        doReturn(Arrays.asList(caseRoleEntity1, caseRoleEntity2, caseRoleEntity3))
            .when(caseRoleRepository)
            .findCaseRoleEntitiesByCaseType(CASE_TYPE_REFERENCE);
        doReturn(caseRole1).when(dtoMapper).map(caseRoleEntity1);
        doReturn(caseRole2).when(dtoMapper).map(caseRoleEntity2);
        doReturn(caseRole3).when(dtoMapper).map(caseRoleEntity3);
    }

    @DisplayName("should return Case Role List")
    @Test
    void findByCaseTypeId() {
        final List<CaseRole> caseRoles = classUnderTest.findByCaseTypeId(CASE_TYPE_REFERENCE);

        assertAll(
            () -> assertThat(caseRoles.size(), is(3)),
            () -> assertThat(caseRoles.get(0).getId(), is(caseRoleEntity1.getReference()))
        );
    }
}
