package uk.gov.hmcts.ccd.definition.store.elastic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.handler.ElasticsearchErrorHandler;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticGlobalSearchListenerTest {

    @InjectMocks
    private TestDefinitionImportListener listener;

    @Mock
    private HighLevelCCDElasticClient ccdElasticClient;

    @Mock
    private ObjectFactory<HighLevelCCDElasticClient> clientObjectFactory;

    @BeforeEach
    public void setUp() {
        when(clientObjectFactory.getObject()).thenReturn(ccdElasticClient);
    }

    @Test
    void createsIndexIfNotExistsForGlobalSearch() throws IOException {
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(false);

        listener.initialiseElasticSearchForGlobalSearch();

        verify(ccdElasticClient).createIndex("global_search-000001", "global_search");
    }

    @Test
    void skipIndexCreationIfNotExistsForGlobalSearch() throws IOException {
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(true);

        listener.initialiseElasticSearchForGlobalSearch();

        verify(ccdElasticClient, never()).createIndex(anyString(), anyString());
    }

    private static class TestDefinitionImportListener extends ElasticGlobalSearchListener {
        public TestDefinitionImportListener(
            ObjectFactory<HighLevelCCDElasticClient> clientFactory,
            ElasticsearchErrorHandler elasticsearchErrorHandler) {
            super(clientFactory, elasticsearchErrorHandler);
        }
    }
}

