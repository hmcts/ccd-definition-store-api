package uk.gov.hmcts.ccd.definition.store.elastic;

import java.io.IOException;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.elastic.client.CCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

@ExtendWith(MockitoExtension.class)
public class AsynchronousElasticDefinitionImportListenerTest {

    @InjectMocks
    private AsynchronousElasticDefinitionImportListener listener;

    @Mock
    private CCDElasticClient ccdElasticClient;

    @Mock
    private CcdElasticSearchProperties config;

    @Mock
    private CaseMappingGenerator caseMappingGenerator;

    private CaseTypeEntity caseA = new CaseTypeBuilder().withJurisdiction("jurA").withReference("caseTypeA").build();
    private CaseTypeEntity caseB = new CaseTypeBuilder().withJurisdiction("jurB").withReference("caseTypeB").build();

    //the listener is tested in ElasticDefinitionImportListenerTest, this is just a simple test to please Jacoco
    @Test
    public void createsIndexIfNotExists() throws IOException {
        when(config.getCasesIndexNameFormat()).thenReturn("%s");
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);

        listener.onDefinitionImported(newEvent(caseA, caseB));

        verify(ccdElasticClient).createIndex("casetypea-000001", "casetypea");
    }

    private DefinitionImportedEvent newEvent(CaseTypeEntity... caseTypes) {
        return new DefinitionImportedEvent(newArrayList(caseTypes));
    }
}

