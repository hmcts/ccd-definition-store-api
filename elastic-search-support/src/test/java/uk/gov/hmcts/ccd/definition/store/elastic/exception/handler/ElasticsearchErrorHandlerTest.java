package uk.gov.hmcts.ccd.definition.store.elastic.exception.handler;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticsearchTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
        MockitoAnnotations.openMocks(this);
        messageBuilderA = new TestElasticsearchErrorMessageBuilder(".*PATTERN ONE.*", "ERROR MESSAGE ONE");
        messageBuilderB = new TestElasticsearchErrorMessageBuilder("PATTERN TWO .+", "ERROR MESSAGE TWO");
        errorMessageBuilders = Arrays.asList(messageBuilderA, messageBuilderB);
        errorHandler = new ElasticsearchErrorHandler(errorMessageBuilders);
    }

    @Test
    void shouldCreateExceptionForKnownReason() {
        ElasticsearchException exception =
            elasticException("Elasticsearch exception [type=TYPE, reason=PATTERN TWO REASON]");

        ElasticSearchInitialisationException result = errorHandler.createException(exception, caseTypeEntity);

        assertAll(
            () -> assertThat(result.getMessage(), is("ERROR MESSAGE TWO"))
        );
    }

    @Test
    void shouldCreateExceptionForUnhandledReasonMatchingErrorPattern() {
        ElasticsearchException exception =
            elasticException("Elasticsearch exception [type=TYPE, reason=UNHANDLED REASON]");

        ElasticSearchInitialisationException result = errorHandler.createException(exception, caseTypeEntity);

        assertAll(
            () -> assertThat(result.getMessage(), is(exception.getMessage()))
        );
    }

    @Test
    void shouldCreateExceptionForErrorNotMatchingPattern() {
        ElasticsearchException exception = elasticException("UNHANDLED ERROR MESSAGE");

        ElasticSearchInitialisationException result = errorHandler.createException(exception, caseTypeEntity);

        assertAll(
            () -> assertThat(result.getCause().getMessage(), is(exception.getMessage()))
        );
    }

    private ElasticsearchException elasticException(String exceptionMessage) {
        return ElasticsearchTestUtils.exception(exceptionMessage, 400);
    }
}
