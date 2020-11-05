package uk.gov.hmcts.ccd.definition.store.elastic.exception.handler;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ElasticsearchErrorHandlerTest {

    private ElasticsearchErrorHandler errorHandler;

    @Mock
    private CaseTypeEntity caseTypeEntity;

    private ElasticsearchErrorMessageBuilder messageBuilderA;
    private ElasticsearchErrorMessageBuilder messageBuilderB;
    private List<ElasticsearchErrorMessageBuilder> errorMessageBuilders;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        messageBuilderA = new TestElasticsearchErrorMessageBuilder(".*PATTERN ONE.*", "ERROR MESSAGE ONE");
        messageBuilderB = new TestElasticsearchErrorMessageBuilder("PATTERN TWO .+", "ERROR MESSAGE TWO");
        errorMessageBuilders = Arrays.asList(messageBuilderA, messageBuilderB);
        errorHandler = new ElasticsearchErrorHandler(errorMessageBuilders);
    }

    @Test
    void shouldCreateExceptionForKnownReason() {
        ElasticsearchStatusException exception =
            elasticException("Elasticsearch exception [type=TYPE, reason=PATTERN TWO REASON]");

        ElasticSearchInitialisationException result = errorHandler.createException(exception, caseTypeEntity);

        assertAll(
            () -> assertThat(result.getMessage(), is("ERROR MESSAGE TWO"))
        );
    }

    @Test
    void shouldCreateExceptionForUnhandledReasonMatchingErrorPattern() {
        ElasticsearchStatusException exception =
            elasticException("Elasticsearch exception [type=TYPE, reason=UNHANDLED REASON]");

        ElasticSearchInitialisationException result = errorHandler.createException(exception, caseTypeEntity);

        assertAll(
            () -> assertThat(result.getMessage(), is("Elasticsearch exception [type=TYPE, reason=UNHANDLED REASON]"))
        );
    }

    @Test
    void shouldCreateExceptionForErrorNotMatchingPattern() {
        ElasticsearchStatusException exception = elasticException("UNHANDLED ERROR MESSAGE");

        ElasticSearchInitialisationException result = errorHandler.createException(exception, caseTypeEntity);

        assertAll(
            () -> assertThat(result.getCause().getMessage(), is("UNHANDLED ERROR MESSAGE"))
        );
    }

    private ElasticsearchStatusException elasticException(String exceptionMessage) {
        return new ElasticsearchStatusException(exceptionMessage, RestStatus.BAD_REQUEST);
    }
}
