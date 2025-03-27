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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

    private CaseTypeEntity caseA = new CaseTypeBuilder().withJurisdiction("jurA").withReference("caseTypeA").build();
    private CaseTypeEntity caseB = new CaseTypeBuilder().withJurisdiction("jurB").withReference("caseTypeB").build();
    private final String baseIndexName = "casetypea";
    private final String caseTypeName = "casetypea-0001";
    private final String incrementedCaseTypeName = "casetypea-0002";

    @BeforeEach
    public void setUp() {
        when(clientObjectFactory.getObject()).thenReturn(ccdElasticClient);
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
    public void initialiseElasticSearchWhenReindexAndDeleteOldIndexAreTrue() throws IOException, ExecutionException,
        InterruptedException {
        mockAliasResponse();

        CompletableFuture<String> mockFuture = CompletableFuture.completedFuture("taskId");
        when(ccdElasticClient.reindexData(anyString(), anyString()))
            .thenReturn(mockFuture);

        listener.onDefinitionImported(newReindexDeleteOldIndexEvent(caseA));

        //check set readonly, created new index, alias updated to new index, returns task id
        verify(ccdElasticClient).setIndexReadOnly("casetypea", true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(incrementedCaseTypeName, "caseMapping");
        verify(ccdElasticClient).reindexData(caseTypeName, incrementedCaseTypeName);
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, "casetypea-0001", incrementedCaseTypeName);
        assertEquals("taskId", mockFuture.get());

        //check old task id deleted
        verify(ccdElasticClient).removeIndex(caseTypeName);
        ArgumentCaptor<String> oldIndexCaptor = ArgumentCaptor.forClass(String.class);
        verify(ccdElasticClient).removeIndex(oldIndexCaptor.capture());
        assertEquals(caseTypeName, oldIndexCaptor.getValue());
    }

    @Test
    public void initialiseElasticSearchWhenReindexTrueAndDeleteOldIndexFalse() throws IOException, ExecutionException,
        InterruptedException {
        mockAliasResponse();

        CompletableFuture<String> mockFuture = CompletableFuture.completedFuture("taskId");
        when(ccdElasticClient.reindexData(anyString(), anyString()))
            .thenReturn(mockFuture);

        listener.onDefinitionImported(newReindexEvent(caseA));

        //check set readonly, created new index, alias updated to new index, returns task id
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(incrementedCaseTypeName, "caseMapping");
        verify(ccdElasticClient).reindexData(caseTypeName, incrementedCaseTypeName);
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, caseTypeName, incrementedCaseTypeName);
        assertEquals("taskId", mockFuture.get());

        //check old task id not deleted
        verify(ccdElasticClient, never()).removeIndex(caseTypeName);
    }

    @Test
    public void initialiseElasticSearchWhenReindexAndDeleteOldIndexAreFalse() throws IOException {
        //should be same behaviour as default reindex false old index true
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(caseMappingGenerator.generateMapping(caseA)).thenReturn("caseMapping");

        listener.onDefinitionImported(newEventDeleteOldIndex(caseA));

        //don't call reindex, generate and upsert mapping
        verify(ccdElasticClient, never()).reindexData(anyString(), anyString());
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).upsertMapping("casetypea", "caseMapping");
        // check deleteOldIndex is not called
        verify(ccdElasticClient, never()).removeIndex(anyString());
    }

    @Test
    public void deletesNewIndexWhenReindexingFails() throws IOException {
        mockAliasResponse();

        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Elasticsearch reindexing failed"));

        when(ccdElasticClient.reindexData(caseTypeName, incrementedCaseTypeName)).thenReturn(failedFuture);

        assertThrows(ElasticSearchInitialisationException.class, () ->
            listener.onDefinitionImported(newReindexDeleteOldIndexEvent(caseA)));

        //delete new index, set old index writable, close connection
        verify(ccdElasticClient).removeIndex(incrementedCaseTypeName);
        verify(ccdElasticClient).setIndexReadOnly(caseTypeName, false);
        verify(ccdElasticClient).close();
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
        return new DefinitionImportedEvent(newArrayList(caseTypes), false, true);
    }

    private DefinitionImportedEvent newEventDeleteOldIndex(CaseTypeEntity... caseTypes) {
        return new DefinitionImportedEvent(newArrayList(caseTypes), false, false);
    }

    private DefinitionImportedEvent newReindexDeleteOldIndexEvent(CaseTypeEntity... caseTypes) {
        return new DefinitionImportedEvent(newArrayList(caseTypes), true, true);
    }

    private DefinitionImportedEvent newReindexEvent(CaseTypeEntity... caseTypes) {
        return new DefinitionImportedEvent(newArrayList(caseTypes), true, false);
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

