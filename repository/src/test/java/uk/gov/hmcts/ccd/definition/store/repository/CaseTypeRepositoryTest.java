package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import javax.persistence.EntityManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
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
    private JurisdictionEntity testJurisdictionWithCaseTypeACL;

    @Before
    public void setUp() {
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
        CaseTypeACLEntity caseTypeACLWithCreateOnly = caseTypeACLWithUserRoleEntity(
            "role-with-create-only", true, false, false, false,
            "User Role 1", "User Role 1", SecurityClassification.RESTRICTED);
        CaseTypeACLEntity caseTypeACLWithReadOnly = caseTypeACLWithUserRoleEntity(
            "role-with-read-only", false, true, false, false,
            "User Role 2", "User Role 2", SecurityClassification.PRIVATE);
        CaseTypeACLEntity caseTypeACLWithUpdateOnly = caseTypeACLWithUserRoleEntity(
            "role-with-update-only", false, false, true, false,
            "User Role 3", "User Role 3", SecurityClassification.RESTRICTED);
        CaseTypeACLEntity caseTypeACLWithDeleteOnly = caseTypeACLWithUserRoleEntity(
            "role-with-delete-only", false, false, false, true,
            "User Role 4", "User Role 4", SecurityClassification.PUBLIC);
        return (Arrays.asList(
            caseTypeACLWithCreateOnly, caseTypeACLWithReadOnly, caseTypeACLWithUpdateOnly, caseTypeACLWithDeleteOnly));
    }

    private Collection<CaseTypeACLEntity> createCaseTypeACLWithFullAccess() {
        CaseTypeACLEntity caseTypeACLWithFullAccess = caseTypeACLWithUserRoleEntity(
            "role-with-full-access", true, true, true, true,
            "User Role Full Access", "User Role Full Access", SecurityClassification.PUBLIC);
        return (Collections.singletonList(caseTypeACLWithFullAccess));
    }

    private CaseTypeACLEntity caseTypeACLWithUserRoleEntity(String reference,
                                                            Boolean create,
                                                            Boolean read,
                                                            Boolean update,
                                                            Boolean delete,
                                                            String userRoleReference,
                                                            String userRoleName,
                                                            SecurityClassification sc) {
        CaseTypeACLEntity caseTypeACLEntity = new CaseTypeACLEntity();
        caseTypeACLEntity.setUserRole(createUserRoleEntity(userRoleReference, userRoleName, sc));
        caseTypeACLEntity.setCreate(create);
        caseTypeACLEntity.setRead(read);
        caseTypeACLEntity.setUpdate(update);
        caseTypeACLEntity.setDelete(delete);
        return caseTypeACLEntity;
    }

    private UserRoleEntity createUserRoleEntity(String reference, String userRoleName, SecurityClassification sc) {
        return testHelper.createUserRole(reference, userRoleName, sc);
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

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void severalVersionsOfCaseTypeExistForJurisdiction_findByJurisdictionIdReturnsCurrentVersionOfCaseTypeForJurisdiction() {
        List<CaseTypeEntity> caseTypeEntities
            = classUnderTest.findByJurisdictionId(testJurisdiction.getReference());
        assertEquals(7, caseTypeEntities.size());
        assertEquals(1, caseTypeEntities.get(0).getVersion().intValue());
    }

    @Test
    public void caseTypeDoesNotExistForJurisdiction_emptyListReturned() {
        List<CaseTypeEntity> caseTypeEntities
            = classUnderTest.findByJurisdictionId("Non Existing Jurisdiction");
        assertTrue(caseTypeEntities.isEmpty());
    }

    @Test
    public void shouldReturnZeroCountIfCaseTypeIsOfExcludedJurisdiction() {
        Integer result = classUnderTest.caseTypeExistsInAnyJurisdiction(
            CASE_TYPE_REFERENCE, testJurisdiction.getReference());
        assertEquals(0, result.intValue());
    }

    @Test
    public void shouldReturnAPositiveCountIfCaseTypeIsNotOfExcludedJurisdiction() {
        Integer result = classUnderTest.caseTypeExistsInAnyJurisdiction(
            CASE_TYPE_REFERENCE, "OtherJurisdiction");
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

    @Test
    public void saveAndValidateCaseTypeWithACLAndUserRoleDataTest() {
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
        assertEquals(caseTypeACLEntityWithRestrictedAccess.getUserRole().getName(),
            caseTypeACLEntityWithRestrictedAccessFromDB.getUserRole().getName());
        assertEquals(caseTypeACLEntityWithRestrictedAccess.getUserRole().getSecurityClassification(),
            caseTypeACLEntityWithRestrictedAccessFromDB.getUserRole().getSecurityClassification());
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
        assertEquals(caseTypeACLEntityWithFullAccess.getUserRole().getName(),
            caseTypeACLEntityWithFullAccessFromDB.getUserRole().getName());
        assertEquals(caseTypeACLEntityWithFullAccess.getUserRole().getSecurityClassification(),
            caseTypeACLEntityWithFullAccessFromDB.getUserRole().getSecurityClassification());
        assertEquals(caseTypeACLEntityWithFullAccess.getCreate(), caseTypeACLEntityWithFullAccessFromDB.getCreate());
        assertEquals(caseTypeACLEntityWithFullAccess.getUpdate(), caseTypeACLEntityWithFullAccessFromDB.getUpdate());
        assertEquals(caseTypeACLEntityWithFullAccess.getDelete(), caseTypeACLEntityWithFullAccessFromDB.getDelete());
        assertEquals(caseTypeACLEntityWithFullAccess.getRead(), caseTypeACLEntityWithFullAccessFromDB.getRead());
    }

    @Test
    public void getAllCaseTypeDefinitinon() {
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
}
