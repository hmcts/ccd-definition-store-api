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
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class CaseTypeLiteRepositoryTest {

    @Autowired
    private CaseTypeLiteRepository classUnderTest;

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    private JurisdictionEntity testJurisdiction;

    @Before
    public void setUp() {
        this.testJurisdiction = testHelper.createJurisdiction();

        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("id");
        caseType.setName("Test case");
        caseType.setVersion(1);
        caseType.setDescription("Some case type");
        caseType.setJurisdiction(testJurisdiction);
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);
        saveCaseTypeClearAndFlushSession(caseType);
        caseType.setVersion(2);
        saveCaseTypeClearAndFlushSession(caseType);
        caseType.setVersion(3);
        saveCaseTypeClearAndFlushSession(caseType);

        final CaseTypeEntity caseType2 = new CaseTypeEntity();
        caseType2.setReference("id2");
        caseType2.setName("Test case 2");
        caseType2.setVersion(1);
        caseType2.setDescription("Another case type");
        caseType2.setJurisdiction(testJurisdiction);
        caseType2.setSecurityClassification(SecurityClassification.PUBLIC);
        final StateEntity state = new StateEntity();
        state.setReference("s1");
        state.setName("State 1");
        state.setDescription("A description");
        caseType2.addState(state);
        saveCaseTypeClearAndFlushSession(caseType2);
        caseType2.setVersion(2);
        saveCaseTypeClearAndFlushSession(caseType2);
    }

    @Test
    public void findByJurisdictionIdReturnsCurrentVersionOfCaseTypesWhenSeveralVersionsExist() {
        List<CaseTypeLiteEntity> caseTypeEntityOptional
            = classUnderTest.findByJurisdictionId(testJurisdiction.getReference());
        assertTrue(caseTypeEntityOptional.size() == 2);

        CaseTypeLiteEntity caseTypeJurisdictionIdVersionReferenceIdx1 = caseTypeEntityOptional.get(1);
        assertEquals(3, caseTypeJurisdictionIdVersionReferenceIdx1.getVersion().intValue());
        assertEquals("Test case", caseTypeJurisdictionIdVersionReferenceIdx1.getName());
        assertEquals("Some case type", caseTypeJurisdictionIdVersionReferenceIdx1.getDescription());

        CaseTypeLiteEntity caseTypeJurisdictionIdVersionReferenceIdx2 = caseTypeEntityOptional.get(0);
        assertEquals(2, caseTypeJurisdictionIdVersionReferenceIdx2.getVersion().intValue());
        assertEquals("Test case 2", caseTypeJurisdictionIdVersionReferenceIdx2.getName());
        assertEquals("Another case type", caseTypeJurisdictionIdVersionReferenceIdx2.getDescription());
        assertEquals(1, caseTypeJurisdictionIdVersionReferenceIdx2.getStates().size());
        assertEquals("s1", caseTypeJurisdictionIdVersionReferenceIdx2.getStates().get(0).getReference());
        assertEquals("State 1", caseTypeJurisdictionIdVersionReferenceIdx2.getStates().get(0).getName());
        assertEquals("A description", caseTypeJurisdictionIdVersionReferenceIdx2.getStates().get(0).getDescription());
    }

    @Test
    public void emptyListReturnedWhenNoCaseTypesForJurisdiction() {
        List<CaseTypeLiteEntity> caseTypeEntityOptional
            = classUnderTest.findByJurisdictionId("Non Existing Jurisdiction");
        assertTrue(caseTypeEntityOptional.isEmpty());
    }

    private void saveCaseTypeClearAndFlushSession(CaseTypeEntity caseType) {
        caseTypeRepository.save(caseType);
        entityManager.flush();
        entityManager.clear();
    }

}
