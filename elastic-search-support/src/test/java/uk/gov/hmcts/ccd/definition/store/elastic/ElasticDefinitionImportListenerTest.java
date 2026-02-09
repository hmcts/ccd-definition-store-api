package uk.gov.hmcts.ccd.definition.store.elastic;

import co.elastic.clients.elasticsearch.indices.AliasDefinition;
import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import co.elastic.clients.elasticsearch.indices.get_alias.IndexAliases;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    private final CaseTypeEntity caseA = new CaseTypeBuilder().withJurisdiction("jurA")
        .withReference("caseTypeA").build();
    private final CaseTypeEntity caseB = new CaseTypeBuilder().withJurisdiction("jurB")
        .withReference("caseTypeB").build();
    private final String baseIndexName = "casetypea";
    private final String caseTypeName = "casetypea_cases-000001";

    @BeforeEach
    void setUp() {
        lenient().when(clientObjectFactory.getObject()).thenReturn(ccdElasticClient);
        ccdElasticClient.close();
    }

    @Test
    void createsAndClosesANewElasticClientOnEachImportToSaveResources() throws IOException {
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(clientObjectFactory).getObject();
        verify(ccdElasticClient, times(1)).close();
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
    void shouldWrapElasticsearchStatusExceptionInInitialisationException() throws IOException {
        // mock alias response
        lenient().when(config.getCasesIndexNameFormat()).thenReturn("%s");
        lenient().when(ccdElasticClient.aliasExists(anyString())).thenReturn(true);

        IndexAliases indexAliases = new IndexAliases.Builder()
            .aliases(Map.of(
                baseIndexName, new AliasDefinition.Builder().build())).build();
        Map<String, IndexAliases> aliasMap = Map.of(caseTypeName, indexAliases);
        GetAliasResponse aliasResponse = new GetAliasResponse.Builder()
            .aliases(aliasMap)
            .build();
        lenient().when(ccdElasticClient.getAlias(anyString())).thenReturn(aliasResponse);

        lenient().when(caseMappingGenerator.generateMapping(any(CaseTypeEntity.class))).thenReturn("caseMapping");

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
    void throwsElasticSearchInitialisationExceptionOnErrors() {
        assertThrows(ElasticSearchInitialisationException.class, () -> {
            when(config.getCasesIndexNameFormat()).thenThrow(new ArrayIndexOutOfBoundsException("test"));
            listener.onDefinitionImported(newEvent(caseA, caseB));
        });
    }

    @Test
    void shouldNotInitialiseElasticSearchWhenReindexFalseAndDeleteOldIndexTrue() throws IOException {
        //expected behaviour should be same as default (reindex false and old index false)
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(caseMappingGenerator.generateMapping(caseA)).thenReturn("caseMapping");

        listener.onDefinitionImported(newEvent(false, true, caseA));

        verify(ccdElasticClient, never()).reindexData(anyString(), anyString(), any());
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).upsertMapping(baseIndexName, "caseMapping");

        verify(ccdElasticClient, never()).removeIndex(anyString());
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
                                            ReindexService reindexService) {
            super(config, mappingGenerator, clientFactory, elasticsearchErrorHandler, reindexService);
        }

        @Override
        public void onDefinitionImported(DefinitionImportedEvent event) {
            super.initialiseElasticSearch(event);
        }
    }
}

