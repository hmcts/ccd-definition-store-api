package uk.gov.hmcts.ccd.definition.store.elastic;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexService;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexServiceImpl;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReindexServiceTest {

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

        verify(reindexRepository).saveAndFlush(any(ReindexEntity.class));
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(newIndexName, "caseMapping");
        verify(ccdElasticClient).reindexData(eq(oldIndexName), eq(newIndexName), any());
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, oldIndexName, newIndexName);
        verify(reindexRepository).save(any(ReindexEntity.class));

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

        verify(reindexRepository).saveAndFlush(any(ReindexEntity.class));
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(newIndexName, "caseMapping");
        verify(ccdElasticClient).reindexData(eq(oldIndexName), eq(newIndexName), any());
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, oldIndexName, newIndexName);
        verify(reindexRepository).save(any(ReindexEntity.class));
        verify(ccdElasticClient, never()).removeIndex(oldIndexName);
    }

    @Test
    void deletesNewIndexOnFailedReindex() throws IOException {
        mockFailedReindex();

        DefinitionImportedEvent event = newEvent(true, true, caseA);

        assertThrows(RuntimeException.class,
            () -> reindexService.asyncReindex(event, baseIndexName, caseA));

        verify(ccdElasticClient).removeIndex(newIndexName);
        verify(ccdElasticClient).setIndexReadOnly(oldIndexName, false);
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
            true, true, caseType, newIndexName
        );

        verify(reindexRepository).saveAndFlush(captor.capture());

        ReindexEntity saved = captor.getValue();
        assertSame(saved, result);
        assertTrue(saved.getReindex());
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

        reindexService.updateEntity(newIndexName, anyString());

        verify(reindexRepository).save(existing);
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
        reindexService.updateEntity(newIndexName, ex);

        verify(reindexRepository).save(existing);
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
            Collections.singletonList(caseA), true, true
        );

        assertThrows(ElasticSearchInitialisationException.class,
            () -> reindexService.asyncReindex(event, baseIndexName, caseA));

        verify(reindexRepository).save(entity);
        assertEquals("FAILED", entity.getStatus());
        assertTrue(entity.getExceptionMessage().contains("ES error"));
    }

    @Test
    void triggerPersistFailureOnFailedReindex() throws IOException {
        mockFailedReindex();

        ReindexEntity entity = new ReindexEntity();
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(entity));

        DefinitionImportedEvent event = new DefinitionImportedEvent(
            Collections.singletonList(caseA), true, true);

        assertThrows(RuntimeException.class,
            () -> reindexService.asyncReindex(event, baseIndexName, caseA));

        ArgumentCaptor<ReindexEntity> captor = ArgumentCaptor.forClass(ReindexEntity.class);
        verify(reindexRepository).save(captor.capture());

        verify(reindexRepository).save(entity);
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
            Collections.singletonList(caseA), true, true
        );

        assertThrows(ElasticSearchInitialisationException.class,
            () -> reindexService.asyncReindex(event, baseIndexName, caseA));

        verify(reindexRepository).save(entity);
        assertEquals("FAILED", entity.getStatus());
        assertTrue(entity.getExceptionMessage().contains("mapping failure before reindex"));
    }

    @Test
    void shouldUnwrapCompletionException() {
        Throwable root = new IllegalArgumentException("Root cause");
        CompletionException completion = new CompletionException(root);

        ReindexEntity entity = new ReindexEntity();
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(entity));

        reindexService.updateEntity(newIndexName, completion);

        verify(reindexRepository).save(entity);
        assertTrue(entity.getExceptionMessage().contains("IllegalArgumentException"));
    }

    @Test
    void shouldSkipMarkSuccessIfEntityNotFound() {
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reindexService.updateEntity(newIndexName, anyString());
        });

        assertTrue(exception.getMessage().contains("No reindex entity metadata found for index name"));
        verify(reindexRepository, never()).save(any());
    }

    @Test
    void shouldSkipMarkFailureIfEntityNotFound() {
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.empty());

        reindexService.updateEntity(newIndexName, new RuntimeException("Fail"));

        verify(reindexRepository, never()).save(any());
    }

    @Test
    void shouldNotUnwrapRegularException() {
        RuntimeException regularException = new RuntimeException("Regular exception");

        ReindexEntity entity = new ReindexEntity();
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(entity));

        reindexService.updateEntity(newIndexName, regularException);

        verify(reindexRepository).save(entity);
        assertTrue(entity.getExceptionMessage().contains("RuntimeException"));
        assertTrue(entity.getExceptionMessage().contains("Regular exception"));
    }

    @Test
    void shouldIncrementIndexNumber() {
        String result = reindexService.incrementIndexNumber(oldIndexName);
        assertEquals(newIndexName, result);
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

        GetAliasesResponse aliasResponse = mock(GetAliasesResponse.class);
        Map<String, Set<AliasMetadata>> aliasMap = new HashMap<>();
        aliasMap.put(oldIndexName,
            Collections.singleton(AliasMetadata.builder(baseIndexName).build()));
        lenient().when(aliasResponse.getAliases()).thenReturn(aliasMap);
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
        return new DefinitionImportedEvent(newArrayList(caseTypes), reindex, deleteOldIndex);
    }
}
