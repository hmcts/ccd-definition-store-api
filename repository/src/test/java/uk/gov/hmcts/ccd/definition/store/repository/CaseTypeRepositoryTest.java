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
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;

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
    private static final String TEST_CASE_TYPE_REFERENCE = "TestCase";
    private static final String DEFINITIVE_CASE_TYPE_REFERENCE = "TESTCASE";
    private static final String ANOTHER_CASE_TYPE_REFERENCE = "ANOTHERCASE";
    private static final String DEFINITIVE_CASE_TYPE_REFERENCE_2 = "AnotherCase";

    private JurisdictionEntity testJurisdiction;

    @Before
    public void setUp() {
        this.testJurisdiction = testHelper.createJurisdiction();

        createMultipleVersionsOfCaseType(CASE_TYPE_REFERENCE, "Test case", testJurisdiction);
        try {
            createMultipleSpellingsOfCaseTypeReference("Test case", 1, testJurisdiction);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        createMultipleSpellingsOfCaseTypeReferenceWithSameTimestamp("Another case", 1, testJurisdiction);
    }

    private void createCaseTypeEntity(String reference, String name, Integer version, JurisdictionEntity jurisdiction) {
        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(reference);
        caseType.setName(name);
        caseType.setVersion(version);
        caseType.setJurisdiction(jurisdiction);
        caseType.setSecurityClassification(PUBLIC);
        saveCaseTypeClearAndFlushSession(caseType);
    }

    private void createMultipleVersionsOfCaseType(String reference, String name, JurisdictionEntity jurisdiction) {
        createCaseTypeEntity(reference, name, 1, jurisdiction);
        createCaseTypeEntity(reference, name, 2, jurisdiction);
        createCaseTypeEntity(reference, name, 3, jurisdiction);
    }

    @SuppressWarnings("squid:S2925") // Ignore Sonar warning about use of Thread.sleep()
    private void createMultipleSpellingsOfCaseTypeReference(String name, Integer version,
                                                            JurisdictionEntity jurisdiction)
        throws InterruptedException {
        createCaseTypeEntity("testcase", name, version, jurisdiction);
        // Add delay to ensure different "createdAt" timestamp
        Thread.sleep(100);
        createCaseTypeEntity(TEST_CASE_TYPE_REFERENCE, name, version, jurisdiction);
        // Add delay to ensure different "createdAt" timestamp
        Thread.sleep(100);
        createCaseTypeEntity(DEFINITIVE_CASE_TYPE_REFERENCE, name, version, jurisdiction);
    }

    private void createMultipleSpellingsOfCaseTypeReferenceWithSameTimestamp(String name, Integer version,
                                                                             JurisdictionEntity jurisdiction) {
        createCaseTypeEntity("anothercase", name, version, jurisdiction);
        createCaseTypeEntity(ANOTHER_CASE_TYPE_REFERENCE, name, version, jurisdiction);
        createCaseTypeEntity(DEFINITIVE_CASE_TYPE_REFERENCE_2, name, version, jurisdiction);
    }

    @Test
    public void severalVersionsOfCaseTypeExistForReference_findCurrentCaseTypeReturnsCurrentVersionOfCaseType() {
        List<CaseTypeEntity> caseTypeEntities = classUnderTest.findAll().stream()
            .filter(c -> c.getReference().equals(CASE_TYPE_REFERENCE)).collect(Collectors.toList());
        assertEquals(3, caseTypeEntities.size());
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
        List<CaseTypeEntity> caseTypeEntities
            = classUnderTest.findByJurisdictionId(testJurisdiction.getReference());
        assertEquals(7, caseTypeEntities.size());
        assertEquals(3, caseTypeEntities.get(0).getVersion().intValue());
    }

    @Test
    public void caseTypeDoesNotExistForJurisdiction_emptyListReturned() {
        List<CaseTypeEntity> caseTypeEntities
            = classUnderTest.findByJurisdictionId("Non Existing Jurisdiction");
        assertTrue(caseTypeEntities.isEmpty());
    }

    @Test
    public void shouldReturnZeroCountIfCaseTypeIsOfExcludedJurisdiction() {
        Integer result = classUnderTest.caseTypeExistsInAnyJurisdiction(CASE_TYPE_REFERENCE, testJurisdiction.getReference());
        assertEquals(0, result.intValue());
    }

    @Test
    public void shouldReturnAPositiveCountIfCaseTypeIsNotOfExcludedJurisdiction() {
        Integer result = classUnderTest.caseTypeExistsInAnyJurisdiction(CASE_TYPE_REFERENCE, "OtherJurisdiction");
        assertEquals(3, result.intValue());
    }

    @Test
    public void caseTypeReferenceHasSeveralSpellings_findDefinitiveReferenceReturnsLatestCreated() {
        Optional<CaseTypeEntity> definitiveCaseTypeOptional =
            classUnderTest.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(TEST_CASE_TYPE_REFERENCE);
        assertTrue(definitiveCaseTypeOptional.isPresent());
        assertEquals(DEFINITIVE_CASE_TYPE_REFERENCE, definitiveCaseTypeOptional.get().getReference());
    }

    @Test
    public void caseTypeReferenceHasSeveralSpellingsAndSameTimestamp_findDefinitiveReferenceReturnsLatestCreated() {
        Optional<CaseTypeEntity> definitiveCaseTypeOptional =
            classUnderTest.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(ANOTHER_CASE_TYPE_REFERENCE);
        assertTrue(definitiveCaseTypeOptional.isPresent());
        assertEquals(DEFINITIVE_CASE_TYPE_REFERENCE_2, definitiveCaseTypeOptional.get().getReference());
    }

    @Test
    public void definitiveCaseTypeReferenceDoesNotExistForReference_emptyOptionalReturned() {
        Optional<CaseTypeEntity> definitiveCaseTypeOptional =
            classUnderTest.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc("Dummy");
        assertFalse(definitiveCaseTypeOptional.isPresent());
    }

    private void saveCaseTypeClearAndFlushSession(CaseTypeEntity caseType) {
        classUnderTest.save(caseType);
        entityManager.flush();
        entityManager.clear();
    }

}
