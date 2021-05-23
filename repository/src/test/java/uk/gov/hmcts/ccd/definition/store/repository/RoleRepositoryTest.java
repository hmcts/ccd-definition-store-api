package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.RESTRICTED;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class RoleRepositoryTest {

    @Autowired
    private CaseRoleRepository caseRoleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    private static final String CASE_TYPE_REFERENCE = "id";
    private static final String CASE_TYPE_NAME = "Case Type I";
    private static final String USER_ROLE_REFERENCE = "Some role reference";
    private static final String USER_ROLE_NAME = "Some user role";
    private static final String CASE_ROLE_REFERENCE = "[Claimant]";
    private static final String CASE_ROLE_NAME = "The claimant party";
    private static final String CASE_ROLE_DESCRIPTION = "Claimant Description";
    private static final String CASE_ROLE_REFERENCE_2 = "[Defendant]";
    private static final String CASE_ROLE_NAME_2 = "The defendant party";
    private static final String CASE_ROLE_DESCRIPTION_2 = "Defendant Description";

    private JurisdictionEntity testJurisdiction;

    private final CaseTypeEntity caseType = new CaseTypeEntity();

    @Before
    public void setUp() {
        this.testJurisdiction = testHelper.createJurisdiction();

        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(CASE_ROLE_REFERENCE);
        caseRoleEntity.setName(CASE_ROLE_NAME);
        caseRoleEntity.setDescription(CASE_ROLE_DESCRIPTION);
        caseRoleEntity.setSecurityClassification(PUBLIC);
        CaseRoleEntity caseRoleEntity2 = new CaseRoleEntity();
        caseRoleEntity2.setReference(CASE_ROLE_REFERENCE_2);
        caseRoleEntity2.setName(CASE_ROLE_NAME_2);
        caseRoleEntity2.setDescription(CASE_ROLE_DESCRIPTION_2);
        caseRoleEntity2.setSecurityClassification(PUBLIC);

        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setReference(USER_ROLE_REFERENCE);
        userRoleEntity.setName(USER_ROLE_NAME);
        userRoleEntity.setSecurityClassification(PUBLIC);


        caseType.setReference(CASE_TYPE_REFERENCE);
        caseType.setName(CASE_TYPE_NAME);
        caseType.setVersion(1);
        caseType.setJurisdiction(testJurisdiction);
        caseType.addCaseRole(caseRoleEntity);
        caseType.addCaseRoles(Arrays.asList(caseRoleEntity2));
        caseType.setSecurityClassification(PUBLIC);
        saveCaseTypeClearAndFlushSession(caseType, userRoleEntity);
    }

    @Test
    public void shouldFindCaseRolesByCaseType() {
        List<CaseRoleEntity> caseRoleEntities = caseRoleRepository.findCaseRoleEntitiesByCaseType(CASE_TYPE_REFERENCE);
        assertThat(caseRoleEntities.size(), is(2));
        assertThat(caseRoleEntities.get(0).getReference(), is(CASE_ROLE_REFERENCE.toUpperCase()));
        assertThat(caseRoleEntities.get(0).getName(), is(CASE_ROLE_NAME));
        assertThat(caseRoleEntities.get(0).getDescription(), is(CASE_ROLE_DESCRIPTION));
        assertThat(caseRoleEntities.get(0).getCaseType().getReference(), is(CASE_TYPE_REFERENCE));
        assertThat(caseRoleEntities.get(1).getReference(), is(CASE_ROLE_REFERENCE_2.toUpperCase()));
        assertThat(caseRoleEntities.get(1).getName(), is(CASE_ROLE_NAME_2));
        assertThat(caseRoleEntities.get(1).getDescription(), is(CASE_ROLE_DESCRIPTION_2));
        assertThat(caseRoleEntities.get(1).getCaseType().getReference(), is(CASE_TYPE_REFERENCE));
    }

    @Test
    public void shouldFindNoCaseRolesForInvalidCaseType() {
        List<CaseRoleEntity> caseRoleEntities = caseRoleRepository.findCaseRoleEntitiesByCaseType("InvalidCaseType");
        assertThat(caseRoleEntities.size(), is(0));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void shouldFailWhenCreateDuplicateCaseRoles() {
        final CaseRoleEntity entity = new CaseRoleEntity();
        entity.setReference(CASE_ROLE_REFERENCE);
        entity.setSecurityClassification(RESTRICTED);
        caseRoleRepository.save(entity);
    }

    @Test
    public void shouldFindCaseRoles() {
        List<CaseRoleEntity> caseRoleEntities = caseRoleRepository.findAll();
        assertThat(caseRoleEntities.size(), is(2));
        assertThat(caseRoleEntities.get(0).getReference(), is(CASE_ROLE_REFERENCE.toUpperCase()));
    }

    @Test
    public void shouldFindSingleUserRole() {
        List<UserRoleEntity> userRoleEntities = userRoleRepository.findAll();
        assertThat(userRoleEntities.size(), is(1));
        assertThat(userRoleEntities.get(0).getReference(), is(USER_ROLE_REFERENCE));
    }

    @Test
    public void shouldFindUserRole() {
        final UserRoleEntity role = userRoleRepository.findTopByReference(USER_ROLE_REFERENCE).get();
        assertThat(role.getId(), is(notNullValue()));
        assertThat(role.getCreatedAt(), is(notNullValue()));
        assertThat(role.getReference(), is(USER_ROLE_REFERENCE));
        assertThat(role.getSecurityClassification(), is(PUBLIC));
    }

    @Test
    public void shouldFindNoUserRoleEntity() {
        final Optional<UserRoleEntity> role = userRoleRepository.findTopByReference("unknown role reference");
        assertThat(role, isEmpty());
    }

    @Test
    public void shouldCreateUserRole() {
        final String role = "a new role reference";
        final UserRoleEntity entity = new UserRoleEntity();
        entity.setReference(role);
        entity.setName("SOME_ROLE");
        entity.setSecurityClassification(RESTRICTED);
        userRoleRepository.save(entity);

        entityManager.flush();
        entityManager.clear();

        final UserRoleEntity afterSave = userRoleRepository.findTopByReference(role).get();
        assertThat(afterSave.getReference(), is(role));
        assertThat(afterSave.getSecurityClassification(), is(RESTRICTED));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void shouldFailWhenCreateDuplicateUserRoles() {
        final UserRoleEntity entity = new UserRoleEntity();
        entity.setReference("xyz = '3'");
        entity.setSecurityClassification(RESTRICTED);
        userRoleRepository.save(entity);
    }

    private void saveCaseTypeClearAndFlushSession(CaseTypeEntity caseType, UserRoleEntity userRole) {
        caseTypeRepository.save(caseType);
        userRoleRepository.save(userRole);
        entityManager.flush();
        entityManager.clear();
    }
}
