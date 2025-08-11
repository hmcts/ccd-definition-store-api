package uk.gov.hmcts.ccd.definition.store.elastic;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.handler.ElasticsearchErrorHandler;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doAnswer;

@ExtendWith(MockitoExtension.class)
public class ElasticDefinitionImportListenerTest {

    @InjectMocks
    private TestDefinitionImportListener listener;

    @Mock
    private HighLevelCCDElasticClient ccdElasticClient;

    @Mock
    private ObjectFactory<HighLevelCCDElasticClient> clientObjectFactory;

    @Mock
    private CcdElasticSearchProperties config;

    @Mock
    private CaseMappingGenerator caseMappingGenerator;

    @Mock
    private ElasticsearchErrorHandler elasticsearchErrorHandler;

    @Mock
    private ReindexRepository reindexRepository;

    private final CaseTypeEntity caseA = new CaseTypeBuilder().withJurisdiction("jurA")
        .withReference("caseTypeA").build();
    private final CaseTypeEntity caseB = new CaseTypeBuilder().withJurisdiction("jurB")
        .withReference("caseTypeB").build();
    private final String baseIndexName = "casetypea";
    private final String caseTypeName = "casetypea_cases-000001";
    private final String incrementedCaseTypeName = "casetypea_cases-000002";
    private final ReindexEntity metadata = new ReindexEntity();

    @BeforeEach
    public void setUp() {
        lenient().when(clientObjectFactory.getObject()).thenReturn(ccdElasticClient);
    }

    @Test
    public void createsAndClosesANewElasticClientOnEachImportToSaveResources() throws IOException {
        mockAliasResponse();
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);
        when(reindexRepository.save(any(ReindexEntity.class))).thenReturn(metadata);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(clientObjectFactory).getObject();
        verify(ccdElasticClient).close();
    }

    @Test
    public void closesClientEvenInCaseOfErrors() {
        when(config.getCasesIndexNameFormat()).thenThrow(new RuntimeException("test"));

        assertThrows(RuntimeException.class, () -> {
            listener.onDefinitionImported(newEvent(caseA, caseB));
            verify(ccdElasticClient).close();
        });
    }

    @Test
    public void createsIndexIfNotExists() throws IOException {
        mockAliasResponse();
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);
        when(reindexRepository.save(any(ReindexEntity.class))).thenReturn(metadata);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(ccdElasticClient).createIndex("casetypea-000001", "casetypea");
        verify(ccdElasticClient).createIndex("casetypeb-000001", "casetypeb");
    }

    @Test
    public void skipIndexCreationIfNotExists() throws IOException {
        mockAliasResponse();
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(true);
        when(reindexRepository.save(any(ReindexEntity.class))).thenReturn(metadata);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(ccdElasticClient, never()).createIndex(anyString(), anyString());
    }

    @Test
    public void createsMapping() throws IOException {
        mockAliasResponse();
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);
        when(caseMappingGenerator.generateMapping(any(CaseTypeEntity.class))).thenReturn("caseMapping");
        when(reindexRepository.save(any(ReindexEntity.class))).thenReturn(metadata);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(caseMappingGenerator).generateMapping(caseA);
        verify(caseMappingGenerator).generateMapping(caseB);
        verify(ccdElasticClient).upsertMapping("casetypea", "caseMapping");
        verify(ccdElasticClient).upsertMapping("casetypeb", "caseMapping");
    }

    @Test
    public void shouldWrapElasticsearchStatusExceptionInInitialisationException() throws IOException {
        mockAliasResponse();
        when(reindexRepository.save(any(ReindexEntity.class))).thenReturn(metadata);

        // mock upsertMapping to throw ElasticsearchStatusException
        when(ccdElasticClient.upsertMapping(anyString(), anyString()))
            .thenThrow(new ElasticsearchStatusException("Simulated ES error", RestStatus.BAD_REQUEST));

        ElasticSearchInitialisationException wrapped =
            new ElasticSearchInitialisationException(new RuntimeException("wrapped"));
        when(elasticsearchErrorHandler.createException(any(), eq(caseA))).thenReturn(wrapped);

        ElasticSearchInitialisationException thrown = assertThrows(
            ElasticSearchInitialisationException.class,
            () -> listener.onDefinitionImported(newEvent(caseA))
        );

        assertEquals(wrapped, thrown);
        verify(elasticsearchErrorHandler).createException(any(ElasticsearchStatusException.class), eq(caseA));
    }


    @Test
    public void throwsElasticSearchInitialisationExceptionOnErrors() {
        assertThrows(ElasticSearchInitialisationException.class, () -> {
            when(config.getCasesIndexNameFormat()).thenThrow(new ArrayIndexOutOfBoundsException("test"));
            listener.onDefinitionImported(newEvent(caseA, caseB));
        });
    }

    @Test
    void initialiseElasticSearchWhenReindexAndDeleteOldIndexAreTrue() throws IOException {
        mockAliasResponse();
        when(reindexRepository.save(any(ReindexEntity.class))).thenReturn(metadata);
        mockSuccessfulReindex();

        listener.onDefinitionImported(newEvent(true, true, caseA));

        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(incrementedCaseTypeName, "caseMapping");
        verify(ccdElasticClient).reindexData(eq(caseTypeName), eq(incrementedCaseTypeName), any());
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, caseTypeName, incrementedCaseTypeName);

        verify(ccdElasticClient).removeIndex(caseTypeName);
        ArgumentCaptor<String> oldIndexCaptor = ArgumentCaptor.forClass(String.class);
        verify(ccdElasticClient).removeIndex(oldIndexCaptor.capture());
        assertEquals(caseTypeName, oldIndexCaptor.getValue());
    }

    @Test
    void initialiseElasticSearchWhenReindexTrueAndDeleteOldIndexFalse() throws IOException {
        mockAliasResponse();
        when(reindexRepository.save(any(ReindexEntity.class))).thenReturn(metadata);
        mockSuccessfulReindex();

        listener.onDefinitionImported(newEvent(true, false, caseA));

        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(incrementedCaseTypeName, "caseMapping");
        verify(ccdElasticClient).reindexData(eq(caseTypeName), eq(incrementedCaseTypeName), any());
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, caseTypeName, incrementedCaseTypeName);
        verify(ccdElasticClient, never()).removeIndex(caseTypeName);
    }

    @Test
    void shouldNotInitialiseElasticSearchWhenReindexFalseAndDeleteOldIndexTrue() throws IOException {
        //expected behaviour should be same as default (reindex false and old index false)
        mockAliasResponse();
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(caseMappingGenerator.generateMapping(caseA)).thenReturn("caseMapping");
        when(reindexRepository.save(any(ReindexEntity.class))).thenReturn(metadata);

        listener.onDefinitionImported(newEvent(false, true, caseA));

        verify(ccdElasticClient, never()).reindexData(anyString(), anyString(), any());
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).upsertMapping(baseIndexName, "caseMapping");

        verify(ccdElasticClient, never()).removeIndex(anyString());
    }

    @Test
    void deletesNewIndexWhenReindexingFails() throws IOException {
        mockAliasResponse();
        when(reindexRepository.save(any(ReindexEntity.class))).thenReturn(metadata);
        mockFailedReindex();

        DefinitionImportedEvent event = newEvent(true, true, caseA);

        assertThrows(ElasticSearchInitialisationException.class, () ->
            listener.onDefinitionImported(event));

        verify(ccdElasticClient).removeIndex(incrementedCaseTypeName);
        verify(ccdElasticClient).setIndexReadOnly(caseTypeName, false);
        //using a single mock, so close() is called twice (in event listener and reindexing failure handler)
        verify(ccdElasticClient, atLeast(2)).close();
    }

    @Test
    void savesMetadataOnSuccessfulReindex() throws IOException {
        mockAliasResponse();

        ArgumentCaptor<ReindexEntity> captor = ArgumentCaptor.forClass(ReindexEntity.class);
        when(reindexRepository.save(any(ReindexEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        mockSuccessfulReindex();

        listener.onDefinitionImported(newEvent(true, true, caseA));

        //verify that the reindex metadata was saved twice, once before reindexing and once after
        verify(reindexRepository, atLeast(2)).save(captor.capture());
        List<ReindexEntity> metadata = captor.getAllValues();
        ReindexEntity finalSave = metadata.get(metadata.size() - 1);

        assertEquals("casetypea_cases-000002", finalSave.getIndexName());
        assertEquals("jurA", finalSave.getJurisdiction());
        assertEquals("caseTypeA", finalSave.getCaseType());
        assertNotNull(finalSave.getStartTime());
        assertNotNull(finalSave.getEndTime());
        assertEquals("SUCCESS", finalSave.getStatus());
        assertNull(finalSave.getMessage());
    }

    @Test
    void savesMetadataOnFailedReindex() throws IOException {
        mockAliasResponse();

        ArgumentCaptor<ReindexEntity> captor = ArgumentCaptor.forClass(ReindexEntity.class);
        when(reindexRepository.save(any(ReindexEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        mockFailedReindex();

        DefinitionImportedEvent event = newEvent(true, true, caseA);

        assertThrows(ElasticSearchInitialisationException.class, () ->
            listener.onDefinitionImported(event)
        );

        //verify that the reindex metadata was saved twice, once before reindexing and once after
        verify(reindexRepository, atLeast(2)).save(captor.capture());
        List<ReindexEntity> metadata = captor.getAllValues();
        ReindexEntity failedSave = metadata.get(metadata.size() - 1);

        //will still include the incremented index name
        assertEquals("caseTypeA", failedSave.getCaseType());
        assertEquals("jurA", failedSave.getJurisdiction());
        assertEquals("casetypea_cases-000002", failedSave.getIndexName());
        assertNotNull(failedSave.getStartTime());
        assertNotNull(failedSave.getEndTime());
        assertEquals("FAILED", failedSave.getStatus());
        assertTrue(failedSave.getMessage().contains("reindexing failed"));
    }

    @Test
    void savesMetadataWhenReindexFalse() throws IOException {
        mockAliasResponse();
        when(caseMappingGenerator.generateMapping(any())).thenReturn("caseMapping");

        ArgumentCaptor<ReindexEntity> captor = ArgumentCaptor.forClass(ReindexEntity.class);
        when(reindexRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        listener.onDefinitionImported(newEvent(false, false, caseA));

        verify(reindexRepository).save(captor.capture());
        ReindexEntity metadata = captor.getValue();

        //will be the original index name without increment
        assertTrue(metadata.getReindex() == false);
        assertTrue(metadata.getDeleteOldIndex() == false);
        assertEquals("jurA", metadata.getJurisdiction());
        assertEquals("casetypea_cases-000001", metadata.getIndexName());
        assertNotNull(metadata.getStartTime());
        //end time is null as reindexing did not occur
        assertNull(metadata.getEndTime());
        assertEquals("STARTED", metadata.getStatus());
        //message is null as no error occurred
        assertNull(metadata.getMessage());
    }

    @Test
    void throwsExceptionIfMetadataSavedFailed() throws IOException {
        mockAliasResponse();
        when(reindexRepository.save(any(ReindexEntity.class))).thenReturn(null);

        ElasticSearchInitialisationException exception = assertThrows(
            ElasticSearchInitialisationException.class,
            () -> listener.onDefinitionImported(newEvent(true, true, caseA))
        );

        assertTrue(exception.getCause() instanceof ElasticSearchInitialisationException);
        assertTrue(exception.getMessage().contains("Failed to persist reindex metadata"));
        verify(ccdElasticClient, never()).reindexData(any(), any(), any());
    }

    @Test
    void shouldIncrementIndexNumber() {
        String result = listener.incrementIndexNumber(caseTypeName);
        assertEquals(incrementedCaseTypeName, result);
    }

    @Test
    void incrementToDoubleDigitIndexNumber() {
        String result = listener.incrementIndexNumber("casetypea_cases-000009");
        assertEquals("casetypea_cases-000010", result);
    }

    @Test
    void incrementIndexNumberWithDash() {
        String result = listener.incrementIndexNumber("casetype-a_cases-000001");
        assertEquals("casetype-a_cases-000002", result);
    }

    @Test
    void throwExceptionWhenIndexFormatIsInvalid() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            listener.incrementIndexNumber("invalidindex"));

        assertTrue(ex.getMessage().contains("invalid index name format"));
    }

    @Test
    void testConstructorWithAllArguments() {
        DefinitionImportedEvent event = newEvent(true, true, caseA);

        assertTrue(event.isReindex());
        assertTrue(event.isDeleteOldIndex());
    }

    @Test
    void testConstructorWithDefaults() {
        DefinitionImportedEvent event = newEvent(caseA);

        //default parameters are reindex = false and deleteOldIndex = false
        assertFalse(event.isReindex());
        assertFalse(event.isDeleteOldIndex());
    }

    private void mockAliasResponse() throws IOException {
        lenient().when(config.getCasesIndexNameFormat()).thenReturn("%s");

        GetAliasesResponse aliasResponse = mock(GetAliasesResponse.class);
        Map<String, Set<AliasMetadata>> aliasMap = new HashMap<>();
        aliasMap.put(caseTypeName,
            Collections.singleton(AliasMetadata.builder(baseIndexName).build()));
        lenient().when(aliasResponse.getAliases()).thenReturn(aliasMap);
        lenient().when(ccdElasticClient.getAlias(anyString())).thenReturn(aliasResponse);

        lenient().when(caseMappingGenerator.generateMapping(any(CaseTypeEntity.class))).thenReturn("caseMapping");
    }

    private void mockSuccessfulReindex() {
        doAnswer(invocation -> {
            ActionListener<BulkByScrollResponse> listener = invocation.getArgument(2);
            listener.onResponse(mock(BulkByScrollResponse.class));
            return null;
        }).when(ccdElasticClient).reindexData(eq(caseTypeName), eq(incrementedCaseTypeName), any());
    }

    private void mockFailedReindex() {
        doAnswer(invocation -> {
            ActionListener<BulkByScrollResponse> listener = invocation.getArgument(2);
            listener.onFailure(new RuntimeException("reindexing failed"));
            return null;
        }).when(ccdElasticClient).reindexData(eq(caseTypeName), eq(incrementedCaseTypeName), any());
    }

    private DefinitionImportedEvent newEvent(CaseTypeEntity... caseTypes) {
        return new DefinitionImportedEvent(newArrayList(caseTypes));
    }

    private DefinitionImportedEvent newEvent(Boolean reindex, Boolean deleteOldIndex, CaseTypeEntity... caseTypes) {
        return new DefinitionImportedEvent(newArrayList(caseTypes), reindex, deleteOldIndex);
    }

    private static class TestDefinitionImportListener extends ElasticDefinitionImportListener {

        public TestDefinitionImportListener(CcdElasticSearchProperties config, CaseMappingGenerator mappingGenerator,
                                            ObjectFactory<HighLevelCCDElasticClient> clientFactory,
                                            ElasticsearchErrorHandler elasticsearchErrorHandler,
                                            ReindexRepository reindexRepository) {
            super(config, mappingGenerator, clientFactory, elasticsearchErrorHandler, reindexRepository);
        }

        @Override
        public void onDefinitionImported(DefinitionImportedEvent event) {
            super.initialiseElasticSearch(event);
        }
    }
}

