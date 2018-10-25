package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.repository.DraftDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Definition;
import uk.gov.hmcts.ccd.definition.store.repository.model.DefinitionModelMapper;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefinitionServiceImplTest {

    @Mock
    private DraftDefinitionRepositoryDecorator decoratedRepository;

    @Mock
    private DefinitionModelMapper mapper;

    @Mock
    private Definition definition;

    @Mock
    private DefinitionEntity definitionEntity;

    @Captor
    private ArgumentCaptor<DefinitionEntity> entityCaptor;

    private DefinitionServiceImpl classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new DefinitionServiceImpl(decoratedRepository, mapper);
        setupMockDefinition(definition, "Mock definition", null);
        setupMockDefinitionEntity("Mock definition");
    }

    @Test
    @DisplayName("Should create a draft Definition")
    void shouldCreateDraftDefinition() {
        final Jurisdiction jurisdiction = mock(Jurisdiction.class);
        when(definition.getJurisdiction()).thenReturn(jurisdiction);
        when(jurisdiction.getId()).thenReturn("TEST");
        when(mapper.toEntity(any(Definition.class))).thenReturn(definitionEntity);

        final JurisdictionEntity jurisdictionEntity = mock(JurisdictionEntity.class);
        when(definitionEntity.getJurisdiction()).thenReturn(jurisdictionEntity);
        when(jurisdictionEntity.getReference()).thenReturn("TEST");

        final DefinitionEntity persistedEntity = mock(DefinitionEntity.class);
        when(decoratedRepository.save(entityCaptor.capture())).thenReturn(persistedEntity);

        final Definition persistedModel = mock(Definition.class);
        setupMockDefinition(persistedModel, "Mock definition", DefinitionStatus.DRAFT);
        when(mapper.toModel(any(DefinitionEntity.class))).thenReturn(persistedModel);

        final Definition saved = classUnderTest.createDraftDefinition(definition).getResponseBody();

        verify(mapper).toEntity(any(Definition.class));
        verify(decoratedRepository).save(any(DefinitionEntity.class));
        verify(mapper).toModel(any(DefinitionEntity.class));

        final DefinitionEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getDescription(), is(definition.getDescription()));
        assertThat(capturedEntity.getStatus(), is(nullValue()));

        verify(persistedEntity, never()).setDescription(anyString());
        verify(persistedEntity, never()).setStatus(any());

        assertThat(saved.getDescription(), is(definition.getDescription()));
        assertThat(saved.getStatus(), is(DefinitionStatus.DRAFT));
    }

    private void setupMockDefinition(final Definition definition,
                                     final String description,
                                     final DefinitionStatus status) {
        when(definition.getDescription()).thenReturn(description);
        when(definition.getStatus()).thenReturn(status);
    }

    private void setupMockDefinitionEntity(final String description) {
        when(definitionEntity.getDescription()).thenReturn(description);
    }
}
