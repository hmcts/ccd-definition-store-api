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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class CaseTypeRepositoryTest {

    private static final String CASE_TYPE_REFERENCE = "id";
    private static final String TEST_CASE_TYPE_REFERENCE = "TestCase";
    private static final String DEFINITIVE_CASE_TYPE_REFERENCE = "TESTCASE";
    private static final String ANOTHER_CASE_TYPE_REFERENCE = "ANOTHERCASE";
    private static final String DEFINITIVE_CASE_TYPE_REFERENCE_2 = "AnotherCase";

    @Autowired
    private CaseTypeRepository classUnderTest;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TestHelper testHelper;

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

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, 42, 99})
    void shouldPersistOptionalCaseTypesOrder(Integer order) {
        CaseTypeEntity caseType = classUnderTest.findCurrentVersionForReference(CASE_TYPE_REFERENCE).orElseThrow();
        caseType.setCaseTypesOrder(order);
        saveCaseTypeClearAndFlushSession(caseType);

        CaseTypeEntity reloaded = classUnderTest.findCurrentVersionForReference(CASE_TYPE_REFERENCE).orElseThrow();
        assertThat(reloaded.getCaseTypesOrder()).isEqualTo(order);
        assertThat(reloaded.getVersion()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 100})
    void shouldRejectCaseTypesOrderOutsideTwoDigitRange(int order) {
        CaseTypeEntity caseType = classUnderTest.findCurrentVersionForReference(CASE_TYPE_REFERENCE).orElseThrow();
        caseType.setCaseTypesOrder(order);

        assertThatThrownBy(() -> classUnderTest.saveAndFlush(caseType))
            .isInstanceOf(DataIntegrityViolationException.class)
            .rootCause()
            .hasMessageContaining("chk_case_type_case_types_order");
    }

    @Test
    void severalVersionsOfCaseTypeExistForReference_findCurrentCaseTypeReturnsCurrentVersionOfCaseType() {
        List<CaseTypeEntity> caseTypeEntities = classUnderTest.findAll().stream()
            .filter(c -> c.getReference().equals(CASE_TYPE_REFERENCE)).collect(Collectors.toList());
        assertThat(caseTypeEntities).hasSize(3);
        Optional<CaseTypeEntity> caseTypeEntityOptional
            = classUnderTest.findCurrentVersionForReference(CASE_TYPE_REFERENCE);
        assertThat(caseTypeEntityOptional).isPresent();
        assertThat(caseTypeEntityOptional.get().getVersion()).isEqualTo(3);
    }

    @Test
    void caseTypeDoesNotExistForReference_emptyOptionalReturned() {
        Optional<CaseTypeEntity> caseTypeEntityOptional
            = classUnderTest.findCurrentVersionForReference("Non Existing Reference");
        assertThat(caseTypeEntityOptional).isEmpty();
    }

    @Test
    @SuppressWarnings("removal")
    void findByJurisdictionIdReturnsCurrentVersionForJurisdiction() {
        List<CaseTypeEntity> caseTypeEntities
            = classUnderTest.findByJurisdictionId(testJurisdiction.getReference());
        assertThat(caseTypeEntities).hasSize(7);
        assertThat(caseTypeEntities.getFirst().getVersion()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("removal")
    void caseTypeDoesNotExistForJurisdiction_emptyListReturned() {
        List<CaseTypeEntity> caseTypeEntities
            = classUnderTest.findByJurisdictionId("Non Existing Jurisdiction");
        assertThat(caseTypeEntities).isEmpty();
    }

    @Test
    void shouldReturnZeroCountIfCaseTypeIsOfExcludedJurisdiction() {
        Integer result = classUnderTest.caseTypeExistsInAnyJurisdiction(
            CASE_TYPE_REFERENCE, testJurisdiction.getReference());
        assertThat(result).isEqualTo(0);
    }

    @Test
    void shouldReturnAPositiveCountIfCaseTypeIsNotOfExcludedJurisdiction() {
        Integer result = classUnderTest.caseTypeExistsInAnyJurisdiction(
            CASE_TYPE_REFERENCE, "OtherJurisdiction");
        assertThat(result).isEqualTo(3);
    }

    @Test
    void caseTypeReferenceHasSeveralSpellings_findDefinitiveReferenceReturnsLatestCreated() {
        Optional<CaseTypeEntity> definitiveCaseTypeOptional =
            classUnderTest.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(TEST_CASE_TYPE_REFERENCE);
        assertThat(definitiveCaseTypeOptional).isPresent();
        assertThat(definitiveCaseTypeOptional.get().getReference()).isEqualTo(DEFINITIVE_CASE_TYPE_REFERENCE);
    }

    @Test
    void caseTypeReferenceHasSeveralSpellingsAndSameTimestamp_findDefinitiveReferenceReturnsLatestCreated() {
        Optional<CaseTypeEntity> definitiveCaseTypeOptional =
            classUnderTest.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(ANOTHER_CASE_TYPE_REFERENCE);
        assertThat(definitiveCaseTypeOptional).isPresent();
        assertThat(definitiveCaseTypeOptional.get().getReference()).isEqualTo(DEFINITIVE_CASE_TYPE_REFERENCE_2);
    }

    @Test
    void definitiveCaseTypeReferenceDoesNotExistForReference_emptyOptionalReturned() {
        Optional<CaseTypeEntity> definitiveCaseTypeOptional =
            classUnderTest.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc("Dummy");
        assertThat(definitiveCaseTypeOptional).isEmpty();
    }

    private void saveCaseTypeClearAndFlushSession(CaseTypeEntity caseType) {
        classUnderTest.save(caseType);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @SuppressWarnings("removal")
    void saveAndValidateCaseTypeWithACLAndAccessProfileDataTest() {
        CaseTypeEntity caseTypeEntityVersionOneWithMultiACL = createCaseTypeEntityWithCaseTypeACL(
            "CaseTypeWithACL", "CaseTypeWithACL", 1,
            testJurisdictionWithCaseTypeACL, createCaseTypeACL());
        List<CaseTypeEntity> caseTypeEntities
            = classUnderTest.findByJurisdictionId(testJurisdictionWithCaseTypeACL.getReference());
        CaseTypeEntity caseTypeEntityVersionOneWithMultiACLFromDB = caseTypeEntities.getFirst();
        Optional<Integer> caseTypeVersion = classUnderTest.findLastVersion(caseTypeEntities.getFirst().getReference());
        assertThat(caseTypeEntityVersionOneWithMultiACLFromDB.getReference())
            .isEqualTo(caseTypeEntityVersionOneWithMultiACL.getReference());
        assertThat(caseTypeVersion).contains(caseTypeEntityVersionOneWithMultiACL.getVersion());
        List<CaseTypeACLEntity> caseTypeACLEntitiesWithRestrictedAccess = caseTypeEntityVersionOneWithMultiACL
            .getCaseTypeACLEntities();
        assertThat(caseTypeEntities).hasSize(1);
        List<CaseTypeACLEntity> caseTypeACLEntitiesWithRestrictedAccessFromDB
            = caseTypeEntityVersionOneWithMultiACLFromDB.getCaseTypeACLEntities();
        assertThat(caseTypeACLEntitiesWithRestrictedAccessFromDB)
            .hasSameSizeAs(caseTypeACLEntitiesWithRestrictedAccess);
        CaseTypeACLEntity caseTypeACLEntityWithRestrictedAccess = caseTypeACLEntitiesWithRestrictedAccess.getFirst();
        CaseTypeACLEntity caseTypeACLEntityWithRestrictedAccessFromDB
            = caseTypeACLEntitiesWithRestrictedAccessFromDB.getFirst();
        assertThat(caseTypeACLEntityWithRestrictedAccessFromDB.getCaseType().getVersion())
            .isEqualTo(caseTypeACLEntityWithRestrictedAccess.getCaseType().getVersion());
        assertThat(caseTypeACLEntityWithRestrictedAccessFromDB.getAccessProfile().getName())
            .isEqualTo(caseTypeACLEntityWithRestrictedAccess.getAccessProfile().getName());
        assertThat(caseTypeACLEntityWithRestrictedAccessFromDB.getAccessProfile().getSecurityClassification())
            .isEqualTo(caseTypeACLEntityWithRestrictedAccess.getAccessProfile().getSecurityClassification());
        assertThat(caseTypeACLEntityWithRestrictedAccessFromDB.getCreate())
            .isEqualTo(caseTypeACLEntityWithRestrictedAccess.getCreate());
        assertThat(caseTypeACLEntityWithRestrictedAccessFromDB.getRead())
            .isEqualTo(caseTypeACLEntityWithRestrictedAccess.getRead());
        assertThat(caseTypeACLEntityWithRestrictedAccessFromDB.getDelete())
            .isEqualTo(caseTypeACLEntityWithRestrictedAccess.getDelete());
        assertThat(caseTypeACLEntityWithRestrictedAccessFromDB.getUpdate())
            .isEqualTo(caseTypeACLEntityWithRestrictedAccess.getUpdate());

        CaseTypeEntity caseTypeEntityVersionTwoWithSingleACL = createCaseTypeEntityWithCaseTypeACL(
            "CaseTypeWithACL", "CaseTypeWithACL",
            2, testJurisdictionWithCaseTypeACL, createCaseTypeACLWithFullAccess());
        caseTypeVersion = classUnderTest.findLastVersion(caseTypeEntities.getFirst().getReference());
        assertThat(caseTypeVersion).contains(caseTypeEntityVersionTwoWithSingleACL.getVersion());
        Optional<CaseTypeEntity> caseTypeEntity = classUnderTest.findCurrentVersionForReference(
            caseTypeEntities.getFirst().getReference());
        assertThat(caseTypeEntity).isPresent();
        List<CaseTypeACLEntity> caseTypeACLEntitiesWithFullAccessFromDB = caseTypeEntity.get().getCaseTypeACLEntities();
        List<CaseTypeACLEntity> caseTypeACLEntitiesWithFullAccess
            = caseTypeEntityVersionTwoWithSingleACL.getCaseTypeACLEntities();
        CaseTypeACLEntity caseTypeACLEntityWithFullAccess = caseTypeACLEntitiesWithFullAccess.getFirst();
        CaseTypeACLEntity caseTypeACLEntityWithFullAccessFromDB = caseTypeACLEntitiesWithFullAccessFromDB.getFirst();
        assertThat(caseTypeACLEntitiesWithFullAccessFromDB).hasSameSizeAs(caseTypeACLEntitiesWithFullAccess);
        assertThat(caseTypeACLEntityWithFullAccessFromDB.getCaseType().getVersion())
            .isEqualTo(caseTypeACLEntityWithFullAccess.getCaseType().getVersion());
        assertThat(caseTypeACLEntityWithFullAccessFromDB.getAccessProfile().getName())
            .isEqualTo(caseTypeACLEntityWithFullAccess.getAccessProfile().getName());
        assertThat(caseTypeACLEntityWithFullAccessFromDB.getAccessProfile().getSecurityClassification())
            .isEqualTo(caseTypeACLEntityWithFullAccess.getAccessProfile().getSecurityClassification());
        assertThat(caseTypeACLEntityWithFullAccessFromDB.getCreate())
            .isEqualTo(caseTypeACLEntityWithFullAccess.getCreate());
        assertThat(caseTypeACLEntityWithFullAccessFromDB.getUpdate())
            .isEqualTo(caseTypeACLEntityWithFullAccess.getUpdate());
        assertThat(caseTypeACLEntityWithFullAccessFromDB.getDelete())
            .isEqualTo(caseTypeACLEntityWithFullAccess.getDelete());
        assertThat(caseTypeACLEntityWithFullAccessFromDB.getRead())
            .isEqualTo(caseTypeACLEntityWithFullAccess.getRead());
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

        assertThat(result).extracting(CaseTypeEntity::getName)
            .contains("name1.1", "name2.2", "name3.1", "name4.4")
            .doesNotContain("name2.1", "name4.1", "name4.2", "name4.3");
    }

    @Test
    void shouldFindAllUniqueCaseTypeIds() {
        List<String> result = classUnderTest.findAllCaseTypeIds();

        assertThat(result).containsExactlyInAnyOrder(
            "TESTCASE", "AnotherCase", "anothercase", "TestCase", "testcase", "id", "ANOTHERCASE");
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

        assertThat(result).extracting(CaseTypeEntity::getName)
            .containsExactlyInAnyOrder("name1.1", "name2.2", "name4.4");
    }
}
