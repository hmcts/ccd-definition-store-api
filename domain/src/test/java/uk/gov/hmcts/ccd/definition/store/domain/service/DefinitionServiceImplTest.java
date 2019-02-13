package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.exception.BadRequestException;
import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.DraftDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Definition;
import uk.gov.hmcts.ccd.definition.store.repository.model.DefinitionModelMapper;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.CREATE;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.UPDATE;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus.PUBLISHED;

class DefinitionServiceImplTest {

    private static final String JURISDICTION_ID = "TEST";

    @Mock
    private JurisdictionRepository jurisdictionRepository;

    @Mock
    private DraftDefinitionRepositoryDecorator decoratedRepository;

    @Mock
    private DefinitionModelMapper mapper;

    @Mock
    private JurisdictionEntity jurisdictionEntity;

    @Mock
    private Definition definition;

    @Mock
    private Definition persistedModel;

    @Mock
    private DefinitionEntity definitionEntity;

    @Mock
    private DefinitionEntity definitionEntity2;

    @Mock
    private DefinitionEntity definitionEntity3;

    @Mock
    private Definition definition2;

    @Captor
    private ArgumentCaptor<DefinitionEntity> entityCaptor;

    private DefinitionServiceImpl classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new DefinitionServiceImpl(jurisdictionRepository, decoratedRepository, mapper);
        setupMockJurisdictionEntity();
        setupMockDefinition(definition, "Mock definition", null);
        setupMockDefinitionEntity("Mock definition");
        setupMockDefinition(persistedModel, "Mock definition", DefinitionStatus.DRAFT);
    }

    @Test
    @DisplayName("Should create a draft Definition")
    void shouldCreateDraftDefinition() {
        when(jurisdictionRepository.findFirstByReferenceOrderByVersionDesc(anyString()))
            .thenReturn(Optional.of(jurisdictionEntity));
        when(definitionEntity.getJurisdiction()).thenReturn(jurisdictionEntity);

        when(mapper.toEntity(any(Definition.class))).thenReturn(definitionEntity);

        final DefinitionEntity persistedEntity = mock(DefinitionEntity.class);
        when(decoratedRepository.save(entityCaptor.capture())).thenReturn(persistedEntity);


        when(mapper.toModel(any(DefinitionEntity.class))).thenReturn(persistedModel);

        final Definition saved = classUnderTest.createDraftDefinition(definition).getResponseBody();

        verify(jurisdictionRepository).findFirstByReferenceOrderByVersionDesc(anyString());
        verify(mapper).toEntity(any(Definition.class));
        verify(decoratedRepository).save(any(DefinitionEntity.class));
        verify(mapper).toModel(any(DefinitionEntity.class));

        final DefinitionEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getJurisdiction().getId(), is(jurisdictionEntity.getId()));
        assertThat(capturedEntity.getDescription(), is(definitionEntity.getDescription()));
        assertThat(capturedEntity.getStatus(), is(nullValue()));
        assertThat(capturedEntity.getAuthor(), is(definitionEntity.getAuthor()));

        verify(persistedEntity, never()).setJurisdiction(any(JurisdictionEntity.class));
        verify(persistedEntity, never()).setDescription(anyString());
        verify(persistedEntity, never()).setStatus(any(DefinitionStatus.class));
        verify(persistedEntity, never()).setAuthor(anyString());

        assertThat(saved.getJurisdiction().getId(), is(definition.getJurisdiction().getId()));
        assertThat(saved.getDescription(), is(definition.getDescription()));
        assertThat(saved.getStatus(), is(DefinitionStatus.DRAFT));
        assertThat(saved.getAuthor(), is(definition.getAuthor()));
    }

    @Test
    @DisplayName("Should throw an exception if the Definition does not contain a Jurisdiction")
    void shouldThrowExceptionIfDefinitionDoesNotHaveJurisdiction() {
        when(definition.getJurisdiction()).thenReturn(null);

        Throwable exception =
            assertThrows(BadRequestException.class, () -> classUnderTest.preConditionCheck(definition));
        assertThat(exception.getMessage(), is("No Jurisdiction present in Definition"));
    }

    @Test
    @DisplayName("Should throw an exception if the Jurisdiction cannot be retrieved")
    void shouldThrowExceptionIfJurisdictionDoesNotExist() {
        when(jurisdictionRepository.findFirstByReferenceOrderByVersionDesc(anyString())).thenReturn(Optional.empty());

        Throwable exception =
            assertThrows(BadRequestException.class, () -> classUnderTest.createDraftDefinition(definition));
        assertThat(exception.getMessage(), is("Jurisdiction TEST could not be retrieved or does not exist"));
    }

    @Test
    @DisplayName("Should throw an exception if the Definition description is null")
    void shouldThrowExceptionIfDefinitionDescriptionIsNull() {
        when(definition.getDescription()).thenReturn(null);

        Throwable exception =
            assertThrows(BadRequestException.class, () -> classUnderTest.preConditionCheck(definition));
        assertThat(exception.getMessage(), is("Definition description cannot be null"));
    }

    @Test
    @DisplayName("Should throw an exception if the Definition author is null")
    void shouldThrowExceptionIfDefinitionAuthorIsNull() {
        when(definition.getAuthor()).thenReturn(null);

        Throwable exception =
            assertThrows(BadRequestException.class, () -> classUnderTest.preConditionCheck(definition));
        assertThat(exception.getMessage(), is("Definition author cannot be null"));
    }

    @Test
    @DisplayName("Should return null when repository finds nothing")
    void shouldReturnNullWhenRepositoryFindsNothing() {
        when(decoratedRepository.findByJurisdictionIdAndVersion("jurisdiction", -1)).thenReturn(null);
        assertNull(classUnderTest.findByJurisdictionIdAndVersion("jurisdiction", -1));
    }

    @Test
    @DisplayName("Should return a definition entity when repository finds the data and version not specified")
    void shouldReturnEntityWhenRepositoryFindsDataAndVersionNotSpecified() {
        when(decoratedRepository.findByJurisdictionIdAndVersion("jurisdiction", null))
            .thenReturn(definitionEntity);
        when(mapper.toModel(definitionEntity)).thenReturn(persistedModel);
        assertThat(classUnderTest.findByJurisdictionIdAndVersion("jurisdiction", null), is(persistedModel));
    }

    @Test
    @DisplayName("Should return a definition entity when repository finds the data with version specified")
    void shouldReturnEntityWhenRepositoryFindsDataAndVersionSpecfied() {
        when(decoratedRepository.findByJurisdictionIdAndVersion("jurisdiction", -1))
            .thenReturn(definitionEntity);
        when(mapper.toModel(definitionEntity)).thenReturn(definition);
        assertThat(classUnderTest.findByJurisdictionIdAndVersion("jurisdiction", -1), is(definition));
    }

    @Test
    @DisplayName("Should return a list of entities")
    void shouldReturnEntityList() {
        when(decoratedRepository.findByJurisdictionId("xyz"))
            .thenReturn(asList(definitionEntity, definitionEntity2, definitionEntity3));
        when(mapper.toModel(definitionEntity)).thenReturn(definition);
        when(mapper.toModel(definitionEntity2)).thenReturn(persistedModel);
        when(mapper.toModel(definitionEntity3)).thenReturn(definition2);
        final List<Definition> found = classUnderTest.findByJurisdictionId("xyz");
        assertAll(() -> assertThat(found.size(), is(3)),
            () -> assertThat(found.get(0), is(definition)),
            () -> assertThat(found.get(1), is(persistedModel)),
            () -> assertThat(found.get(2), is(definition2)));
    }

    @Test
    @DisplayName("Should return an empty list")
    void shouldReturnEmptyListWhenFoundNothing() {
        final List<Definition> list = classUnderTest.findByJurisdictionId("xyz");
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("Should save an existent draft Definition")
    void shouldSaveDraftDefinition() {
        when(definition.getVersion()).thenReturn(-9);
        when(decoratedRepository.findByJurisdictionIdAndVersion(JURISDICTION_ID, -9)).thenReturn(definitionEntity);
        final ServiceResponse<Definition> definitionServiceResponse = classUnderTest.saveDraftDefinition(definition);
        assertThat(definitionServiceResponse.getOperation(), is(UPDATE));
    }

    @Test
    @DisplayName("Should save a new draft Definition")
    void shouldSaveNewDraftDefinition() {
        when(jurisdictionRepository.findFirstByReferenceOrderByVersionDesc(JURISDICTION_ID))
            .thenReturn(Optional.of(jurisdictionEntity));
        when(definitionEntity.getJurisdiction()).thenReturn(jurisdictionEntity);
        when(mapper.toEntity(definition)).thenReturn(definitionEntity);

        final ServiceResponse<Definition> definitionServiceResponse = classUnderTest.saveDraftDefinition(definition);
        assertThat(definitionServiceResponse.getOperation(), is(CREATE));
    }

    @Test
    @DisplayName("Should not save a definition if it is published")
    void shouldNotSavePublishedDefinition() {
        when(definition.getVersion()).thenReturn(-33);
        when(decoratedRepository.findByJurisdictionIdAndVersion(JURISDICTION_ID, -33)).thenReturn(definitionEntity);
        when(definitionEntity.getStatus()).thenReturn(PUBLISHED);
        Throwable exception =
            assertThrows(BadRequestException.class, () -> classUnderTest.saveDraftDefinition(definition));
        assertThat(exception.getMessage(), is("Definition is live"));
    }

    @Test
    @DisplayName("Should not save a definition if it is deleted")
    void shouldNotSaveDeletedDefinition() {
        when(definition.getVersion()).thenReturn(-83);
        when(decoratedRepository.findByJurisdictionIdAndVersion(JURISDICTION_ID, -83)).thenReturn(definitionEntity);
        when(definitionEntity.isDeleted()).thenReturn(true);
        Throwable exception =
            assertThrows(BadRequestException.class, () -> classUnderTest.saveDraftDefinition(definition));
        assertThat(exception.getMessage(), is("Definition is deleted"));
    }

    @Test
    @DisplayName("Should delete a draft definition")
    void shouldDeleteDefinition() {
        when(decoratedRepository.findByJurisdictionIdAndVersion(JURISDICTION_ID, -98)).thenReturn(definitionEntity);
        classUnderTest.deleteDraftDefinition(JURISDICTION_ID, -98);
        verify(definitionEntity).setDeleted(true);
        verify(decoratedRepository).simpleSave(definitionEntity);
    }

    @Test
    @DisplayName("Should not delete a non existent draft definition")
    void shouldNotDeleteNotExistDefinition() {
        Throwable exception =
            assertThrows(BadRequestException.class, () -> classUnderTest.deleteDraftDefinition(JURISDICTION_ID, -96));
        assertThat(exception.getMessage(), is("Can't find draft definition"));
    }

    @Test
    @DisplayName("Should not delete a published draft definition")
    void shouldNotDeletePublishedDefinition() {
        when(decoratedRepository.findByJurisdictionIdAndVersion(JURISDICTION_ID, -97)).thenReturn(definitionEntity);
        when(definitionEntity.getStatus()).thenReturn(PUBLISHED);
        Throwable exception =
            assertThrows(BadRequestException.class, () -> classUnderTest.deleteDraftDefinition(JURISDICTION_ID, -97));
        assertThat(exception.getMessage(), is("Definition is live"));
        verify(definitionEntity, never()).setDeleted(true);
        verify(decoratedRepository, never()).simpleSave(definitionEntity);
    }

    @Test
    @DisplayName("Should not delete a deleted draft definition")
    void shouldNotDeleteDeletedDefinition() {
        when(decoratedRepository.findByJurisdictionIdAndVersion(JURISDICTION_ID, -99)).thenReturn(definitionEntity);
        when(definitionEntity.isDeleted()).thenReturn(true);
        Throwable exception =
            assertThrows(BadRequestException.class, () -> classUnderTest.deleteDraftDefinition(JURISDICTION_ID, -99));
        assertThat(exception.getMessage(), is("Draft definition is deleted"));
        verify(definitionEntity, never()).setDeleted(true);
        verify(decoratedRepository, never()).simpleSave(definitionEntity);
    }

    private void setupMockJurisdictionEntity() {
        when(jurisdictionEntity.getId()).thenReturn(1);
    }

    private void setupMockDefinition(final Definition definition,
                                     final String description,
                                     final DefinitionStatus status) {
        when(definition.getDescription()).thenReturn(description);
        when(definition.getStatus()).thenReturn(status);
        when(definition.getAuthor()).thenReturn("ccd@hmcts");
        final Jurisdiction jurisdiction = mock(Jurisdiction.class);
        when(definition.getJurisdiction()).thenReturn(jurisdiction);
        when(jurisdiction.getId()).thenReturn(JURISDICTION_ID);
    }

    private void setupMockDefinitionEntity(final String description) {
        when(definitionEntity.getDescription()).thenReturn(description);
        when(definitionEntity.getAuthor()).thenReturn("ccd@hmcts");
    }
}
