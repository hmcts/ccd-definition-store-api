package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class CaseRoleRepositoryTest {

    @Autowired
    private CaseRoleRepository classUnderTest;

    @Autowired
    CaseTypeRepository caseTypeRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    private static final String CASE_TYPE_REFERENCE = "id";
    private static final String CASE_ROLE_REFERENCE = "[Claimant]";
    private static final String CASE_ROLE_NAME = "The claimant party";
    private static final String CASE_ROLE_DESCRIPTION = "Claimant Description";
    private static final String CASE_ROLE_REFERENCE_2 = "[Defendant]";
    private static final String CASE_ROLE_NAME_2 = "The defendant party";
    private static final String CASE_ROLE_DESCRIPTION_2 = "Defendant Description";

    private JurisdictionEntity testJurisdiction;

    @Before
    public void setUp() {
        this.testJurisdiction = testHelper.createJurisdiction();

        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(CASE_ROLE_REFERENCE);
        caseRoleEntity.setName(CASE_ROLE_NAME);
        caseRoleEntity.setDescription(CASE_ROLE_DESCRIPTION);
        CaseRoleEntity caseRoleEntity2 = new CaseRoleEntity();
        caseRoleEntity2.setReference(CASE_ROLE_REFERENCE_2);
        caseRoleEntity2.setName(CASE_ROLE_NAME_2);
        caseRoleEntity2.setDescription(CASE_ROLE_DESCRIPTION_2);

        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_REFERENCE);
        caseType.setName("Test case");
        caseType.setVersion(1);
        caseType.setJurisdiction(testJurisdiction);
        caseType.addCaseRole(caseRoleEntity);
        caseType.addCaseRoles(Arrays.asList(caseRoleEntity2));
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);
        saveCaseTypeClearAndFlushSession(caseType);
    }

    @Test
    public void caseRoleRepositoryFindsCasRolesByCaseType() {
        List<CaseRoleEntity> caseRoleEntities
            = classUnderTest.findCaseRoleEntitiesByCaseType(CASE_TYPE_REFERENCE);
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

    private void saveCaseTypeClearAndFlushSession(CaseTypeEntity caseType) {
        caseTypeRepository.save(caseType);
        entityManager.flush();
        entityManager.clear();
    }
}
