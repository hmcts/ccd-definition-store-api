package uk.gov.hmcts.ccd.definition.store.elastic;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
public class ReindexServiceTest {

    @InjectMocks
    private ReindexService reindexService;

    @Mock
    private HighLevelCCDElasticClient ccdElasticClient;

    @Mock
    private CaseMappingGenerator caseMappingGenerator;

    @Mock
    private ObjectFactory<HighLevelCCDElasticClient> clientObjectFactory;

    private final String baseIndexName = "casetypea";
    private final String caseTypeName = "casetypea_cases-000001";
    private final String incrementedCaseTypeName = "casetypea_cases-000002";

    private final CaseTypeEntity caseA = new CaseTypeBuilder()
        .withJurisdiction("jurA")
        .withReference("caseTypeA")
        .build();

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(clientObjectFactory.getObject()).thenReturn(ccdElasticClient);
    }

    @Test
    void initialiseElasticSearchWhenReindexAndDeleteOldIndexAreTrue() throws IOException {
        mockAliasResponse();

        doAnswer(invocation -> {
            ActionListener<BulkByScrollResponse> listener = invocation.getArgument(2);
            BulkByScrollResponse mockResponse = mock(BulkByScrollResponse.class);
            listener.onResponse(mockResponse);
            return null;
        }).when(ccdElasticClient).reindexData(
            eq(caseTypeName),
            eq(incrementedCaseTypeName),
            any()
        );

        DefinitionImportedEvent event = new DefinitionImportedEvent(newArrayList(caseA), true, true);
        reindexService.asyncReindex(event, baseIndexName, caseA);

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

        doAnswer(invocation -> {
            ActionListener<BulkByScrollResponse> listener = invocation.getArgument(2);
            BulkByScrollResponse mockResponse = mock(BulkByScrollResponse.class);
            listener.onResponse(mockResponse);
            return null;
        }).when(ccdElasticClient).reindexData(
            eq(caseTypeName),
            eq(incrementedCaseTypeName),
            any()
        );

        DefinitionImportedEvent event = new DefinitionImportedEvent(newArrayList(caseA), true, false);
        reindexService.asyncReindex(event, baseIndexName, caseA);

        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, true);
        verify(caseMappingGenerator).generateMapping(any(CaseTypeEntity.class));
        verify(ccdElasticClient).createIndexAndMapping(incrementedCaseTypeName, "caseMapping");
        verify(ccdElasticClient).reindexData(eq(caseTypeName), eq(incrementedCaseTypeName), any());
        verify(ccdElasticClient).setIndexReadOnly(baseIndexName, false);
        verify(ccdElasticClient).updateAlias(baseIndexName, caseTypeName, incrementedCaseTypeName);
        verify(ccdElasticClient, never()).removeIndex(caseTypeName);
    }

    @Test
    void deletesNewIndexWhenReindexingFails() throws IOException {
        mockAliasResponse();

        doAnswer(invocation -> {
            ActionListener<BulkByScrollResponse> listener = invocation.getArgument(2);
            listener.onFailure(new RuntimeException("reindexing failed"));
            return null;
        }).when(ccdElasticClient).reindexData(eq(caseTypeName), eq(incrementedCaseTypeName), any());

        DefinitionImportedEvent event = new DefinitionImportedEvent(newArrayList(caseA), true, true);
        assertThrows(RuntimeException.class,
            () -> reindexService.asyncReindex(event, baseIndexName, caseA));

        verify(ccdElasticClient).removeIndex(incrementedCaseTypeName);
        verify(ccdElasticClient).setIndexReadOnly(caseTypeName, false);
        //using a single mock, so close() is called twice (in event listener and reindexing failure handler)
        verify(ccdElasticClient, atLeast(2)).close();
    }

    @Test
    void shouldIncrementIndexNumber() {
        String result = reindexService.incrementIndexNumber(caseTypeName);
        assertEquals(incrementedCaseTypeName, result);
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

    @Test
    void throwExceptionWhenIndexFormatIsInvalid() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            reindexService.incrementIndexNumber("invalidindex"));

        assertTrue(ex.getMessage().contains("invalid index name format"));
    }

    private void mockAliasResponse() throws IOException {
        GetAliasesResponse aliasResponse = mock(GetAliasesResponse.class);
        Map<String, Set<AliasMetadata>> aliasMap = new HashMap<>();
        aliasMap.put(caseTypeName, Collections.singleton(AliasMetadata.builder(baseIndexName).build()));
        when(aliasResponse.getAliases()).thenReturn(aliasMap);

        when(ccdElasticClient.getAlias(anyString())).thenReturn(aliasResponse);
        when(caseMappingGenerator.generateMapping(any(CaseTypeEntity.class))).thenReturn("caseMapping");
    }

}
