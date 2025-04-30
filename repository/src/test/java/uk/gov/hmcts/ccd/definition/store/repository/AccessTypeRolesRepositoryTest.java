package uk.gov.hmcts.ccd.definition.store.repository;

import org.hamcrest.core.Is;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity.toCaseTypeLiteEntity;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class AccessTypeRolesRepositoryTest {

    @Autowired
    private AccessTypeRolesRepository accessTypeRolesRespository;

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    @Test
    public void saveAndRetrieveAccessTypeRoles() {

        final CaseTypeEntity caseType = createCaseTypeEntity();
        saveCaseType(caseType);

        final AccessTypeRoleEntity accessTypeRoles = createAccessTypeRolesEntity(caseType);
        saveAccessTypeRoles(accessTypeRoles);

        List<AccessTypeRoleEntity> accessTypeRolesList = accessTypeRolesRespository.findAllWithCaseTypeIds();
        assertThat(accessTypeRolesList.size(), is(1));

        AccessTypeRoleEntity result = accessTypeRolesList.get(0);
        assertThat(result.getId(), Is.is(1));
        assertThat(result.getLiveFrom(), Is.is(LocalDate.of(2023, Month.FEBRUARY, 12)));
        assertThat(result.getLiveTo(), Is.is(LocalDate.of(2027, Month.OCTOBER, 17)));
        assertThat(result.getCaseType().getId(), Is.is(caseType.getId()));
        assertThat(result.getAccessTypeId(), Is.is("some access type id"));
        assertThat(result.getOrganisationProfileId(), Is.is("some org profile id"));

    }

    @NotNull
    private CaseTypeEntity createCaseTypeEntity() {
        final JurisdictionEntity testJurisdiction = testHelper.createJurisdiction();

        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("id");
        caseType.setName("Test case");
        caseType.setVersion(1);
        caseType.setDescription("Some case type");
        caseType.setJurisdiction(testJurisdiction);
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);
        return caseType;
    }

    @NotNull
    private AccessTypeRoleEntity createAccessTypeRolesEntity(CaseTypeEntity caseType) {
        final AccessTypeRoleEntity accessTypeRoles = new AccessTypeRoleEntity();
        accessTypeRoles.setLiveFrom(LocalDate.of(2023, Month.FEBRUARY, 12));
        accessTypeRoles.setLiveTo(LocalDate.of(2027, Month.OCTOBER, 17));
        accessTypeRoles.setCaseType(toCaseTypeLiteEntity(caseType));
        accessTypeRoles.setAccessTypeId("some access type id");
        accessTypeRoles.setOrganisationProfileId("some org profile id");
        accessTypeRoles.setOrganisationalRoleName("some org role name");
        accessTypeRoles.setGroupRoleName("some group role name");
        accessTypeRoles.setCaseAssignedRoleField("some case assigned role field");
        accessTypeRoles.setGroupAccessEnabled(true);
        accessTypeRoles.setCaseAccessGroupIdTemplate("some access group id template");
        return accessTypeRoles;
    }

    private void saveCaseType(CaseTypeEntity caseType) {
        caseTypeRepository.save(caseType);
        entityManager.flush();
        entityManager.clear();
    }

    private void saveAccessTypeRoles(AccessTypeRoleEntity accessTypeRoles) {
        accessTypeRolesRespository.save(accessTypeRoles);
        entityManager.flush();
        entityManager.clear();
    }
}
