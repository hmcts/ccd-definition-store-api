package uk.gov.hmcts.ccd.definition.store.repository;


import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class CaseTypeRepositoryTest {

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
    private JurisdictionEntity testJurisdictionWithCaseTypeACL;

    @BeforeEach
    void setUp() {
        this.testJurisdiction = testHelper.createJurisdiction();
        this.testJurisdictionWithCaseTypeACL = testHelper.createJurisdiction(
            "jurisdictionWithCaseTypeACL", "nameWithCaseTypeACL", "descWithCaseTypeACL");
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

    private CaseTypeEntity createCaseTypeEntityWithCaseTypeACL(String reference,
                                                               String name,
                                                               Integer version,
                                                               JurisdictionEntity jurisdiction,
                                                               Collection<CaseTypeACLEntity> caseTypeACLList) {
        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(reference);
        caseType.setName(name);
        caseType.setVersion(version);
        caseType.setJurisdiction(jurisdiction);
        caseType.setSecurityClassification(PUBLIC);
        caseType.addCaseTypeACLEntities(caseTypeACLList);
        saveCaseTypeClearAndFlushSession(caseType);
        return caseType;
    }

    private Collection<CaseTypeACLEntity> createCaseTypeACL() {
        CaseTypeACLEntity caseTypeACLWithCreateOnly = caseTypeACLWithAccessProfileEntity(
            "acl-with-create-only", true, false, false, false,
            "Access Profile 1", "Access Profile 1", SecurityClassification.RESTRICTED);
        CaseTypeACLEntity caseTypeACLWithReadOnly = caseTypeACLWithAccessProfileEntity(
            "acl-with-read-only", false, true, false, false,
            "Access Profile 2", "Access Profile 2", SecurityClassification.PRIVATE);
        CaseTypeACLEntity caseTypeACLWithUpdateOnly = caseTypeACLWithAccessProfileEntity(
            "acl-with-update-only", false, false, true, false,
            "Access Profile 3", "Access Profile 3", SecurityClassification.RESTRICTED);
        CaseTypeACLEntity caseTypeACLWithDeleteOnly = caseTypeACLWithAccessProfileEntity(
            "acl-with-delete-only", false, false, false, true,
            "Access Profile 4", "Access Profile 4", SecurityClassification.PUBLIC);
        return (Arrays.asList(
            caseTypeACLWithCreateOnly, caseTypeACLWithReadOnly, caseTypeACLWithUpdateOnly, caseTypeACLWithDeleteOnly));
    }

    private Collection<CaseTypeACLEntity> createCaseTypeACLWithFullAccess() {
        CaseTypeACLEntity caseTypeACLWithFullAccess = caseTypeACLWithAccessProfileEntity(
            "acl-with-full-access", true, true, true, true,
            "Access Profile Full Access", "Access Profile Full Access", SecurityClassification.PUBLIC);
        return (Collections.singletonList(caseTypeACLWithFullAccess));
    }

    private CaseTypeACLEntity caseTypeACLWithAccessProfileEntity(String reference,
                                                                 Boolean create,
                                                                 Boolean read,
                                                                 Boolean update,
                                                                 Boolean delete,
                                                                 String accessProfileReference,
                                                                 String accessProfileName,
                                                                 SecurityClassification sc) {
        CaseTypeACLEntity caseTypeACLEntity = new CaseTypeACLEntity();
        caseTypeACLEntity.setAccessProfile(createAccessProfileEntity(accessProfileReference, accessProfileName, sc));
        caseTypeACLEntity.setCreate(create);
        caseTypeACLEntity.setRead(read);
        caseTypeACLEntity.setUpdate(update);
        caseTypeACLEntity.setDelete(delete);
        return caseTypeACLEntity;
    }

    private AccessProfileEntity createAccessProfileEntity(String reference,
                                                          String accessProfileName,
                                                          SecurityClassification sc) {
        return testHelper.createAccessProfile(reference, accessProfileName, sc);
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
    void severalVersionsOfCaseTypeExistForReference_findCurrentCaseTypeReturnsCurrentVersionOfCaseType() {
        List<CaseTypeEntity> caseTypeEntities = classUnderTest.findAll().stream()
            .filter(c -> c.getReference().equals(CASE_TYPE_REFERENCE)).collect(Collectors.toList());
        assertEquals(3, caseTypeEntities.size());
        Optional<CaseTypeEntity> caseTypeEntityOptional
            = classUnderTest.findCurrentVersionForReference(CASE_TYPE_REFERENCE);
        assertTrue(caseTypeEntityOptional.isPresent());
        assertEquals(3, caseTypeEntityOptional.get().getVersion().intValue());
    }

    @Test
    void caseTypeDoesNotExistForReference_emptyOptionalReturned() {
        Optional<CaseTypeEntity> caseTypeEntityOptional
            = classUnderTest.findCurrentVersionForReference("Non Existing Reference");
        assertFalse(caseTypeEntityOptional.isPresent());
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    void severalVersionsOfCaseTypeExistForJurisdiction_findByJurisdictionIdReturnsCurrentVersionOfCaseTypeForJurisdiction() {
        List<CaseTypeEntity> caseTypeEntities
            = classUnderTest.findByJurisdictionId(testJurisdiction.getReference());
        assertEquals(7, caseTypeEntities.size());
        assertEquals(1, caseTypeEntities.get(0).getVersion().intValue());
    }

    @Test
    void caseTypeDoesNotExistForJurisdiction_emptyListReturned() {
        List<CaseTypeEntity> caseTypeEntities
            = classUnderTest.findByJurisdictionId("Non Existing Jurisdiction");
        assertTrue(caseTypeEntities.isEmpty());
    }

    @Test
    void shouldReturnZeroCountIfCaseTypeIsOfExcludedJurisdiction() {
        Integer result = classUnderTest.caseTypeExistsInAnyJurisdiction(
            CASE_TYPE_REFERENCE, testJurisdiction.getReference());
        assertEquals(0, result.intValue());
    }

    @Test
    void shouldReturnAPositiveCountIfCaseTypeIsNotOfExcludedJurisdiction() {
        Integer result = classUnderTest.caseTypeExistsInAnyJurisdiction(
            CASE_TYPE_REFERENCE, "OtherJurisdiction");
        assertEquals(3, result.intValue());
    }

    @Test
    void caseTypeReferenceHasSeveralSpellings_findDefinitiveReferenceReturnsLatestCreated() {
        Optional<CaseTypeEntity> definitiveCaseTypeOptional =
            classUnderTest.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(TEST_CASE_TYPE_REFERENCE);
        assertTrue(definitiveCaseTypeOptional.isPresent());
        assertEquals(DEFINITIVE_CASE_TYPE_REFERENCE, definitiveCaseTypeOptional.get().getReference());
    }

    @Test
    void caseTypeReferenceHasSeveralSpellingsAndSameTimestamp_findDefinitiveReferenceReturnsLatestCreated() {
        Optional<CaseTypeEntity> definitiveCaseTypeOptional =
            classUnderTest.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(ANOTHER_CASE_TYPE_REFERENCE);
        assertTrue(definitiveCaseTypeOptional.isPresent());
        assertEquals(DEFINITIVE_CASE_TYPE_REFERENCE_2, definitiveCaseTypeOptional.get().getReference());
    }

    @Test
    void definitiveCaseTypeReferenceDoesNotExistForReference_emptyOptionalReturned() {
        Optional<CaseTypeEntity> definitiveCaseTypeOptional =
            classUnderTest.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc("Dummy");
        assertFalse(definitiveCaseTypeOptional.isPresent());
    }

    private void saveCaseTypeClearAndFlushSession(CaseTypeEntity caseType) {
        classUnderTest.save(caseType);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void saveAndValidateCaseTypeWithACLAndAccessProfileDataTest() {
        CaseTypeEntity caseTypeEntityVersionOneWithMultiACL = createCaseTypeEntityWithCaseTypeACL(
            "CaseTypeWithACL", "CaseTypeWithACL", 1,
            testJurisdictionWithCaseTypeACL, createCaseTypeACL());
        List<CaseTypeEntity> caseTypeEntities
            = classUnderTest.findByJurisdictionId(testJurisdictionWithCaseTypeACL.getReference());
        CaseTypeEntity caseTypeEntityVersionOneWithMultiACLFromDB = caseTypeEntities.get(0);
        Optional<Integer> caseTypeVersion = classUnderTest.findLastVersion(caseTypeEntities.get(0).getReference());
        assertEquals(caseTypeEntityVersionOneWithMultiACL.getReference(),
            caseTypeEntityVersionOneWithMultiACLFromDB.getReference());
        assertTrue(caseTypeVersion.isPresent());
        assertEquals(caseTypeEntityVersionOneWithMultiACL.getVersion().intValue(), caseTypeVersion.get().intValue());
        List<CaseTypeACLEntity> caseTypeACLEntitiesWithRestrictedAccess = caseTypeEntityVersionOneWithMultiACL
            .getCaseTypeACLEntities();
        assertEquals(1, caseTypeEntities.size());
        List<CaseTypeACLEntity> caseTypeACLEntitiesWithRestrictedAccessFromDB
            = caseTypeEntityVersionOneWithMultiACLFromDB.getCaseTypeACLEntities();
        assertEquals(caseTypeACLEntitiesWithRestrictedAccess.size(),
            caseTypeACLEntitiesWithRestrictedAccessFromDB.size());
        CaseTypeACLEntity caseTypeACLEntityWithRestrictedAccess = caseTypeACLEntitiesWithRestrictedAccess.get(0);
        CaseTypeACLEntity caseTypeACLEntityWithRestrictedAccessFromDB
            = caseTypeACLEntitiesWithRestrictedAccessFromDB.get(0);
        assertEquals(caseTypeACLEntityWithRestrictedAccess.getCaseType().getVersion().intValue(),
            caseTypeACLEntityWithRestrictedAccessFromDB.getCaseType().getVersion().intValue());
        assertEquals(caseTypeACLEntityWithRestrictedAccess.getAccessProfile().getName(),
            caseTypeACLEntityWithRestrictedAccessFromDB.getAccessProfile().getName());
        assertEquals(caseTypeACLEntityWithRestrictedAccess.getAccessProfile().getSecurityClassification(),
            caseTypeACLEntityWithRestrictedAccessFromDB.getAccessProfile().getSecurityClassification());
        assertEquals(
            caseTypeACLEntityWithRestrictedAccess.getCreate(), caseTypeACLEntityWithRestrictedAccessFromDB.getCreate());
        assertEquals(
            caseTypeACLEntityWithRestrictedAccess.getRead(), caseTypeACLEntityWithRestrictedAccessFromDB.getRead());
        assertEquals(
            caseTypeACLEntityWithRestrictedAccess.getDelete(), caseTypeACLEntityWithRestrictedAccessFromDB.getDelete());
        assertEquals(
            caseTypeACLEntityWithRestrictedAccess.getUpdate(), caseTypeACLEntityWithRestrictedAccessFromDB.getUpdate());

        CaseTypeEntity caseTypeEntityVersionTwoWithSingleACL = createCaseTypeEntityWithCaseTypeACL(
            "CaseTypeWithACL", "CaseTypeWithACL",
            2, testJurisdictionWithCaseTypeACL, createCaseTypeACLWithFullAccess());
        caseTypeVersion = classUnderTest.findLastVersion(caseTypeEntities.get(0).getReference());
        assertTrue(caseTypeVersion.isPresent());
        assertEquals(caseTypeEntityVersionTwoWithSingleACL.getVersion().intValue(), caseTypeVersion.get().intValue());
        Optional<CaseTypeEntity> caseTypeEntity = classUnderTest.findCurrentVersionForReference(
            caseTypeEntities.get(0).getReference());
        assertTrue(caseTypeEntity.isPresent());
        List<CaseTypeACLEntity> caseTypeACLEntitiesWithFullAccessFromDB = caseTypeEntity.get().getCaseTypeACLEntities();
        List<CaseTypeACLEntity> caseTypeACLEntitiesWithFullAccess
            = caseTypeEntityVersionTwoWithSingleACL.getCaseTypeACLEntities();
        CaseTypeACLEntity caseTypeACLEntityWithFullAccess = caseTypeACLEntitiesWithFullAccess.get(0);
        CaseTypeACLEntity caseTypeACLEntityWithFullAccessFromDB = caseTypeACLEntitiesWithFullAccessFromDB.get(0);
        assertEquals(caseTypeACLEntitiesWithFullAccess.size(), caseTypeACLEntitiesWithFullAccessFromDB.size());
        assertEquals(caseTypeACLEntityWithFullAccess.getCaseType().getVersion().intValue(),
            caseTypeACLEntityWithFullAccessFromDB.getCaseType().getVersion().intValue());
        assertEquals(caseTypeACLEntityWithFullAccess.getAccessProfile().getName(),
            caseTypeACLEntityWithFullAccessFromDB.getAccessProfile().getName());
        assertEquals(caseTypeACLEntityWithFullAccess.getAccessProfile().getSecurityClassification(),
            caseTypeACLEntityWithFullAccessFromDB.getAccessProfile().getSecurityClassification());
        assertEquals(caseTypeACLEntityWithFullAccess.getCreate(), caseTypeACLEntityWithFullAccessFromDB.getCreate());
        assertEquals(caseTypeACLEntityWithFullAccess.getUpdate(), caseTypeACLEntityWithFullAccessFromDB.getUpdate());
        assertEquals(caseTypeACLEntityWithFullAccess.getDelete(), caseTypeACLEntityWithFullAccessFromDB.getDelete());
        assertEquals(caseTypeACLEntityWithFullAccess.getRead(), caseTypeACLEntityWithFullAccessFromDB.getRead());
    }

    @Test
    void getAllCaseTypeDefinitions() {
        createCaseTypeEntity("ref1", "name1.1", 1, testJurisdiction);
        createCaseTypeEntity("ref2", "name2.1", 1, testJurisdiction);
        createCaseTypeEntity("ref2", "name2.2", 2, testJurisdiction);
        createCaseTypeEntity("ref3", "name3.1", 1, testJurisdiction);
        createCaseTypeEntity("ref4", "name4.1", 1, testJurisdiction);
        createCaseTypeEntity("ref4", "name4.2", 2, testJurisdiction);
        createCaseTypeEntity("ref4", "name4.3", 3, testJurisdiction);
        createCaseTypeEntity("ref4", "name4.4", 4, testJurisdiction);

        List<CaseTypeEntity> result = classUnderTest.findAllLatestVersions();

        assertAll(
            () -> assertThat(result, hasItem(hasProperty("name", is("name1.1")))),
            () -> assertThat(result, hasItem(hasProperty("name", is("name2.2")))),
            () -> assertThat(result, hasItem(hasProperty("name", is("name3.1")))),
            () -> assertThat(result, hasItem(hasProperty("name", is("name4.4")))),
            () -> assertThat(result, not(hasItem(hasProperty("name", is("name2.1"))))),
            () -> assertThat(result, not(hasItem(hasProperty("name", is("name4.1"))))),
            () -> assertThat(result, not(hasItem(hasProperty("name", is("name4.2"))))),
            () -> assertThat(result, not(hasItem(hasProperty("name", is("name4.3")))))
        );
    }

    @Test
    void shouldFindAllUniqueCaseTypeIds() {
        List<String> result = classUnderTest.findAllCaseTypeIds();

        assertAll(
            () -> assertEquals(7, result.size()),
            () -> assertTrue(result.contains("TESTCASE")),
            () -> assertTrue(result.contains("AnotherCase")),
            () -> assertTrue(result.contains("anothercase")),
            () -> assertTrue(result.contains("TestCase")),
            () -> assertTrue(result.contains("testcase")),
            () -> assertTrue(result.contains("id")),
            () -> assertTrue(result.contains("ANOTHERCASE"))
        );
    }

    @Test
    void getAllCaseTypeDefinitionsByReferences() {
        createCaseTypeEntity("ref1", "name1.1", 1, testJurisdiction);
        createCaseTypeEntity("ref2", "name2.1", 1, testJurisdiction);
        createCaseTypeEntity("ref2", "name2.2", 2, testJurisdiction);
        createCaseTypeEntity("ref3", "name3.1", 1, testJurisdiction);
        createCaseTypeEntity("ref4", "name4.1", 1, testJurisdiction);
        createCaseTypeEntity("ref4", "name4.2", 2, testJurisdiction);
        createCaseTypeEntity("ref4", "name4.3", 3, testJurisdiction);
        createCaseTypeEntity("ref4", "name4.4", 4, testJurisdiction);

        List<CaseTypeEntity> result = classUnderTest.findAllLatestVersions(List.of(
            "ref1", "ref2", "ref4"
        ));

        assertAll(
            () -> assertEquals(3, result.size()),
            () -> assertThat(result, hasItem(hasProperty("name", is("name1.1")))),
            () -> assertThat(result, hasItem(hasProperty("name", is("name2.2")))),
            () -> assertThat(result, hasItem(hasProperty("name", is("name4.4"))))
        );
    }
}
