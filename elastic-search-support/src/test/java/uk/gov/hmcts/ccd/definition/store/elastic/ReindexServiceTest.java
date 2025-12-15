package uk.gov.hmcts.ccd.definition.store.elastic;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;
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
class ReindexServiceTest {

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
    void setUp() {
        lenient().when(clientObjectFactory.getObject()).thenReturn(ccdElasticClient);
    }

    @Test
    void initialiseElasticSearchWhenReindexAndDeleteOldIndexAreTrue() throws IOException {
        mockAliasResponse();

        doAnswer(invocation -> {
            ReindexListener listener = invocation.getArgument(2);
            listener.onSuccess();
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
            ReindexListener listener = invocation.getArgument(2);
            listener.onSuccess();
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
            ReindexListener listener = invocation.getArgument(2);
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
    void shouldLogBeginReindexingMessageEvenWhenReindexFails() throws IOException {
        Logger logger = (Logger) LoggerFactory.getLogger(ReindexService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        mockAliasResponse();

        when(ccdElasticClient.countDocuments(caseTypeName)).thenReturn(50L);

        doAnswer(invocation -> {
            ReindexListener listener = invocation.getArgument(2);
            listener.onFailure(new RuntimeException("reindexing failed"));
            return null;
        }).when(ccdElasticClient).reindexData(eq(caseTypeName), eq(incrementedCaseTypeName), any());

        DefinitionImportedEvent event = new DefinitionImportedEvent(newArrayList(caseA), true, true);
        assertThrows(RuntimeException.class,
            () -> reindexService.asyncReindex(event, baseIndexName, caseA));

        // assert begin reindexing log message is present
        String combinedLogs = appender.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .reduce("", (a, b) -> a + "\n" + b);
        assertTrue(
            combinedLogs.contains(
                "Begin reindexing. Source index '" + caseTypeName + "' contains 50 cases/documents"
            )
        );
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
