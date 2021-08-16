package uk.gov.hmcts.ccd.definition.store.elastic;

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

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ElasticGlobalSearchListenerTest {

    @InjectMocks
    private TestDefinitionImportListener listener;

    @Mock
    private HighLevelCCDElasticClient ccdElasticClient;

    @Mock
    private ObjectFactory<HighLevelCCDElasticClient> clientObjectFactory;

    @Mock
    private ElasticsearchErrorHandler elasticsearchErrorHandler;

    private CaseTypeEntity caseA = new CaseTypeBuilder().withJurisdiction("jurA").withReference("caseTypeA").build();
    private CaseTypeEntity caseB = new CaseTypeBuilder().withJurisdiction("jurB").withReference("caseTypeB").build();

    @BeforeEach
    public void setUp() {
        when(clientObjectFactory.getObject()).thenReturn(ccdElasticClient);
    }

    @Test
    public void createsIndexIfNotExistsForGlobalSearch() throws IOException {
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);

        listener.initialiseElasticSearchForGlobalSearch();

        verify(ccdElasticClient).createIndex("globalsearch-000001", "globalsearch");
    }

    @Test
    public void skipIndexCreationIfNotExistsForGlobalSearch() throws IOException {
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(true);

        listener.initialiseElasticSearchForGlobalSearch();

        verify(ccdElasticClient, never()).createIndex(anyString(), anyString());
    }


    private DefinitionImportedEvent newEvent(CaseTypeEntity... caseTypes) {
        return new DefinitionImportedEvent(newArrayList(caseTypes));
    }

    private static class TestDefinitionImportListener extends ElasticGlobalSearchListener {
        public TestDefinitionImportListener(ObjectFactory<uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient> clientFactory, ElasticsearchErrorHandler elasticsearchErrorHandler) {
            super(clientFactory, elasticsearchErrorHandler);
        }
    }
}

