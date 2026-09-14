package uk.gov.hmcts.ccd.definition.store.domain.service.shellmapping;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.repository.ShellMappingRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMapping;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMappingResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShellMappingServiceImplTest {

    @Mock
    private ShellMappingRepository repository;

    @Mock
    private CaseTypeService caseTypeService;

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    @InjectMocks
    private ShellMappingServiceImpl sut;

    @Test
    @DisplayName("Should save single ShellMapping Entity")
    void shouldSaveSingleShellMappingEntity() {
        ShellMappingEntity mockedEntity = mock(ShellMappingEntity.class);
        List<ShellMappingEntity> entitiesToSave = Lists.newArrayList(mockedEntity);
        sut.saveAll(entitiesToSave);

        verify(repository, times(1)).saveAll(eq(entitiesToSave));
    }

    @Test
    @DisplayName("Should save multiple ShellMapping Entities")
    void shouldSaveMultipleShellMappingEntities() {
        ShellMappingEntity mockedEntity1 = mock(ShellMappingEntity.class);
        ShellMappingEntity mockedEntity2 = mock(ShellMappingEntity.class);
        ShellMappingEntity mockedEntity3 = mock(ShellMappingEntity.class);
        List<ShellMappingEntity> entitiesToSave = Lists.newArrayList(mockedEntity1, mockedEntity2, mockedEntity3);
        sut.saveAll(entitiesToSave);

        verify(repository, times(1)).saveAll(eq(entitiesToSave));
    }

    @Test
    @DisplayName("Should save empty list of ShellMapping Entities")
    void shouldSaveEmptyListOfShellMappingEntities() {
        List<ShellMappingEntity> entitiesToSave = new ArrayList<>();
        sut.saveAll(entitiesToSave);

        verify(repository, times(1)).saveAll(eq(entitiesToSave));
    }

    @Test
    @DisplayName("Should find all ShellMapping entities and map them to DTOs")
    void shouldFindAllShellMappingEntitiesAndMapToDTOs() {
        // Given
        ShellMappingEntity entity1 = createShellMappingEntity("SHELL_TYPE_1", "shellField1",
            "ORIG_TYPE_1", "origField1");
        ShellMappingEntity entity2 = createShellMappingEntity("SHELL_TYPE_2", "shellField2",
            "ORIG_TYPE_2", "origField2");
        List<ShellMappingEntity> entities = Lists.newArrayList(entity1, entity2);

        ShellMapping dto1 = createShellMappingDTO("SHELL_TYPE_1", "shellField1",
            "ORIG_TYPE_1", "origField1");
        ShellMapping dto2 = createShellMappingDTO("SHELL_TYPE_2", "shellField2",
            "ORIG_TYPE_2", "origField2");

        when(repository.findAll()).thenReturn(entities);
        when(dtoMapper.map(entity1)).thenReturn(dto1);
        when(dtoMapper.map(entity2)).thenReturn(dto2);

        // When
        List<ShellMapping> result = sut.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("SHELL_TYPE_1", result.get(0).getShellCaseTypeId());
        assertEquals("shellField1", result.get(0).getShellCaseFieldName());
        assertEquals("ORIG_TYPE_1", result.get(0).getOriginatingCaseTypeId());
        assertEquals("origField1", result.get(0).getOriginatingCaseFieldName());
        assertEquals("SHELL_TYPE_2", result.get(1).getShellCaseTypeId());
        assertEquals("shellField2", result.get(1).getShellCaseFieldName());
        assertEquals("ORIG_TYPE_2", result.get(1).getOriginatingCaseTypeId());
        assertEquals("origField2", result.get(1).getOriginatingCaseFieldName());

        verify(repository, times(1)).findAll();
        verify(dtoMapper, times(1)).map(entity1);
        verify(dtoMapper, times(1)).map(entity2);
    }

    @Test
    @DisplayName("Should return empty list when no ShellMapping entities exist")
    void shouldReturnEmptyListWhenNoShellMappingEntitiesExist() {
        // Given
        when(repository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<ShellMapping> result = sut.findAll();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(repository, times(1)).findAll();
        verify(dtoMapper, times(0)).map(any(ShellMappingEntity.class));
    }

    @Test
    @DisplayName("Should find all ShellMapping entities with live dates")
    void shouldFindAllShellMappingEntitiesWithLiveDates() {
        // Given
        ShellMappingEntity entity = createShellMappingEntity("SHELL_TYPE_1", "shellField1",
            "ORIG_TYPE_1", "origField1");
        entity.setLiveFrom(LocalDate.of(2024, 1, 1));
        entity.setLiveTo(LocalDate.of(2024, 12, 31));

        ShellMapping dto = createShellMappingDTO("SHELL_TYPE_1", "shellField1",
            "ORIG_TYPE_1", "origField1");

        when(repository.findAll()).thenReturn(Lists.newArrayList(entity));
        when(dtoMapper.map(entity)).thenReturn(dto);

        // When
        List<ShellMapping> result = sut.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findAll();
        verify(dtoMapper, times(1)).map(entity);
    }

    @Nested
    @DisplayName("findByOriginatingCaseTypeId tests")
    class FindByOriginatingCaseTypeIdTests {

        @Test
        @DisplayName("Should call caseTypeService.findByCaseTypeId and return"
            + " shell mapping response when case type exists")
        void shouldCallCaseTypeServiceAndReturnResponseWhenCaseTypeExists() {
            // Given
            String caseTypeId = "ORIG_TYPE_1";
            CaseType caseType = new CaseType();
            caseType.setId(caseTypeId);

            ShellMappingEntity entity1 = createShellMappingEntity("SHELL_TYPE_1", "shellField1",
                "ORIG_TYPE_1", "origField1");
            ShellMappingEntity entity2 = createShellMappingEntity("SHELL_TYPE_1", "shellField2",
                "ORIG_TYPE_1", "origField2");
            List<ShellMappingEntity> entities = Lists.newArrayList(entity1, entity2);

            when(caseTypeService.findByCaseTypeId(caseTypeId)).thenReturn(Optional.of(caseType));
            when(repository.findByOriginatingCaseTypeIdReference(caseTypeId)).thenReturn(entities);

            // When
            ShellMappingResponse result = sut.findByOriginatingCaseTypeId(caseTypeId);

            // Then
            assertNotNull(result);
            assertThat(result.getShellCaseTypeID(), equalTo("SHELL_TYPE_1"));
            assertThat(result.getShellCaseMappings(), hasSize(2));
            assertThat(result.getShellCaseMappings().get(0).getOriginatingCaseFieldName(), equalTo("origField1"));
            assertThat(result.getShellCaseMappings().get(0).getShellCaseFieldName(), equalTo("shellField1"));
            assertThat(result.getShellCaseMappings().get(1).getOriginatingCaseFieldName(), equalTo("origField2"));
            assertThat(result.getShellCaseMappings().get(1).getShellCaseFieldName(), equalTo("shellField2"));

            verify(caseTypeService, times(1)).findByCaseTypeId(caseTypeId);
            verify(repository, times(1)).findByOriginatingCaseTypeIdReference(caseTypeId);
        }

        @Test
        @DisplayName("Should throw CaseTypeValidationException when case type does not exist")
        void shouldThrowCaseTypeValidationExceptionWhenCaseTypeDoesNotExist() {
            // Given
            String caseTypeId = "NON_EXISTENT_CASE_TYPE";
            when(caseTypeService.findByCaseTypeId(caseTypeId)).thenReturn(Optional.empty());

            // When & Then
            CaseTypeValidationException exception = assertThrows(CaseTypeValidationException.class,
                () -> sut.findByOriginatingCaseTypeId(caseTypeId));

            assertThat(exception.getErrors().size(), equalTo(1));
            assertThat(exception.getErrors().iterator().next(), equalTo("Case Type not found " + caseTypeId));

            verify(caseTypeService, times(1)).findByCaseTypeId(caseTypeId);
            verify(repository, times(0)).findByOriginatingCaseTypeIdReference(any());
        }

        @Test
        @DisplayName("Should call caseTypeService.findByCaseTypeId with correct parameter")
        void shouldCallCaseTypeServiceWithCorrectParameter() {
            // Given
            String caseTypeId = "ORIG_TYPE_1";
            CaseType caseType = new CaseType();
            ShellMappingEntity entity = createShellMappingEntity("SHELL_TYPE_1", "shellField1",
                "ORIG_TYPE_1", "origField1");

            when(caseTypeService.findByCaseTypeId(caseTypeId)).thenReturn(Optional.of(caseType));
            when(repository.findByOriginatingCaseTypeIdReference(caseTypeId)).thenReturn(Lists.newArrayList(entity));

            // When
            sut.findByOriginatingCaseTypeId(caseTypeId);

            // Then
            verify(caseTypeService, times(1)).findByCaseTypeId(eq(caseTypeId));
        }

        @Test
        @DisplayName("Should throw NotFoundException when case type exists but no shell mappings found")
        void shouldThrowNotFoundExceptionWhenCaseTypeExistsButNoShellMappingsFound() {
            // Given
            String caseTypeId = "ORIG_TYPE_1";
            CaseType caseType = new CaseType();
            when(caseTypeService.findByCaseTypeId(caseTypeId)).thenReturn(Optional.of(caseType));
            when(repository.findByOriginatingCaseTypeIdReference(caseTypeId)).thenReturn(new ArrayList<>());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class,
                () -> sut.findByOriginatingCaseTypeId(caseTypeId));

            assertThat(exception.getMessage(), equalTo("No Shell case found for case type id " + caseTypeId));

            verify(caseTypeService, times(1)).findByCaseTypeId(caseTypeId);
            verify(repository, times(1)).findByOriginatingCaseTypeIdReference(caseTypeId);
        }
    }

    private ShellMappingEntity createShellMappingEntity(String shellCaseTypeRef, String shellCaseFieldRef,
                                                        String origCaseTypeRef, String origCaseFieldRef) {
        ShellMappingEntity entity = new ShellMappingEntity();
        entity.setShellCaseTypeId(createCaseTypeLiteEntity(shellCaseTypeRef));
        entity.setShellCaseFieldName(createCaseFieldEntity(shellCaseFieldRef));
        entity.setOriginatingCaseTypeId(createCaseTypeLiteEntity(origCaseTypeRef));
        entity.setOriginatingCaseFieldName(createCaseFieldEntity(origCaseFieldRef));
        return entity;
    }

    private CaseTypeLiteEntity createCaseTypeLiteEntity(String reference) {
        CaseTypeLiteEntity entity = new CaseTypeLiteEntity();
        entity.setReference(reference);
        return entity;
    }

    private CaseFieldEntity createCaseFieldEntity(String reference) {
        CaseFieldEntity entity = new CaseFieldEntity();
        entity.setReference(reference);
        return entity;
    }

    private ShellMapping createShellMappingDTO(String shellCaseTypeId, String shellCaseFieldName,
                                                String originatingCaseTypeId, String originatingCaseFieldName) {
        ShellMapping dto = new ShellMapping();
        dto.setShellCaseTypeId(shellCaseTypeId);
        dto.setShellCaseFieldName(shellCaseFieldName);
        dto.setOriginatingCaseTypeId(originatingCaseTypeId);
        dto.setOriginatingCaseFieldName(originatingCaseFieldName);
        return dto;
    }
}
