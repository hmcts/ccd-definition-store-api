package uk.gov.hmcts.ccd.definition.store.elastic;

import co.elastic.clients.elasticsearch.indices.AliasDefinition;
import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import co.elastic.clients.elasticsearch.indices.get_alias.IndexAliases;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexService;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexServiceImpl;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ReindexTask;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReindexServiceTest {

    public static final String TEST_USER_EMAIL = "testUser@hmcts.net";
    private ReindexService reindexService;

    @Mock
    private HighLevelCCDElasticClient ccdElasticClient;

    @Mock
    private CaseMappingGenerator caseMappingGenerator;

    @Mock
    private ObjectFactory<HighLevelCCDElasticClient> clientFactory;

    @Mock
    private CcdElasticSearchProperties config;

    @Mock
    private ReindexRepository reindexRepository;

    @Mock
    private EntityToResponseDTOMapper mapper;

    private final CaseTypeEntity caseA = new CaseTypeBuilder().withJurisdiction("jurA")
        .withReference("caseTypeA").build();
    private final String baseIndexName = "casetypea";
    private final String oldIndexName = "casetypea_cases-000001";
    private final String newIndexName = "casetypea_cases-000002";
    private final String caseTypeName = oldIndexName;
    private final String incrementedCaseTypeName = newIndexName;


    @BeforeEach
    void setUp() {
        lenient().when(clientFactory.getObject()).thenReturn(ccdElasticClient);
        lenient().when(config.getCasesIndexNameFormat()).thenReturn("%s");

        reindexService = new ReindexServiceImpl(
            reindexRepository,
            mapper,
            caseMappingGenerator,
            clientFactory
        );
    }

    @Test
    void initialiseElasticSearchWhenReindexAndDeleteOldIndexAreTrue() throws IOException {
        mockSuccessfulReindex();

        DefinitionImportedEvent event = newEvent(true, true, caseA);
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(new ReindexEntity()));
        reindexService.asyncReindex(event, baseIndexName, caseA);

        // index is set read-only on the concrete index name, not the alias
        verify(ccdElasticClient).setIndexReadOnly(caseTypeName, true);
        verify(reindexRepository, times(2)).saveAndFlush(any(ReindexEntity.class));
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(newIndexName, "caseMapping");
        verify(ccdElasticClient).reindexData(eq(oldIndexName), eq(newIndexName), any());
        // on success we reset read-only on the old concrete index
        verify(ccdElasticClient).setIndexReadOnly(caseTypeName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, oldIndexName, newIndexName);
        verify(reindexRepository, times(2)).saveAndFlush(any(ReindexEntity.class));

        verify(ccdElasticClient).removeIndex(oldIndexName);
        ArgumentCaptor<String> oldIndexCaptor = ArgumentCaptor.forClass(String.class);
        verify(ccdElasticClient).removeIndex(oldIndexCaptor.capture());
        assertEquals(oldIndexName, oldIndexCaptor.getValue());
    }

    @Test
    void initialiseElasticSearchWhenReindexTrueAndDeleteOldIndexFalse() throws IOException {
        mockSuccessfulReindex();

        DefinitionImportedEvent event = newEvent(true, false, caseA);
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(new ReindexEntity()));
        reindexService.asyncReindex(event, baseIndexName, caseA);

        verify(reindexRepository, times(2)).saveAndFlush(any(ReindexEntity.class));
        verify(ccdElasticClient).setIndexReadOnly(caseTypeName, true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(newIndexName, "caseMapping");
        verify(ccdElasticClient).reindexData(eq(oldIndexName), eq(newIndexName), any());
        verify(ccdElasticClient).setIndexReadOnly(caseTypeName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, oldIndexName, newIndexName);
        verify(reindexRepository, times(2)).saveAndFlush(any(ReindexEntity.class));
        verify(ccdElasticClient, never()).removeIndex(oldIndexName);
    }

    @Test
    void shouldCleanupWhenCreateIndexAndMappingFails() throws IOException {
        mockAliasResponse();
        when(ccdElasticClient.createIndexAndMapping(anyString(), anyString()))
            .thenThrow(new IOException("put mapping failed"));

        DefinitionImportedEvent event = new DefinitionImportedEvent(newArrayList(caseA), true, true, TEST_USER_EMAIL);

        assertThrows(IOException.class,
            () -> reindexService.asyncReindex(event, baseIndexName, caseA));

        verify(ccdElasticClient).createIndexAndMapping(incrementedCaseTypeName, "caseMapping");
        verify(ccdElasticClient).removeIndex(incrementedCaseTypeName);
        verify(ccdElasticClient).setIndexReadOnly(caseTypeName, false);
        verify(ccdElasticClient, never()).reindexData(anyString(), anyString(), any());
    }

    @Test
    void deletesNewIndexOnFailedReindex() throws IOException {
        mockFailedReindex();

        DefinitionImportedEvent event = newEvent(true, true, caseA);

        assertThrows(RuntimeException.class,
            () -> reindexService.asyncReindex(event, baseIndexName, caseA));

        verify(ccdElasticClient).removeIndex(incrementedCaseTypeName);
        // on reindex failure we reset read-only using the alias name
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        //using a single mock, so close() is called twice (in event listener and reindexing failure handler)
        verify(ccdElasticClient, atLeast(2)).close();
    }

    @Test
    void shouldInitiateReindexEntity() {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("caseTypeA");
        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference("jurA");
        caseType.setJurisdiction(jurisdiction);

        ArgumentCaptor<ReindexEntity> captor = ArgumentCaptor.forClass(ReindexEntity.class);
        when(reindexRepository.saveAndFlush(any(ReindexEntity.class))).thenAnswer(i -> i.getArgument(0));

        ReindexEntity result = reindexService.saveEntity(
            true, caseType, newIndexName, TEST_USER_EMAIL
        );

        verify(reindexRepository).saveAndFlush(captor.capture());

        ReindexEntity saved = captor.getValue();
        assertSame(saved, result);
        assertTrue(saved.getDeleteOldIndex());
        assertEquals("caseTypeA", saved.getCaseType());
        assertEquals("jurA", saved.getJurisdiction());
        assertEquals(newIndexName, saved.getIndexName());
        assertNotNull(saved.getStartTime());
        assertEquals("STARTED", saved.getStatus());
    }

    @Test
    void shouldUpdateEntitySuccess() {
        ReindexEntity existing = new ReindexEntity();
        existing.setIndexName(newIndexName);

        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(existing));

        reindexService.updateEntity(newIndexName, "response", TEST_USER_EMAIL);

        verify(reindexRepository).saveAndFlush(existing);
        assertEquals("SUCCESS", existing.getStatus());
        assertNotNull(existing.getEndTime());
        assertNotNull(existing.getReindexResponse());
    }

    @Test
    void shouldUpdateEntityFailure() {
        ReindexEntity existing = new ReindexEntity();
        existing.setIndexName(newIndexName);

        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(existing));

        RuntimeException ex = new RuntimeException("Simulated failure");
        reindexService.updateEntity(newIndexName, ex, TEST_USER_EMAIL);

        verify(reindexRepository).saveAndFlush(existing);
        assertEquals("FAILED", existing.getStatus());
        assertNotNull(existing.getEndTime());
        assertTrue(existing.getExceptionMessage().contains("Simulated failure"));
        assertNull(existing.getReindexResponse());
    }

    @Test
    void shouldPersistFailureElasticsearchStatusExceptionBeforeReindex() throws IOException {
        mockAliasResponse();

        when(caseMappingGenerator.generateMapping(any()))
            .thenThrow(new ElasticsearchStatusException("ES error", RestStatus.BAD_REQUEST));

        ReindexEntity entity = new ReindexEntity();
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(entity));

        DefinitionImportedEvent event = new DefinitionImportedEvent(
            Collections.singletonList(caseA), true, true, TEST_USER_EMAIL
        );

        assertThrows(ElasticsearchStatusException.class,
            () -> reindexService.asyncReindex(event, baseIndexName, caseA));

        verify(reindexRepository).saveAndFlush(entity);
        assertEquals("FAILED", entity.getStatus());
        assertTrue(entity.getExceptionMessage().contains("ES error"));
    }

    @Test
    void triggerPersistFailureOnFailedReindex() throws IOException {
        mockFailedReindex();

        ReindexEntity entity = new ReindexEntity();
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(entity));

        DefinitionImportedEvent event = new DefinitionImportedEvent(
            Collections.singletonList(caseA), true, true, TEST_USER_EMAIL);

        assertThrows(RuntimeException.class,
            () -> reindexService.asyncReindex(event, baseIndexName, caseA));

        ArgumentCaptor<ReindexEntity> captor = ArgumentCaptor.forClass(ReindexEntity.class);
        verify(reindexRepository, times(2)).saveAndFlush(captor.capture());
        assertEquals("FAILED", entity.getStatus());
        assertTrue(entity.getExceptionMessage().contains("reindexing failed"));
    }

    @Test
    void triggerPersistFailureWhenReindexFailsBeforeHandleReindexing() throws IOException {
        mockAliasResponse();

        when(caseMappingGenerator.generateMapping(any())).thenThrow(
            new RuntimeException("mapping failure before reindex"));

        ReindexEntity entity = new ReindexEntity();
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(entity));

        DefinitionImportedEvent event = new DefinitionImportedEvent(
            Collections.singletonList(caseA), true, true, TEST_USER_EMAIL
        );

        assertThrows(RuntimeException.class,
            () -> reindexService.asyncReindex(event, baseIndexName, caseA));

        verify(reindexRepository).saveAndFlush(entity);
        assertEquals("FAILED", entity.getStatus());
        assertTrue(entity.getExceptionMessage().contains("mapping failure before reindex"));
    }

    @Test
    void shouldUnwrapCompletionException() {
        Throwable root = new IllegalArgumentException("Root cause");
        CompletionException completion = new CompletionException(root);

        ReindexEntity entity = new ReindexEntity();
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(entity));

        reindexService.updateEntity(newIndexName, completion, TEST_USER_EMAIL);

        verify(reindexRepository).saveAndFlush(entity);
        assertTrue(entity.getExceptionMessage().contains("IllegalArgumentException"));
    }

    @Test
    void shouldSkipMarkSuccessIfEntityNotFound() {
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> reindexService.updateEntity(newIndexName, "response", TEST_USER_EMAIL));

        assertTrue(exception.getMessage().contains("No reindex entity metadata found for index name"));
        verify(reindexRepository, never()).save(any());
    }

    @Test
    void shouldSkipMarkFailureIfEntityNotFound() {
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.empty());

        reindexService.updateEntity(newIndexName, new RuntimeException("Fail"), TEST_USER_EMAIL);

        verify(reindexRepository, never()).save(any());
    }

    @Test
    void shouldNotUnwrapRegularException() {
        RuntimeException regularException = new RuntimeException("Regular exception");

        ReindexEntity entity = new ReindexEntity();
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(entity));

        reindexService.updateEntity(newIndexName, regularException, TEST_USER_EMAIL);

        verify(reindexRepository).saveAndFlush(entity);
        assertTrue(entity.getExceptionMessage().contains("RuntimeException"));
        assertTrue(entity.getExceptionMessage().contains("Regular exception"));
    }

    @Test
    void shouldIncrementIndexNumber() {
        String result = reindexService.incrementIndexNumber(oldIndexName);
        assertEquals(newIndexName, result);
    }

    @Test
    void shouldReturnMappedTasksWithDurationFromGetAll() {
        ReindexEntity entity = new ReindexEntity();
        ReindexTask mappedTask = new ReindexTask();
        LocalDateTime start = LocalDateTime.of(2026, 4, 28, 14, 30, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 28, 14, 45, 30);
        mappedTask.setStartTime(start);
        mappedTask.setEndTime(end);

        when(reindexRepository.findAll()).thenReturn(List.of(entity));
        when(mapper.map(entity)).thenReturn(mappedTask);

        List<ReindexTask> result = reindexService.getAll();

        assertEquals(1, result.size());
        assertSame(mappedTask, result.get(0));
        assertEquals(start, result.get(0).getStartTime());
        assertEquals(end, result.get(0).getEndTime());
        assertEquals(930L, result.get(0).getDuration());
    }

    @Test
    void shouldReturnMappedTasksByCaseTypeWithDuration() {
        ReindexEntity entity = new ReindexEntity();
        ReindexTask mappedTask = new ReindexTask();
        LocalDateTime start = LocalDateTime.of(2026, 4, 28, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 28, 10, 1, 5);
        mappedTask.setStartTime(start);
        mappedTask.setEndTime(end);

        when(reindexRepository.findByCaseType("caseTypeA")).thenReturn(List.of(entity));
        when(mapper.map(entity)).thenReturn(mappedTask);

        List<ReindexTask> result = reindexService.getTasksByCaseType("caseTypeA");

        assertEquals(1, result.size());
        assertSame(mappedTask, result.get(0));
        assertEquals(65L, result.get(0).getDuration());
    }

    @Test
    void shouldReturnMappedTasksWithLongDurationValue() {
        ReindexEntity entity = new ReindexEntity();
        ReindexTask mappedTask = new ReindexTask();
        LocalDateTime start = LocalDateTime.of(2026, 4, 20, 8, 15, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 28, 11, 45, 30);
        mappedTask.setStartTime(start);
        mappedTask.setEndTime(end);

        when(reindexRepository.findByCaseType("caseTypeA")).thenReturn(List.of(entity));
        when(mapper.map(entity)).thenReturn(mappedTask);

        List<ReindexTask> result = reindexService.getTasksByCaseType("caseTypeA");

        assertEquals(1, result.size());
        assertSame(mappedTask, result.get(0));
        assertEquals(703_830L, result.get(0).getDuration());
        assertTrue(result.get(0).getDuration() > 86_400L);
    }

    @Test
    void shouldReturnPagedTasksForBlankCaseType() {
        ReindexEntity entity = new ReindexEntity();
        ReindexTask mappedTask = new ReindexTask();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReindexEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(reindexRepository.findAll(pageable)).thenReturn(entityPage);
        when(mapper.map(entity)).thenReturn(mappedTask);

        Page<ReindexTask> result = reindexService.getTasksByCaseType("", pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(mappedTask, result.getContent().get(0));
    }

    @Test
    void shouldReturnPagedTasksFilteredByCaseType() {
        ReindexEntity entity = new ReindexEntity();
        ReindexTask mappedTask = new ReindexTask();
        Pageable pageable = PageRequest.of(1, 5);
        Page<ReindexEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 6);

        when(reindexRepository.findByCaseType("caseTypeA", pageable)).thenReturn(entityPage);
        when(mapper.map(entity)).thenReturn(mappedTask);

        Page<ReindexTask> result = reindexService.getTasksByCaseType("caseTypeA", pageable);

        assertEquals(6, result.getTotalElements());
        assertSame(mappedTask, result.getContent().get(0));
    }

    @Test
    void incrementToDoubleDigitIndexNumber() {
        String result = reindexService.incrementIndexNumber("casetypea_cases-000009");
        assertEquals("casetypea_cases-000010", result);
    }

    @Test
    void incrementIndexNumberWithDash() {
        String result = reindexService.incrementIndexNumber("casetype-a_cases-000001");
        assertEquals("casetype-a_cases-000002", result);
    }

    private void mockAliasResponse() throws IOException {
        lenient().when(config.getCasesIndexNameFormat()).thenReturn("%s");

        IndexAliases indexAliases = new IndexAliases.Builder()
            .aliases(Map.of(baseIndexName, new AliasDefinition.Builder().build()))
            .build();
        GetAliasResponse aliasResponse = new GetAliasResponse.Builder()
            .aliases(Map.of(oldIndexName, indexAliases))
            .build();
        lenient().when(ccdElasticClient.getAlias(anyString())).thenReturn(aliasResponse);

        lenient().when(caseMappingGenerator.generateMapping(any(CaseTypeEntity.class))).thenReturn("caseMapping");
    }

    private void mockSuccessfulReindex() throws IOException {
        mockAliasResponse();
        doAnswer(invocation -> {
            ReindexListener listener = invocation.getArgument(2);
            listener.onSuccess("BulkByScrollResponse[took=1ms,...]");
            return null;
        }).when(ccdElasticClient).reindexData(eq(oldIndexName), eq(newIndexName), any(ReindexListener.class));
        IndexAliases indexAliases = new IndexAliases.Builder()
            .aliases(Map.of(
                baseIndexName, new AliasDefinition.Builder().build())).build();
        Map<String, IndexAliases> aliasMap = Map.of(oldIndexName, indexAliases);
        GetAliasResponse aliasResponse = new GetAliasResponse.Builder()
            .aliases(aliasMap)
            .build();
        when(ccdElasticClient.getAlias(anyString())).thenReturn(aliasResponse);
        when(caseMappingGenerator.generateMapping(any(CaseTypeEntity.class))).thenReturn("caseMapping");
    }

    private void mockFailedReindex() throws IOException {
        mockAliasResponse();
        doAnswer(invocation -> {
            ReindexListener listener = invocation.getArgument(2);
            listener.onFailure(new RuntimeException("reindexing failed"));
            return null;
        }).when(ccdElasticClient).reindexData(eq(oldIndexName), eq(newIndexName), any(ReindexListener.class));
    }

    private DefinitionImportedEvent newEvent(Boolean reindex, Boolean deleteOldIndex, CaseTypeEntity... caseTypes) {
        return new DefinitionImportedEvent(newArrayList(caseTypes), reindex, deleteOldIndex, TEST_USER_EMAIL);
    }
}
