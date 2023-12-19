package uk.gov.hmcts.ccd.definition.store.repository;

import org.hamcrest.core.Is;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
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
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    private CaseTypeEntity caseType;

    @Test
    public void saveAndRetrieveAccessTypeRoles() {
        caseType = testHelper.createCaseType("some case type", "desc");
        createAccessTypeRolesEntity(caseType);

        entityManager.flush();
        entityManager.clear();

        List<AccessTypeRolesEntity> accessTypeRolesList = accessTypeRolesRespository.findAllWithCaseTypeIds();
        assertThat(accessTypeRolesList.size(), is(1));

        AccessTypeRolesEntity result = accessTypeRolesList.get(0);
        assertThat(result.getId(), Is.is(1));
        assertThat(result.getLiveFrom(), Is.is(LocalDate.of(2023, Month.FEBRUARY, 12)));
        assertThat(result.getLiveTo(), Is.is(LocalDate.of(2027, Month.OCTOBER, 17)));
        assertThat(result.getCaseTypeId().getId(), Is.is(caseType.getId()));
        assertThat(result.getAccessTypeId(), Is.is("access type id 1"));
        assertThat(result.getOrganisationProfileId(), Is.is("organisationProfileId_1"));
        assertThat(result.getOrganisationPolicyField(), Is.is("some policy field"));
        assertThat(result.getAccessMandatory(), Is.is(true));
    }

    @Test
    public void saveAndRetrieveAccessTypeRolesByOrganisationProfileId() {
        caseType = testHelper.createCaseType("some case type", "desc");
        createAccessTypeRolesEntity(caseType);
        createAccessTypeRolesEntity2(caseType);

        entityManager.flush();
        entityManager.clear();

        List<String> organisationProfileIds = List.of(new String[]{"organisationProfileId_2"});
        List<AccessTypeRolesEntity> accessTypeRolesList = accessTypeRolesRespository
            .findByOrganisationProfileIds(organisationProfileIds);
        assertThat(accessTypeRolesList.size(), is(1));

        AccessTypeRolesEntity result = accessTypeRolesList.get(0);
        assertThat(result.getOrganisationProfileId(), Is.is("organisationProfileId_2"));
    }

    @NotNull
    private AccessTypeRolesEntity createAccessTypeRolesEntity(CaseTypeEntity caseType) {
        final AccessTypeRolesEntity accessTypeRoles = new AccessTypeRolesEntity();
        accessTypeRoles.setLiveFrom(LocalDate.of(2023, Month.FEBRUARY, 12));
        accessTypeRoles.setLiveTo(LocalDate.of(2027, Month.OCTOBER, 17));
        accessTypeRoles.setCaseTypeId(caseType);
        accessTypeRoles.setAccessTypeId("access type id 1");
        accessTypeRoles.setOrganisationProfileId("organisationProfileId_1");
        accessTypeRoles.setAccessMandatory(true);
        accessTypeRoles.setAccessDefault(true);
        accessTypeRoles.setDisplay(true);
        accessTypeRoles.setDescription("some description");
        accessTypeRoles.setHint("some hint");
        accessTypeRoles.setDisplayOrder(1);
        accessTypeRoles.setOrganisationalRoleName("some org role name");
        accessTypeRoles.setGroupRoleName("some group role name");
        accessTypeRoles.setOrganisationPolicyField("some policy field");
        accessTypeRoles.setGroupAccessEnabled(true);
        accessTypeRoles.setCaseAccessGroupIdTemplate("some access group id template");
        return accessTypeRolesRespository.save(accessTypeRoles);
    }

    @NotNull
    private AccessTypeRolesEntity createAccessTypeRolesEntity2(CaseTypeEntity caseType) {
        final AccessTypeRolesEntity accessTypeRoles = new AccessTypeRolesEntity();
        accessTypeRoles.setLiveFrom(LocalDate.of(2023, Month.FEBRUARY, 12));
        accessTypeRoles.setLiveTo(LocalDate.of(2027, Month.OCTOBER, 17));
        accessTypeRoles.setCaseTypeId(caseType);
        accessTypeRoles.setAccessTypeId("access type id 2");
        accessTypeRoles.setOrganisationProfileId("organisationProfileId_2");
        accessTypeRoles.setAccessMandatory(true);
        accessTypeRoles.setAccessDefault(true);
        accessTypeRoles.setDisplay(true);
        accessTypeRoles.setDescription("some description");
        accessTypeRoles.setHint("some hint");
        accessTypeRoles.setDisplayOrder(1);
        accessTypeRoles.setOrganisationalRoleName("some org role name");
        accessTypeRoles.setGroupRoleName("some group role name");
        accessTypeRoles.setOrganisationPolicyField("some policy field");
        accessTypeRoles.setGroupAccessEnabled(true);
        accessTypeRoles.setCaseAccessGroupIdTemplate("some access group id template");
        return accessTypeRolesRespository.save(accessTypeRoles);
    }
}
