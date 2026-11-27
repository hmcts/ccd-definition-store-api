package uk.gov.hmcts.ccd.definition.store.repository;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import java.util.List;

import com.vladmihalcea.sql.SQLStatementCountValidator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class CaseTypeLiteRepositoryTest {

    @Autowired
    private CaseTypeLiteRepository classUnderTest;

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    private JurisdictionEntity testJurisdiction;

    @BeforeEach
    void setUp() {
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

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {1, 42, 99})
    void shouldRetrieveOptionalCaseTypesOrderFromLatestVersion(Integer order) {
        caseTypeRepository.findAll().stream()
            .filter(caseType -> "id".equals(caseType.getReference()))
            .forEach(caseType -> caseType.setCaseTypesOrder(caseType.getVersion() == 3 ? order : Integer.valueOf(7)));
        entityManager.flush();
        entityManager.clear();

        CaseTypeLiteEntity caseType = getCaseTypeLiteEntityByReference("id",
            classUnderTest.findByJurisdictionId(testJurisdiction.getReference()));
        assertThat(caseType.getVersion()).isEqualTo(3);
        assertThat(caseType.getCaseTypesOrder()).isEqualTo(order);

        entityManager.clear();
        CaseTypeLiteEntity currentVersion = classUnderTest.findCurrentVersionForReference("id").orElseThrow();
        assertThat(currentVersion.getVersion()).isEqualTo(3);
        assertThat(currentVersion.getCaseTypesOrder()).isEqualTo(order);
    }

    @Test
    void shouldRetainCaseTypesOrderWhenConvertingToLiteEntity() {
        CaseTypeEntity caseType = caseTypeRepository.findCurrentVersionForReference("id").orElseThrow();
        caseType.setCaseTypesOrder(42);
        saveCaseTypeClearAndFlushSession(caseType);

        CaseTypeEntity reloaded = caseTypeRepository.findCurrentVersionForReference("id").orElseThrow();
        assertThat(CaseTypeLiteEntity.toCaseTypeLiteEntity(reloaded).getCaseTypesOrder()).isEqualTo(42);
    }

    @Test
    void findByJurisdictionIdReturnsCurrentVersionOfCaseTypesWhenSeveralVersionsExist() {
        List<CaseTypeLiteEntity> caseTypeEntityOptional
            = classUnderTest.findByJurisdictionId(testJurisdiction.getReference());
        assertThat(caseTypeEntityOptional).hasSize(2);

        CaseTypeLiteEntity caseTypeJurisdictionIdVersionReferenceIdx1 = getCaseTypeLiteEntityByReference("id",
            caseTypeEntityOptional);
        assertThat(caseTypeJurisdictionIdVersionReferenceIdx1.getVersion()).isEqualTo(3);
        assertThat(caseTypeJurisdictionIdVersionReferenceIdx1.getName()).isEqualTo("Test case");
        assertThat(caseTypeJurisdictionIdVersionReferenceIdx1.getDescription()).isEqualTo("Some case type");

        CaseTypeLiteEntity caseTypeJurisdictionIdVersionReferenceIdx2 = getCaseTypeLiteEntityByReference("id2",
            caseTypeEntityOptional);
        assertThat(caseTypeJurisdictionIdVersionReferenceIdx2.getVersion()).isEqualTo(2);
        assertThat(caseTypeJurisdictionIdVersionReferenceIdx2.getName()).isEqualTo("Test case 2");
        assertThat(caseTypeJurisdictionIdVersionReferenceIdx2.getDescription()).isEqualTo("Another case type");
        assertThat(caseTypeJurisdictionIdVersionReferenceIdx2.getStates()).hasSize(1);
        assertThat(caseTypeJurisdictionIdVersionReferenceIdx2.getStates().getFirst().getReference()).isEqualTo("s1");
        assertThat(caseTypeJurisdictionIdVersionReferenceIdx2.getStates().getFirst().getName()).isEqualTo("State 1");
        assertThat(caseTypeJurisdictionIdVersionReferenceIdx2.getStates().getFirst().getDescription())
            .isEqualTo("A description");
    }

    @Test
    void emptyListReturnedWhenNoCaseTypesForJurisdiction() {
        List<CaseTypeLiteEntity> caseTypeEntityOptional
            = classUnderTest.findByJurisdictionId("Non Existing Jurisdiction");
        assertThat(caseTypeEntityOptional).isEmpty();
    }

    @Test
    void checkSQLStatementCounts() {

        SQLStatementCountValidator.reset();

        List<CaseTypeLiteEntity> caseTypeEntityOptional
            = classUnderTest.findByJurisdictionId(testJurisdiction.getReference());

        // caseType entity only no child associations (lazy)
        SQLStatementCountValidator.assertSelectCount(1);

        CaseTypeLiteEntity caseTypeLiteEntity =
            getCaseTypeLiteEntityByReference("id2", caseTypeEntityOptional);


        assertThat(caseTypeLiteEntity.getStates()).hasSize(1);

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
