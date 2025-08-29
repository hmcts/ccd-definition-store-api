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
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticDefinitionImportListenerTest {

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
    private ReindexEntityService reindexEntityService;

    private final CaseTypeEntity caseA = new CaseTypeBuilder().withJurisdiction("jurA")
        .withReference("caseTypeA").build();
    private final CaseTypeEntity caseB = new CaseTypeBuilder().withJurisdiction("jurB")
        .withReference("caseTypeB").build();
    private final String baseIndexName = "casetypea";
    private final String caseTypeName = "casetypea_cases-000001";
    private final String incrementedCaseTypeName = "casetypea_cases-000002";

    @BeforeEach
    void setUp() throws IOException {
        //mock alias response
        lenient().when(clientObjectFactory.getObject()).thenReturn(ccdElasticClient);
        lenient().when(config.getCasesIndexNameFormat()).thenReturn("%s");

        GetAliasesResponse aliasResponse = mock(GetAliasesResponse.class);
        Map<String, Set<AliasMetadata>> aliasMap = new HashMap<>();
        aliasMap.put(caseTypeName,
            Collections.singleton(AliasMetadata.builder(baseIndexName).build()));
        lenient().when(aliasResponse.getAliases()).thenReturn(aliasMap);
        lenient().when(ccdElasticClient.getAlias(anyString())).thenReturn(aliasResponse);

        lenient().when(caseMappingGenerator.generateMapping(any(CaseTypeEntity.class))).thenReturn("caseMapping");

        //mock reindex entity
        ReindexEntity reindexEntity = new ReindexEntity();
        reindexEntity.setIndexName(incrementedCaseTypeName);
        lenient().when(reindexEntityService.persistInitialReindexMetadata(anyBoolean(), anyBoolean(),
            any(), any())).thenReturn(reindexEntity);
    }

    @Test
    void createsAndClosesANewElasticClientOnEachImportToSaveResources() throws IOException {
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(clientObjectFactory).getObject();
        verify(ccdElasticClient).close();
    }

    @Test
    void closesClientEvenInCaseOfErrors() {
        when(config.getCasesIndexNameFormat()).thenThrow(new RuntimeException("test"));

        assertThrows(RuntimeException.class, () -> {
            listener.onDefinitionImported(newEvent(caseA, caseB));
            verify(ccdElasticClient).close();
        });
    }

    @Test
    void createsIndexIfNotExists() throws IOException {
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(ccdElasticClient).createIndex("casetypea-000001", "casetypea");
        verify(ccdElasticClient).createIndex("casetypeb-000001", "casetypeb");
    }

    @Test
    void skipIndexCreationIfNotExists() throws IOException {
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(true);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(ccdElasticClient, never()).createIndex(anyString(), anyString());
    }

    @Test
    void createsMapping() throws IOException {
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);
        when(caseMappingGenerator.generateMapping(any(CaseTypeEntity.class))).thenReturn("caseMapping");

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(caseMappingGenerator).generateMapping(caseA);
        verify(caseMappingGenerator).generateMapping(caseB);
        verify(ccdElasticClient).upsertMapping("casetypea", "caseMapping");
        verify(ccdElasticClient).upsertMapping("casetypeb", "caseMapping");
    }

    @Test
    public void shouldWrapElasticsearchStatusExceptionInInitialisationException() throws IOException {
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
        mockSuccessfulReindex();

        listener.onDefinitionImported(newEvent(true, true, caseA));

        verify(reindexEntityService).persistInitialReindexMetadata(eq(true), eq(true), eq(caseA),
            eq(incrementedCaseTypeName));
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(incrementedCaseTypeName, "caseMapping");
        verify(ccdElasticClient).reindexData(eq(caseTypeName), eq(incrementedCaseTypeName), any());
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, caseTypeName, incrementedCaseTypeName);
        verify(reindexEntityService).persistSuccess(incrementedCaseTypeName, anyString());

        verify(ccdElasticClient).removeIndex(caseTypeName);
        ArgumentCaptor<String> oldIndexCaptor = ArgumentCaptor.forClass(String.class);
        verify(ccdElasticClient).removeIndex(oldIndexCaptor.capture());
        assertEquals(caseTypeName, oldIndexCaptor.getValue());
    }

    @Test
    void initialiseElasticSearchWhenReindexTrueAndDeleteOldIndexFalse() throws IOException {
        mockSuccessfulReindex();

        listener.onDefinitionImported(newEvent(true, false, caseA));

        verify(reindexEntityService).persistInitialReindexMetadata(eq(true), eq(false), eq(caseA),
            eq(incrementedCaseTypeName));
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(incrementedCaseTypeName, "caseMapping");
        verify(ccdElasticClient).reindexData(eq(caseTypeName), eq(incrementedCaseTypeName), any());
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, caseTypeName, incrementedCaseTypeName);
        verify(reindexEntityService).persistSuccess(eq(incrementedCaseTypeName), anyString());
        verify(ccdElasticClient, never()).removeIndex(caseTypeName);
    }

    @Test
    void shouldNotInitialiseElasticSearchWhenReindexFalseAndDeleteOldIndexTrue() throws IOException {
        //expected behaviour should be same as default (reindex false and old index false)
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(caseMappingGenerator.generateMapping(caseA)).thenReturn("caseMapping");

        listener.onDefinitionImported(newEvent(false, true, caseA));

        verify(reindexEntityService, never()).persistInitialReindexMetadata(any(), any(), any(), any());
        verify(ccdElasticClient, never()).reindexData(anyString(), anyString(), any());
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).upsertMapping(baseIndexName, "caseMapping");

        verify(ccdElasticClient, never()).removeIndex(anyString());
    }

    @Test
    void deletesNewIndexOnFailedReindex() throws IOException {
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
    void triggerPersistFailureOnFailedReindex() {
        mockFailedReindex();

        DefinitionImportedEvent event = new DefinitionImportedEvent(
            Collections.singletonList(caseA), true, true);

        assertThrows(ElasticSearchInitialisationException.class, () ->
            listener.onDefinitionImported(event)
        );

        verify(reindexEntityService).persistFailure(eq(incrementedCaseTypeName), any(Exception.class));
    }

    @Test
    void triggersPersistFailureWhenReindexFailsBeforeHandleReindexing() {
        when(caseMappingGenerator.generateMapping(any())).thenThrow(
            new RuntimeException("mapping failure before reindex"));

        assertThrows(ElasticSearchInitialisationException.class, () ->
            listener.onDefinitionImported(newEvent(true, true, caseA))
        );

        verify(reindexEntityService).persistFailure(eq(incrementedCaseTypeName), any());
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

        when(reindexEntityService.persistInitialReindexMetadata(
            eq(true), eq(true), eq(caseA), eq(incrementedCaseTypeName))
        ).thenReturn(new ReindexEntity());
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
                                            ReindexEntityService reindexEntityService) {
            super(config, mappingGenerator, clientFactory, elasticsearchErrorHandler, reindexEntityService);
        }

        @Override
        public void onDefinitionImported(DefinitionImportedEvent event) {
            super.initialiseElasticSearch(event);
        }
    }
}

