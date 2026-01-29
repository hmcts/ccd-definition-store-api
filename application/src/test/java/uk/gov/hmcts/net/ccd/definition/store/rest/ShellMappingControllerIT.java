package uk.gov.hmcts.net.ccd.definition.store.rest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionRepository;
import uk.gov.hmcts.ccd.definition.store.repository.ShellMappingRepository;
import uk.gov.hmcts.ccd.definition.store.repository.VersionedDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMappingResponse;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity.toCaseTypeLiteEntity;

@Transactional
class ShellMappingControllerIT extends BaseTest {

    private static final String RETRIEVE_SHELL_MAPPINGS_URL = "/api/retrieve-shell-mappings";

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private ShellMappingRepository shellMappingRepository;

    @Inject
    private JurisdictionRepository jurisdictionRepository;

    @Inject
    private FieldTypeRepository fieldTypeRepository;

    @Inject
    private CaseTypeRepository caseTypeRepository;

    @BeforeEach
    void setUp() {
        VersionedDefinitionRepositoryDecorator<JurisdictionEntity, Integer> versionedJurisdictionRepository =
            new VersionedDefinitionRepositoryDecorator<>(jurisdictionRepository);
        VersionedDefinitionRepositoryDecorator<FieldTypeEntity, Integer> versionedFieldTypeRepository =
            new VersionedDefinitionRepositoryDecorator<>(fieldTypeRepository);
        VersionedDefinitionRepositoryDecorator<CaseTypeEntity, Integer> versionedCaseTypeRepository =
            new VersionedDefinitionRepositoryDecorator<>(caseTypeRepository);

        // Create jurisdictions
        JurisdictionEntity jurisdiction1 = createJurisdiction(versionedJurisdictionRepository,
            "JURISDICTION_1", "Jurisdiction 1", "Description 1");
        JurisdictionEntity jurisdiction2 = createJurisdiction(versionedJurisdictionRepository,
            "JURISDICTION_2",
            "Jurisdiction 2",
            "Description 2");

        // Create field types
        FieldTypeEntity fieldType1 = createFieldType(versionedFieldTypeRepository, jurisdiction1);
        FieldTypeEntity fieldType2 = createFieldType(versionedFieldTypeRepository, jurisdiction2);

        // Create shell case types with fields
        final CaseTypeEntity shellCaseType1 = createCaseTypeWithField(versionedCaseTypeRepository,
            "SHELL_CASE_TYPE_1",
            "Shell Case Type 1",
            jurisdiction1,
            fieldType1,
            "shellField1");
        final CaseTypeEntity shellCaseType2 = createCaseTypeWithField(versionedCaseTypeRepository,
            "SHELL_CASE_TYPE_2",
            "Shell Case Type 2",
            jurisdiction2,
            fieldType2,
            "shellField2");

        // Create originating case types with fields
        final CaseTypeEntity origCaseType1 = createCaseTypeWithField(versionedCaseTypeRepository,
            "ORIG_CASE_TYPE_1",
            "Originating Case Type 1",
            jurisdiction1,
            fieldType1,
            "origField1");
        final CaseTypeEntity origCaseType2 = createCaseTypeWithField(versionedCaseTypeRepository,
            "ORIG_CASE_TYPE_2",
            "Originating Case Type 2",
            jurisdiction2,
            fieldType2,
            "origField2");

        // Add another field to origCaseType1 for testing multiple mappings
        CaseFieldEntity origCaseField1b = new CaseFieldEntity();
        origCaseField1b.setReference("origField1b");
        origCaseField1b.setFieldType(fieldType1);
        origCaseField1b.setLabel("origField1b");
        origCaseField1b.setHidden(false);
        origCaseField1b.setSecurityClassification(PUBLIC);
        origCaseType1.addCaseField(origCaseField1b);
        caseTypeRepository.save(origCaseType1);
        entityManager.flush();

        // Get case fields
        CaseFieldEntity shellCaseField1 = shellCaseType1.getCaseFields().stream()
            .filter(f -> "shellField1".equals(f.getReference()))
            .findFirst().orElseThrow();
        CaseFieldEntity shellCaseField2 = shellCaseType2.getCaseFields().stream()
            .filter(f -> "shellField2".equals(f.getReference()))
            .findFirst().orElseThrow();
        CaseFieldEntity origCaseField1 = origCaseType1.getCaseFields().stream()
            .filter(f -> "origField1".equals(f.getReference()))
            .findFirst().orElseThrow();
        CaseFieldEntity origCaseField1bEntity = origCaseType1.getCaseFields().stream()
            .filter(f -> "origField1b".equals(f.getReference()))
            .findFirst().orElseThrow();
        CaseFieldEntity origCaseField2 = origCaseType2.getCaseFields().stream()
            .filter(f -> "origField2".equals(f.getReference()))
            .findFirst().orElseThrow();

        // Create shell mappings
        CaseTypeLiteEntity shellCaseTypeLite1 = toCaseTypeLiteEntity(shellCaseType1);
        CaseTypeLiteEntity shellCaseTypeLite2 = toCaseTypeLiteEntity(shellCaseType2);
        CaseTypeLiteEntity origCaseTypeLite1 = toCaseTypeLiteEntity(origCaseType1);
        CaseTypeLiteEntity origCaseTypeLite2 = toCaseTypeLiteEntity(origCaseType2);

        ShellMappingEntity shellMapping1 = createShellMappingEntity(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31),
            shellCaseTypeLite1,
            shellCaseField1,
            origCaseTypeLite1,
            origCaseField1
        );

        ShellMappingEntity shellMapping2 = createShellMappingEntity(
            LocalDate.of(2024, 6, 1),
            null,
            shellCaseTypeLite2,
            shellCaseField2,
            origCaseTypeLite2,
            origCaseField2
        );

        // Create another mapping for the same originating case type to test multiple results
        // This maps a different originating field to the same shell field
        ShellMappingEntity shellMapping3 = createShellMappingEntity(
            LocalDate.of(2024, 1, 1),
            null,
            shellCaseTypeLite1,
            shellCaseField1,
            origCaseTypeLite1,
            origCaseField1bEntity
        );

        // Save and flush
        shellMappingRepository.save(shellMapping1);
        shellMappingRepository.save(shellMapping2);
        shellMappingRepository.save(shellMapping3);
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("GET /api/retrieve-shell-mappings")
    class RetrieveShellMappingsTests {

        @Test
        @DisplayName("Should return shell mappings for valid originating case type ID")
        void shouldReturnShellMappingsForValidOriginatingCaseTypeId() throws Exception {
            final String url = RETRIEVE_SHELL_MAPPINGS_URL + "/ORIG_CASE_TYPE_1";
            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.shellCaseTypeID").exists())
                .andExpect(jsonPath("$.shellCaseMappings").isArray())
                .andReturn();

            ShellMappingResponse response = mapper.readValue(
                result.getResponse().getContentAsString(),
                ShellMappingResponse.class
            );

            assertAll(
                () -> assertThat(response.getShellCaseTypeID(), equalTo("SHELL_CASE_TYPE_1")),
                () -> assertThat(response.getShellCaseMappings(), hasSize(2)),
                () -> assertThat(response.getShellCaseMappings(), hasItem(hasProperty("originatingCaseFieldName",
                    equalTo("origField1")))),
                () -> assertThat(response.getShellCaseMappings(), hasItem(hasProperty("originatingCaseFieldName",
                    equalTo("origField1b")))),
                () -> assertThat(response.getShellCaseMappings(), hasItem(hasProperty("shellCaseFieldName",
                    equalTo("shellField1"))))
            );
        }

        @Test
        @DisplayName("Should return single shell mapping when only one exists")
        void shouldReturnSingleShellMapping() throws Exception {
            final String url = RETRIEVE_SHELL_MAPPINGS_URL + "/ORIG_CASE_TYPE_2";
            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.shellCaseTypeID").exists())
                .andExpect(jsonPath("$.shellCaseMappings").isArray())
                .andReturn();

            ShellMappingResponse response = mapper.readValue(
                result.getResponse().getContentAsString(),
                ShellMappingResponse.class
            );

            assertAll(
                () -> assertThat(response.getShellCaseTypeID(), equalTo("SHELL_CASE_TYPE_2")),
                () -> assertThat(response.getShellCaseMappings(), hasSize(1)),
                () -> assertThat(response.getShellCaseMappings().get(0).getOriginatingCaseFieldName(),
                    equalTo("origField2")),
                () -> assertThat(response.getShellCaseMappings().get(0).getShellCaseFieldName(),
                    equalTo("shellField2"))
            );
        }

        @Test
        @DisplayName("Should return 400 Bad Request when case type does not exist")
        void shouldReturnBadRequestWhenCaseTypeDoesNotExist() throws Exception {
            final String url = RETRIEVE_SHELL_MAPPINGS_URL + "/NON_EXISTENT_CASE_TYPE";
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Should return correct JSON structure")
        void shouldReturnCorrectJsonStructure() throws Exception {
            final String url = RETRIEVE_SHELL_MAPPINGS_URL + "/ORIG_CASE_TYPE_1";
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.shellCaseTypeID").exists())
                .andExpect(jsonPath("$.shellCaseMappings").isArray())
                .andExpect(jsonPath("$.shellCaseMappings[0].OriginatingCaseFieldName").exists())
                .andExpect(jsonPath("$.shellCaseMappings[0].ShellCaseFieldName").exists());
        }
    }

    private JurisdictionEntity createJurisdiction(
        VersionedDefinitionRepositoryDecorator<JurisdictionEntity, Integer> repository,
        String reference, String name, String description) {
        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference(reference);
        jurisdiction.setName(name);
        jurisdiction.setDescription(description);
        return repository.save(jurisdiction);
    }

    private FieldTypeEntity createFieldType(
        VersionedDefinitionRepositoryDecorator<FieldTypeEntity, Integer> repository,
        JurisdictionEntity jurisdiction) {
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference("Text");
        fieldType.setMinimum("2");
        fieldType.setMaximum("4");
        fieldType.setJurisdiction(jurisdiction);
        return repository.save(fieldType);
    }

    private CaseTypeEntity createCaseTypeWithField(
        VersionedDefinitionRepositoryDecorator<CaseTypeEntity, Integer> repository,
        String reference, String name,
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

        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference(fieldReference);
        caseField.setFieldType(fieldType);
        caseField.setLabel(fieldReference);
        caseField.setHidden(false);
        caseField.setSecurityClassification(PUBLIC);
        caseType.addCaseField(caseField);

        return repository.save(caseType);
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
}
