package uk.gov.hmcts.ccd.definition.store.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class ShellMappingRepositoryTest {

    @Autowired
    private ShellMappingRepository shellMappingRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    private ShellMappingEntity testShellMapping1;
    private ShellMappingEntity testShellMapping2;
    private CaseTypeLiteEntity shellCaseTypeLite1;
    private CaseTypeLiteEntity shellCaseTypeLite2;
    private CaseTypeLiteEntity origCaseTypeLite1;
    private CaseTypeLiteEntity origCaseTypeLite2;
    private CaseFieldEntity shellCaseField1;
    private CaseFieldEntity shellCaseField2;
    private CaseFieldEntity origCaseField1;
    private CaseFieldEntity origCaseField2;

    @BeforeEach
    void setUp() {
        // Create jurisdictions
        JurisdictionEntity jurisdiction1 = testHelper.createJurisdiction("JURISDICTION_1", "Jurisdiction 1", "Desc 1");
        JurisdictionEntity jurisdiction2 = testHelper.createJurisdiction("JURISDICTION_2", "Jurisdiction 2", "Desc 2");

        // Create field types
        FieldTypeEntity fieldType1 = testHelper.createType(jurisdiction1);
        FieldTypeEntity fieldType2 = testHelper.createType(jurisdiction2);

        // Create case types with case fields
        CaseTypeEntity shellCaseType1 = createCaseTypeWithField("SHELL_CASE_TYPE_1", "Shell Case Type 1", 
            jurisdiction1, fieldType1, "shellField1");
        CaseTypeEntity shellCaseType2 = createCaseTypeWithField("SHELL_CASE_TYPE_2", "Shell Case Type 2", 
            jurisdiction2, fieldType2, "shellField2");
        CaseTypeEntity origCaseType1 = createCaseTypeWithField("ORIG_CASE_TYPE_1", "Orig Case Type 1", 
            jurisdiction1, fieldType1, "origField1");
        CaseTypeEntity origCaseType2 = createCaseTypeWithField("ORIG_CASE_TYPE_2", "Orig Case Type 2", 
            jurisdiction2, fieldType2, "origField2");

        // Convert to CaseTypeLiteEntity
        shellCaseTypeLite1 = CaseTypeLiteEntity.toCaseTypeLiteEntity(shellCaseType1);
        shellCaseTypeLite2 = CaseTypeLiteEntity.toCaseTypeLiteEntity(shellCaseType2);
        origCaseTypeLite1 = CaseTypeLiteEntity.toCaseTypeLiteEntity(origCaseType1);
        origCaseTypeLite2 = CaseTypeLiteEntity.toCaseTypeLiteEntity(origCaseType2);

        // Get case fields from saved case types
        shellCaseField1 = shellCaseType1.getCaseFields().stream()
            .filter(f -> "shellField1".equals(f.getReference()))
            .findFirst().orElseThrow();
        shellCaseField2 = shellCaseType2.getCaseFields().stream()
            .filter(f -> "shellField2".equals(f.getReference()))
            .findFirst().orElseThrow();
        origCaseField1 = origCaseType1.getCaseFields().stream()
            .filter(f -> "origField1".equals(f.getReference()))
            .findFirst().orElseThrow();
        origCaseField2 = origCaseType2.getCaseFields().stream()
            .filter(f -> "origField2".equals(f.getReference()))
            .findFirst().orElseThrow();

        // Create shell mappings
        testShellMapping1 = createShellMappingEntity(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31),
            shellCaseTypeLite1,
            shellCaseField1,
            origCaseTypeLite1,
            origCaseField1
        );

        testShellMapping2 = createShellMappingEntity(
            LocalDate.of(2024, 6, 1),
            null,
            shellCaseTypeLite2,
            shellCaseField2,
            origCaseTypeLite2,
            origCaseField2
        );

        saveShellMappingAndFlushSession(testShellMapping1, testShellMapping2);
    }

    @Test
    void shouldSaveShellMappingEntity() {
        // Given
        JurisdictionEntity jurisdiction = testHelper.createJurisdiction();
        FieldTypeEntity fieldType = testHelper.createType(jurisdiction);
        CaseTypeEntity shellCaseType = createCaseTypeWithField("SHELL_CASE_TYPE_3", "Shell Case Type 3", 
            jurisdiction, fieldType, "shellField3");
        CaseTypeEntity origCaseType = createCaseTypeWithField("ORIG_CASE_TYPE_3", "Orig Case Type 3", 
            jurisdiction, fieldType, "origField3");

        CaseTypeLiteEntity shellCaseTypeLite = CaseTypeLiteEntity.toCaseTypeLiteEntity(shellCaseType);
        CaseTypeLiteEntity origCaseTypeLite = CaseTypeLiteEntity.toCaseTypeLiteEntity(origCaseType);
        CaseFieldEntity shellCaseField = shellCaseType.getCaseFields().stream()
            .filter(f -> "shellField3".equals(f.getReference()))
            .findFirst().orElseThrow();
        CaseFieldEntity origCaseField = origCaseType.getCaseFields().stream()
            .filter(f -> "origField3".equals(f.getReference()))
            .findFirst().orElseThrow();

        ShellMappingEntity newShellMapping = createShellMappingEntity(
            LocalDate.of(2025, 1, 1),
            null,
            shellCaseTypeLite,
            shellCaseField,
            origCaseTypeLite,
            origCaseField
        );

        // When
        ShellMappingEntity saved = shellMappingRepository.save(newShellMapping);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertNotNull(saved.getShellCaseTypeId()),
            () -> assertNotNull(saved.getShellCaseFieldName()),
            () -> assertNotNull(saved.getOriginatingCaseTypeId()),
            () -> assertNotNull(saved.getOriginatingCaseFieldName()),
            () -> assertEquals(LocalDate.of(2025, 1, 1), saved.getLiveFrom()),
            () -> assertThat(saved.getLiveTo(), is((LocalDate) null))
        );
    }

    @Test
    void shouldFindShellMappingById() {
        // When
        Optional<ShellMappingEntity> found = shellMappingRepository.findById(testShellMapping1.getId());

        // Then
        assertAll(
            () -> assertTrue(found.isPresent()),
            () -> assertEquals(testShellMapping1.getId(), found.get().getId()),
            () -> assertNotNull(found.get().getShellCaseTypeId()),
            () -> assertNotNull(found.get().getShellCaseFieldName()),
            () -> assertNotNull(found.get().getOriginatingCaseTypeId()),
            () -> assertNotNull(found.get().getOriginatingCaseFieldName())
        );
    }

    @Test
    void shouldReturnEmptyOptionalWhenShellMappingNotFound() {
        // When
        Optional<ShellMappingEntity> found = shellMappingRepository.findById(99999);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindAllShellMappings() {
        // When
        List<ShellMappingEntity> allMappings = shellMappingRepository.findAll();

        // Then
        assertAll(
            () -> assertThat(allMappings.size(), is(2)),
            () -> assertTrue(allMappings.stream()
                .anyMatch(m -> m.getId().equals(testShellMapping1.getId()))),
            () -> assertTrue(allMappings.stream()
                .anyMatch(m -> m.getId().equals(testShellMapping2.getId())))
        );
    }

    @Test
    void shouldUpdateShellMappingEntity() {
        // Given
        ShellMappingEntity existing = shellMappingRepository.findById(testShellMapping1.getId()).orElseThrow();
        existing.setLiveFrom(LocalDate.of(2025, 1, 1));
        existing.setLiveTo(LocalDate.of(2025, 12, 31));

        // When
        ShellMappingEntity updated = shellMappingRepository.save(existing);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<ShellMappingEntity> found = shellMappingRepository.findById(testShellMapping1.getId());
        assertAll(
            () -> assertTrue(found.isPresent()),
            () -> assertEquals(LocalDate.of(2025, 1, 1), found.get().getLiveFrom()),
            () -> assertEquals(LocalDate.of(2025, 12, 31), found.get().getLiveTo()),
            () -> assertEquals(testShellMapping1.getId(), found.get().getId())
        );
    }

    @Test
    void shouldDeleteShellMappingEntity() {
        // Given
        Integer idToDelete = testShellMapping1.getId();

        // When
        shellMappingRepository.deleteById(idToDelete);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<ShellMappingEntity> found = shellMappingRepository.findById(idToDelete);
        assertFalse(found.isPresent());
    }

    @Test
    void shouldDeleteShellMappingEntityByEntity() {
        // Given
        ShellMappingEntity entityToDelete = testShellMapping1;

        // When
        shellMappingRepository.delete(entityToDelete);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<ShellMappingEntity> found = shellMappingRepository.findById(entityToDelete.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void shouldCountShellMappings() {
        // When
        long count = shellMappingRepository.count();

        // Then
        assertEquals(2L, count);
    }

    @Test
    void shouldReturnTrueWhenShellMappingExists() {
        // When
        boolean exists = shellMappingRepository.existsById(testShellMapping1.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenShellMappingDoesNotExist() {
        // When
        boolean exists = shellMappingRepository.existsById(99999);

        // Then
        assertFalse(exists);
    }

    @Test
    void shouldSaveShellMappingWithNullLiveTo() {
        // Given
        JurisdictionEntity jurisdiction = testHelper.createJurisdiction();
        FieldTypeEntity fieldType = testHelper.createType(jurisdiction);
        CaseTypeEntity shellCaseType = createCaseTypeWithField("SHELL_CASE_TYPE_NULL", "Shell Case Type Null", 
            jurisdiction, fieldType, "shellFieldNull");
        CaseTypeEntity origCaseType = createCaseTypeWithField("ORIG_CASE_TYPE_NULL", "Orig Case Type Null", 
            jurisdiction, fieldType, "origFieldNull");

        CaseTypeLiteEntity shellCaseTypeLite = CaseTypeLiteEntity.toCaseTypeLiteEntity(shellCaseType);
        CaseTypeLiteEntity origCaseTypeLite = CaseTypeLiteEntity.toCaseTypeLiteEntity(origCaseType);
        CaseFieldEntity shellCaseField = shellCaseType.getCaseFields().stream()
            .filter(f -> "shellFieldNull".equals(f.getReference()))
            .findFirst().orElseThrow();
        CaseFieldEntity origCaseField = origCaseType.getCaseFields().stream()
            .filter(f -> "origFieldNull".equals(f.getReference()))
            .findFirst().orElseThrow();

        ShellMappingEntity shellMappingWithNullLiveTo = createShellMappingEntity(
            LocalDate.of(2025, 1, 1),
            null,
            shellCaseTypeLite,
            shellCaseField,
            origCaseTypeLite,
            origCaseField
        );

        // When
        ShellMappingEntity saved = shellMappingRepository.save(shellMappingWithNullLiveTo);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<ShellMappingEntity> found = shellMappingRepository.findById(saved.getId());
        assertAll(
            () -> assertTrue(found.isPresent()),
            () -> assertNotNull(found.get().getLiveFrom()),
            () -> assertThat(found.get().getLiveTo(), is((LocalDate) null))
        );
    }

    @Test
    void shouldSaveMultipleShellMappings() {
        // Given
        JurisdictionEntity jurisdiction = testHelper.createJurisdiction();
        FieldTypeEntity fieldType = testHelper.createType(jurisdiction);
        CaseTypeEntity shellCaseType1 = createCaseTypeWithField("MULTI_SHELL_1", "Multi Shell 1", 
            jurisdiction, fieldType, "multiField1");
        CaseTypeEntity shellCaseType2 = createCaseTypeWithField("MULTI_SHELL_2", "Multi Shell 2", 
            jurisdiction, fieldType, "multiField2");
        CaseTypeEntity origCaseType1 = createCaseTypeWithField("MULTI_ORIG_1", "Multi Orig 1", 
            jurisdiction, fieldType, "multiOrigField1");
        CaseTypeEntity origCaseType2 = createCaseTypeWithField("MULTI_ORIG_2", "Multi Orig 2", 
            jurisdiction, fieldType, "multiOrigField2");

        CaseTypeLiteEntity shellCaseTypeLite1 = CaseTypeLiteEntity.toCaseTypeLiteEntity(shellCaseType1);
        CaseTypeLiteEntity shellCaseTypeLite2 = CaseTypeLiteEntity.toCaseTypeLiteEntity(shellCaseType2);
        CaseTypeLiteEntity origCaseTypeLite1 = CaseTypeLiteEntity.toCaseTypeLiteEntity(origCaseType1);
        CaseTypeLiteEntity origCaseTypeLite2 = CaseTypeLiteEntity.toCaseTypeLiteEntity(origCaseType2);
        CaseFieldEntity shellCaseField1 = shellCaseType1.getCaseFields().stream()
            .filter(f -> "multiField1".equals(f.getReference()))
            .findFirst().orElseThrow();
        CaseFieldEntity shellCaseField2 = shellCaseType2.getCaseFields().stream()
            .filter(f -> "multiField2".equals(f.getReference()))
            .findFirst().orElseThrow();
        CaseFieldEntity origCaseField1 = origCaseType1.getCaseFields().stream()
            .filter(f -> "multiOrigField1".equals(f.getReference()))
            .findFirst().orElseThrow();
        CaseFieldEntity origCaseField2 = origCaseType2.getCaseFields().stream()
            .filter(f -> "multiOrigField2".equals(f.getReference()))
            .findFirst().orElseThrow();

        ShellMappingEntity mapping1 = createShellMappingEntity(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 6, 30),
            shellCaseTypeLite1,
            shellCaseField1,
            origCaseTypeLite1,
            origCaseField1
        );

        ShellMappingEntity mapping2 = createShellMappingEntity(
            LocalDate.of(2025, 7, 1),
            null,
            shellCaseTypeLite2,
            shellCaseField2,
            origCaseTypeLite2,
            origCaseField2
        );

        // When
        List<ShellMappingEntity> saved = shellMappingRepository.saveAll(List.of(mapping1, mapping2));
        entityManager.flush();
        entityManager.clear();

        // Then
        assertAll(
            () -> assertEquals(2, saved.size()),
            () -> assertNotNull(saved.get(0).getId()),
            () -> assertNotNull(saved.get(1).getId()),
            () -> assertEquals(4L, shellMappingRepository.count())
        );
    }

    @Test
    void shouldDeleteAllShellMappings() {
        // When
        shellMappingRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Then
        assertAll(
            () -> assertEquals(0L, shellMappingRepository.count()),
            () -> assertTrue(shellMappingRepository.findAll().isEmpty())
        );
    }

    @Test
    void shouldDeleteAllShellMappingsById() {
        // Given
        List<Integer> idsToDelete = List.of(testShellMapping1.getId(), testShellMapping2.getId());

        // When
        shellMappingRepository.deleteAllById(idsToDelete);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertEquals(0L, shellMappingRepository.count());
    }

    @Test
    void shouldFindAllShellMappingsByIds() {
        // Given
        List<Integer> ids = List.of(testShellMapping1.getId(), testShellMapping2.getId());

        // When
        List<ShellMappingEntity> found = shellMappingRepository.findAllById(ids);

        // Then
        assertAll(
            () -> assertEquals(2, found.size()),
            () -> assertTrue(found.stream()
                .anyMatch(m -> m.getId().equals(testShellMapping1.getId()))),
            () -> assertTrue(found.stream()
                .anyMatch(m -> m.getId().equals(testShellMapping2.getId())))
        );
    }

    private CaseTypeEntity createCaseTypeWithField(String reference, String name, 
                                                   JurisdictionEntity jurisdiction,
                                                   FieldTypeEntity fieldType,
                                                   String fieldReference) {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(reference);
        caseType.setName(name);
        caseType.setVersion(1);
        caseType.setDescription(name);
        caseType.setJurisdiction(jurisdiction);
        caseType.setSecurityClassification(PUBLIC);
        
        CaseFieldEntity caseField = testHelper.buildCaseField(fieldReference, fieldType, "Label " + fieldReference, false);
        caseType.addCaseField(caseField);
        
        return caseTypeRepository.save(caseType);
    }

    private ShellMappingEntity createShellMappingEntity(
        LocalDate liveFrom,
        LocalDate liveTo,
        CaseTypeLiteEntity shellCaseTypeId,
        CaseFieldEntity shellCaseFieldName,
        CaseTypeLiteEntity originatingCaseTypeId,
        CaseFieldEntity originatingCaseFieldName) {
        ShellMappingEntity entity = new ShellMappingEntity();
        entity.setLiveFrom(liveFrom);
        entity.setLiveTo(liveTo);
        entity.setShellCaseTypeId(shellCaseTypeId);
        entity.setShellCaseFieldName(shellCaseFieldName);
        entity.setOriginatingCaseTypeId(originatingCaseTypeId);
        entity.setOriginatingCaseFieldName(originatingCaseFieldName);
        return entity;
    }

    private void saveShellMappingAndFlushSession(ShellMappingEntity... entities) {
        for (ShellMappingEntity entity : entities) {
            shellMappingRepository.save(entity);
        }
        entityManager.flush();
        entityManager.clear();
    }
}