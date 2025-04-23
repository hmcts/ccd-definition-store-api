package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.RESTRICTED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class AccessProfileRepositoryTest {

    @Autowired
    private CaseRoleRepository caseRoleRepository;

    @Autowired
    private AccessProfileRepository accessProfileRepository;

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    private static final String CASE_TYPE_REFERENCE = "id";
    private static final String CASE_TYPE_NAME = "Case Type I";
    private static final String ACCESS_PROFILE_REFERENCE = "Some access profile reference";
    private static final String ACCESS_PROFILE_NAME = "Some access profile";
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

        AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
        accessProfileEntity.setReference(ACCESS_PROFILE_REFERENCE);
        accessProfileEntity.setName(ACCESS_PROFILE_NAME);
        accessProfileEntity.setSecurityClassification(PUBLIC);


        caseType.setReference(CASE_TYPE_REFERENCE);
        caseType.setName(CASE_TYPE_NAME);
        caseType.setVersion(1);
        caseType.setJurisdiction(testJurisdiction);
        caseType.addCaseRole(caseRoleEntity);
        caseType.addCaseRoles(Arrays.asList(caseRoleEntity2));
        caseType.setSecurityClassification(PUBLIC);
        saveCaseTypeClearAndFlushSession(caseType, accessProfileEntity);
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
        caseRoleRepository.saveAndFlush(entity);
    }

    @Test
    public void shouldFindCaseRoles() {
        List<CaseRoleEntity> caseRoleEntities = caseRoleRepository.findAll();
        assertThat(caseRoleEntities.size(), is(2));
        assertThat(caseRoleEntities.get(0).getReference(), is(CASE_ROLE_REFERENCE.toUpperCase()));
    }

    @Test
    public void shouldFindSingleAccessProfile() {
        List<AccessProfileEntity> accessProfileEntities = accessProfileRepository.findAll();
        assertThat(accessProfileEntities.size(), is(1));
        assertThat(accessProfileEntities.get(0).getReference(), is(ACCESS_PROFILE_REFERENCE));
    }

    @Test
    public void shouldFindAccessProfile() {
        final AccessProfileEntity entity = accessProfileRepository.findTopByReference(ACCESS_PROFILE_REFERENCE).get();
        assertThat(entity.getId(), is(notNullValue()));
        assertThat(entity.getCreatedAt(), is(notNullValue()));
        assertThat(entity.getReference(), is(ACCESS_PROFILE_REFERENCE));
        assertThat(entity.getSecurityClassification(), is(PUBLIC));
    }

    @Test
    public void shouldFindNoAccessProfileEntity() {
        final Optional<AccessProfileEntity> entity =
            accessProfileRepository.findTopByReference("unknown access profile reference");
        assertThat(entity, isEmpty());
    }

    @Test
    public void shouldCreateAccessProfile() {
        final String accessProfile = "a new access profile reference";
        final AccessProfileEntity entity = new AccessProfileEntity();
        entity.setReference(accessProfile);
        entity.setName("SOME_ACCESS_PROFILE");
        entity.setSecurityClassification(RESTRICTED);
        accessProfileRepository.save(entity);

        entityManager.flush();
        entityManager.clear();

        final AccessProfileEntity afterSave = accessProfileRepository.findTopByReference(accessProfile).get();
        assertThat(afterSave.getReference(), is(accessProfile));
        assertThat(afterSave.getSecurityClassification(), is(RESTRICTED));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void shouldFailWhenCreateDuplicateAccessProfiles() {
        final AccessProfileEntity entity = new AccessProfileEntity();
        entity.setReference("xyz = '3'");
        entity.setSecurityClassification(RESTRICTED);
        accessProfileRepository.saveAndFlush(entity);
    }

    private void saveCaseTypeClearAndFlushSession(CaseTypeEntity caseType, AccessProfileEntity accessProfile) {
        caseTypeRepository.save(caseType);
        accessProfileRepository.save(accessProfile);
        entityManager.flush();
        entityManager.clear();
    }
}
