package uk.gov.hmcts.ccd.definition.store.repository;

import com.vladmihalcea.sql.SQLStatementCountValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import jakarta.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
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

        createCaseType("id", 1, "Test case",
            "Some case type", testJurisdiction, null);
        createCaseType("id", 3, "Test case",
            "Some case type", testJurisdiction, null);
        createCaseType("id", 2, "Test case",
            "Some case type", testJurisdiction, null);

        StateEntity state = new StateEntity();
        state.setReference("s1");
        state.setName("State 1");
        state.setDescription("A description");

        createCaseType("id2", 1, "Test case 2",
            "Another case type", testJurisdiction, state);

        state = new StateEntity();
        state.setReference("s1");
        state.setName("State 1");
        state.setDescription("A description");
        createCaseType("id2", 2, "Test case 2",
            "Another case type", testJurisdiction, state);
    }


    @Test
    public void findByJurisdictionIdReturnsCurrentVersionOfCaseTypesWhenSeveralVersionsExist() {
        List<CaseTypeLiteEntity> caseTypeEntityOptional
            = classUnderTest.findByJurisdictionId(testJurisdiction.getReference());
        assertEquals(2, caseTypeEntityOptional.size());

        CaseTypeLiteEntity caseTypeJurisdictionIdVersionReferenceIdx1 = getCaseTypeLiteEntityByReference("id",
            caseTypeEntityOptional);
        assertEquals(3, caseTypeJurisdictionIdVersionReferenceIdx1.getVersion().intValue());
        assertEquals("Test case", caseTypeJurisdictionIdVersionReferenceIdx1.getName());
        assertEquals("Some case type", caseTypeJurisdictionIdVersionReferenceIdx1.getDescription());

        CaseTypeLiteEntity caseTypeJurisdictionIdVersionReferenceIdx2 = getCaseTypeLiteEntityByReference("id2",
            caseTypeEntityOptional);
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

    @Test
    public void checkSQLStatementCounts() {

        SQLStatementCountValidator.reset();

        List<CaseTypeLiteEntity> caseTypeEntityOptional
            = classUnderTest.findByJurisdictionId(testJurisdiction.getReference());

        // caseType entity only no child associations (lazy)
        SQLStatementCountValidator.assertSelectCount(1);

        CaseTypeLiteEntity caseTypeLiteEntity =
            getCaseTypeLiteEntityByReference("id2", caseTypeEntityOptional);


        assertEquals(1, caseTypeLiteEntity.getStates().size());

        SQLStatementCountValidator.assertSelectCount(3);
    }

    private CaseTypeLiteEntity getCaseTypeLiteEntityByReference(String reference,
                                                                List<CaseTypeLiteEntity> caseTypeEntityOptional) {
        return caseTypeEntityOptional.stream()
            .filter(c -> c.getReference().equals(reference))
                .findFirst().orElse(new CaseTypeLiteEntity());
    }

    private void saveCaseTypeClearAndFlushSession(CaseTypeEntity caseType) {
        caseTypeRepository.save(caseType);
        entityManager.flush();
        entityManager.clear();
    }

    private void createCaseType(String reference, int version, String name, String description,
                                JurisdictionEntity jurisdiction, StateEntity state) {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(reference);
        caseType.setName(name);
        caseType.setVersion(version);
        caseType.setDescription(description);
        caseType.setJurisdiction(jurisdiction);
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);
        if (state != null) {
            caseType.addState(state);
        }
        saveCaseTypeClearAndFlushSession(caseType);
    }
}
