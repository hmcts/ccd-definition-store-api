package uk.gov.hmcts.ccd.definition.store.elastic;

import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.cluster.metadata.AliasMetadata;
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
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private final CaseTypeEntity caseA = new CaseTypeBuilder().withJurisdiction("jurA")
        .withReference("caseTypeA").build();
    private final CaseTypeEntity caseB = new CaseTypeBuilder().withJurisdiction("jurB")
        .withReference("caseTypeB").build();
    private final String baseIndexName = "casetypea";
    private final String caseTypeName = "casetypea-000001";
    private final String incrementedCaseTypeName = "casetypea-000002";

    @BeforeEach
    public void setUp() {
        lenient().when(clientObjectFactory.getObject()).thenReturn(ccdElasticClient);
    }

    @Test
    public void createsAndClosesANewElasticClientOnEachImportToSaveResources() throws IOException {
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);

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
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(ccdElasticClient).createIndex("casetypea-000001", "casetypea");
        verify(ccdElasticClient).createIndex("casetypeb-000001", "casetypeb");
    }

    @Test
    public void skipIndexCreationIfNotExists() throws IOException {
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(true);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(ccdElasticClient, never()).createIndex(anyString(), anyString());
    }

    @Test
    public void createsMapping() throws IOException {
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
    public void throwsElasticSearchInitialisationExceptionOnErrors() {
        assertThrows(ElasticSearchInitialisationException.class, () -> {
            when(config.getCasesIndexNameFormat()).thenThrow(new ArrayIndexOutOfBoundsException("test"));
            listener.onDefinitionImported(newEvent(caseA, caseB));
        });
    }

    @Test
    void initialiseElasticSearchWhenReindexAndDeleteOldIndexAreTrue() throws IOException, ExecutionException,
        InterruptedException {
        mockAliasResponse();

        CompletableFuture<String> mockFuture = CompletableFuture.completedFuture("taskId");
        when(ccdElasticClient.reindexData(anyString(), anyString()))
            .thenReturn(mockFuture);

        listener.onDefinitionImported(newEvent(true, true, caseA));

        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(incrementedCaseTypeName, "caseMapping");
        verify(ccdElasticClient).reindexData(caseTypeName, incrementedCaseTypeName);
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, caseTypeName, incrementedCaseTypeName);
        assertEquals("taskId", mockFuture.get());

        verify(ccdElasticClient).removeIndex(caseTypeName);
        ArgumentCaptor<String> oldIndexCaptor = ArgumentCaptor.forClass(String.class);
        verify(ccdElasticClient).removeIndex(oldIndexCaptor.capture());
        assertEquals(caseTypeName, oldIndexCaptor.getValue());
    }

    @Test
    void initialiseElasticSearchWhenReindexTrueAndDeleteOldIndexFalse() throws IOException, ExecutionException,
        InterruptedException {
        mockAliasResponse();

        CompletableFuture<String> mockFuture = CompletableFuture.completedFuture("taskId");
        when(ccdElasticClient.reindexData(anyString(), anyString()))
            .thenReturn(mockFuture);

        listener.onDefinitionImported(newEvent(true, false, caseA));

        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(incrementedCaseTypeName, "caseMapping");
        verify(ccdElasticClient).reindexData(caseTypeName, incrementedCaseTypeName);
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, caseTypeName, incrementedCaseTypeName);
        assertEquals("taskId", mockFuture.get());

        verify(ccdElasticClient, never()).removeIndex(caseTypeName);
    }

    @Test
    void initialiseElasticSearchWhenReindexFalseAndDeleteOldIndexTrue() throws IOException {
        //expected behaviour should be same as default (reindex false and old index false)
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(caseMappingGenerator.generateMapping(caseA)).thenReturn("caseMapping");

        listener.onDefinitionImported(newEvent(false, true, caseA));

        verify(ccdElasticClient, never()).reindexData(anyString(), anyString());
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).upsertMapping(baseIndexName, "caseMapping");

        verify(ccdElasticClient, never()).removeIndex(anyString());
    }

    @Test
    void deletesNewIndexWhenReindexingFails() throws IOException {
        mockAliasResponse();

        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("reindexing failed"));

        when(ccdElasticClient.reindexData(caseTypeName, incrementedCaseTypeName)).thenReturn(failedFuture);

        DefinitionImportedEvent event = newEvent(true, true, caseA);

        assertThrows(ElasticSearchInitialisationException.class, () ->
            listener.onDefinitionImported(event));

        verify(ccdElasticClient).removeIndex(incrementedCaseTypeName);
        verify(ccdElasticClient).setIndexReadOnly(caseTypeName, false);
        verify(ccdElasticClient).close();
    }

    @Test
    void shouldIncrementIndexNumber() {
        String result = listener.incrementIndexNumber(caseTypeName);
        assertEquals(incrementedCaseTypeName, result);
    }

    @Test
    void incrementToDoubleDigitIndexNumber() {
        String result = listener.incrementIndexNumber("casetype-000009");
        assertEquals("casetype-000010", result);
    }

    @Test
    void throwExceptionWhenIndexFormatIsInvalid() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            listener.incrementIndexNumber("invalidindex"));

        assertTrue(ex.getMessage().contains("Invalid index name format"));
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
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(true);

        GetAliasesResponse aliasResponse = mock(GetAliasesResponse.class);
        Map<String, Set<AliasMetadata>> aliasMap = new HashMap<>();
        aliasMap.put(caseTypeName,
            Collections.singleton(AliasMetadata.builder(baseIndexName).build()));
        when(aliasResponse.getAliases()).thenReturn(aliasMap);
        when(ccdElasticClient.getAlias(anyString())).thenReturn(aliasResponse);

        when(caseMappingGenerator.generateMapping(any(CaseTypeEntity.class))).thenReturn("caseMapping");
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
                                            ElasticsearchErrorHandler elasticsearchErrorHandler) {
            super(config, mappingGenerator, clientFactory, elasticsearchErrorHandler);
        }

        @Override
        public void onDefinitionImported(DefinitionImportedEvent event) {
            super.initialiseElasticSearch(event);
        }
    }
}

