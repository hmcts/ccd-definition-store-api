package uk.gov.hmcts.ccd.definition.store.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.handler.ElasticsearchErrorHandler;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
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

    @Test
    void createsGlobalSearchMappingWithNextHearingDetails() throws IOException {
        when(ccdElasticClient.aliasExists(anyString())).thenReturn(true);

        listener.initialiseElasticSearchForGlobalSearch();

        ArgumentCaptor<String> mappingCaptor = ArgumentCaptor.forClass(String.class);
        verify(ccdElasticClient).upsertMapping(anyString(), mappingCaptor.capture());

        JsonNode nextHearingDetails = objectMapper.readTree(mappingCaptor.getValue())
            .at("/properties/data/properties/nextHearingDetails");

        assertThat(nextHearingDetails.isMissingNode()).isFalse();
        assertThat(nextHearingDetails.at("/properties/hearingID/type").asText()).isEqualTo("text");
        assertThat(nextHearingDetails.at("/properties/hearingID/fields/keyword/type").asText()).isEqualTo("keyword");
        assertThat(nextHearingDetails.at("/properties/hearingDateTime/type").asText()).isEqualTo("date");
        assertThat(nextHearingDetails.at("/properties/hearingDateTime/ignore_malformed").asBoolean()).isTrue();
    }

    private static class TestDefinitionImportListener extends ElasticGlobalSearchListener {
        public TestDefinitionImportListener(
            ObjectFactory<HighLevelCCDElasticClient> clientFactory,
            ElasticsearchErrorHandler elasticsearchErrorHandler) {
            super(clientFactory, elasticsearchErrorHandler);
        }
    }
}
