package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class CaseTypeRepositoryTest {

    @Autowired
    private CaseTypeRepository classUnderTest;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    private static final String CASE_TYPE_REFERENCE = "id";

    private JurisdictionEntity testJurisdiction;

    @Before
    public void setUp() {
        this.testJurisdiction = testHelper.createJurisdiction();

        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("id");
        caseType.setName("Test case");
        caseType.setVersion(1);
        caseType.setJurisdiction(testJurisdiction);
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);
        saveCaseTypeClearAndFlushSession(caseType);
        caseType.setVersion(2);
        saveCaseTypeClearAndFlushSession(caseType);
        caseType.setVersion(3);
        saveCaseTypeClearAndFlushSession(caseType);
    }

    @Test
    public void severalVersionsOfCaseTypeExistForReference_findCurrentCaseTypeReturnsCurrentVersionOfCaseType() {
        Optional<CaseTypeEntity> caseTypeEntityOptional
            = classUnderTest.findCurrentVersionForReference(CASE_TYPE_REFERENCE);
        assertTrue(caseTypeEntityOptional.isPresent());
        assertEquals(3, caseTypeEntityOptional.get().getVersion().intValue());
    }

    @Test
    public void caseTypeDoesNotExistForReference_emptyOptionalReturned() {
        Optional<CaseTypeEntity> caseTypeEntityOptional
            = classUnderTest.findCurrentVersionForReference("Non Existing Reference");
        assertFalse(caseTypeEntityOptional.isPresent());
    }

    @Test
    public void severalVersionsOfCaseTypeExistForJurisdiction_findByJurisdictionIdReturnsCurrentVersionOfCaseTypeForJurisdiction() {
        List<CaseTypeEntity> caseTypeEntityOptional
            = classUnderTest.findByJurisdictionId(testJurisdiction.getReference());
        assertTrue(caseTypeEntityOptional.size() == 1);
        assertEquals(3, caseTypeEntityOptional.get(0).getVersion().intValue());
    }

    @Test
    public void caseTypeDoesNotExistForJurisdiction_emptyListReturned() {
        List<CaseTypeEntity> caseTypeEntityOptional
            = classUnderTest.findByJurisdictionId("Non Existing Jurisdiction");
        assertTrue(caseTypeEntityOptional.isEmpty());
    }

    private void saveCaseTypeClearAndFlushSession(CaseTypeEntity caseType) {
        classUnderTest.save(caseType);
        entityManager.flush();
        entityManager.clear();
    }

}
