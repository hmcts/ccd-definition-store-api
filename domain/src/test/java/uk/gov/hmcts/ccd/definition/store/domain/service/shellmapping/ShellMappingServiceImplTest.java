package uk.gov.hmcts.ccd.definition.store.domain.service.shellmapping;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.ShellMappingRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
