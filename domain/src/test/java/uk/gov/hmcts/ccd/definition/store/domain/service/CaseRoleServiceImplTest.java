package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.repository.CaseRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseRole;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

class CaseRoleServiceImplTest {
    private final String CASE_TYPE_REFERENCE = "Case Type 1";
    @Mock
    EntityToResponseDTOMapper dtoMapper;
    @Mock
    private CaseRoleRepository caseRoleRepository;
    @Mock
    private CaseTypeRepository caseTypeRepository;

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
        classUnderTest = new CaseRoleServiceImpl(caseRoleRepository, caseTypeRepository, dtoMapper);
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
        doReturn(Optional.of(1)).when(caseTypeRepository).findLastVersion(CASE_TYPE_REFERENCE);

        final List<CaseRole> caseRoles = classUnderTest.findByCaseTypeId(CASE_TYPE_REFERENCE);

        assertAll(
            () -> assertThat(caseRoles.size(), is(3)),
            () -> assertThat(caseRoles.get(0).getId(), is(caseRoleEntity1.getReference()))
        );
    }

    @DisplayName("should throw exception when case type not found")
    @Test
    void invalidCaseTypeId() {
        doThrow(new NotFoundException("Not found")).when(caseTypeRepository).findLastVersion(CASE_TYPE_REFERENCE);

        assertThrows(NotFoundException.class, () -> {
            final List<CaseRole> caseRoles = classUnderTest.findByCaseTypeId(CASE_TYPE_REFERENCE);
        });
    }

    @DisplayName("should determine if the reference is a caseRole")
    @Test
    void isCaseRole() {
        assertAll(
            () -> assertThat(CaseRoleServiceImpl.isCaseRole(null), is(false)),
            () -> assertThat(CaseRoleServiceImpl.isCaseRole(""), is(false)),
            () -> assertThat(CaseRoleServiceImpl.isCaseRole("[]"), is(false)),
            () -> assertThat(CaseRoleServiceImpl.isCaseRole("caseworker-test"), is(false)),
            () -> assertThat(CaseRoleServiceImpl.isCaseRole("[anything]"), is(true))
        );
    }
}
