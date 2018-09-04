package uk.gov.hmcts.ccd.definition.store.elastic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.client.CCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import java.io.IOException;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticDefinitionImportListenerTest {

    @InjectMocks
    private TestDefinitionImportListener listener;

    @Mock
    private CCDElasticClient ccdElasticClient;

    @Mock
    private CcdElasticSearchProperties config;

    @Mock
    private CaseMappingGenerator caseMappingGenerator;

    private CaseTypeEntity caseA = new CaseTypeBuilder().withJurisdiction("jurA").withReference("caseTypeA").build();
    private CaseTypeEntity caseB = new CaseTypeBuilder().withJurisdiction("jurB").withReference("caseTypeB").build();

    @Test
    public void createsIndexIfNotExists() throws IOException {
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.indexExists(anyString())).thenReturn(false);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(ccdElasticClient).createIndex("casetypea");
        verify(ccdElasticClient).createIndex("casetypeb");
    }

    @Test
    public void skipIndexCreationIfNotExists() throws IOException {
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.indexExists(anyString())).thenReturn(true);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(ccdElasticClient, never()).createIndex(anyString());
    }

    @Test
    public void createsMapping() throws IOException {
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.indexExists(anyString())).thenReturn(false);
        when(caseMappingGenerator.generateMapping(any(CaseTypeEntity.class))).thenReturn("caseMapping");

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(caseMappingGenerator).generateMapping(caseA);
        verify(caseMappingGenerator).generateMapping(caseB);
        verify(ccdElasticClient).upsertMapping("casetypea", "caseMapping");
        verify(ccdElasticClient).upsertMapping("casetypeb", "caseMapping");
    }

    @Test
    public void throwsRuntimeExceptionOnErrors() {
        assertThrows(RuntimeException.class, () -> {
            when(config.getCasesIndexNameFormat()).thenThrow(new Exception("test"));
            listener.onDefinitionImported(newEvent(caseA, caseB));
        });
    }

    private DefinitionImportedEvent newEvent(CaseTypeEntity... caseTypes) {
        return new DefinitionImportedEvent(newArrayList(caseTypes));
    }

    private static class TestDefinitionImportListener extends ElasticDefinitionImportListener {

        @Override
        public void onDefinitionImported(DefinitionImportedEvent event) {
            super.initialiseElasticSearch(event.getCaseTypes());
        }
    }
}

