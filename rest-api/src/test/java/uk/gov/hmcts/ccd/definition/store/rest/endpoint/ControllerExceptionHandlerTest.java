package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import uk.gov.hmcts.ccd.definition.store.domain.exception.BadRequestException;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules.CaseTypeValidationResult;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.DuplicateFoundException;

import java.io.IOException;
import java.util.Map;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

class ControllerExceptionHandlerTest {

    private static final String INNER_MESSAGE = "Exception message";
    private ControllerExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ControllerExceptionHandler();
    }

    @Test
    void handleExceptionShouldAggregateInnerMessages() {
        final RuntimeException exception = new RuntimeException("Outer message", new Exception("Inner message"));

        final ResponseEntity<Object> response = handler.handleException(exception, mock(WebRequest.class));

        assertThat(response.getBody().toString(), equalTo("Outer message\nInner message"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void handleExceptionShouldStopMessageAggregationAtDepth5() {
        final RuntimeException exception = new RuntimeException("Depth 1",
            new Exception("Depth 2",
                new Exception("Depth 3",
                    new Exception("Depth 4",
                        new Exception("Depth 5",
                            new Exception("Depth 6"))))));

        final ResponseEntity<Object> response = handler.handleException(exception, mock(WebRequest.class));

        assertThat(response.getBody().toString(), equalTo("Depth 1\nDepth 2\nDepth 3\nDepth 4\nDepth 5"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void handleConflictWhenOptimisticLockExceptionHappens() {
        final OptimisticLockException exception = new OptimisticLockException(
            "Outer message", new Exception("Inner message"));

        final ResponseEntity<Object> response = handler.handleConflict(exception, mock(WebRequest.class));

        assertThat(response.getStatusCode(), equalTo(HttpStatus.CONFLICT));
        assertThat(response.getBody().toString(), equalTo("Outer message\nInner message"));
    }

    @Test
    void handleBadRequest() {
        final BadRequestException exception = new BadRequestException("Invalid request");

        final ResponseEntity<Object> response = handler.handleBadRequest(exception, mock(WebRequest.class));

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        assertThat(response.getBody().toString(), equalTo("Invalid request"));
    }

    @Nested
    @DisplayName("objectNotFound()")
    class ObjectNotFound {
        @Test
        @DisplayName("should return error details as map")
        void shouldReturnErrorDetailsAsMap() {
            final Map<String, String> details = handler.objectNotFound(new NotFoundException(INNER_MESSAGE));

            assertAll(
                () -> assertThat(details, is(notNullValue())),
                () -> assertThat(details.get("message"), equalTo("Object Not Found for:" + INNER_MESSAGE))
            );
        }
    }

    @Nested
    @DisplayName("objectFound()")
    class ObjectFound {
        @Test
        @DisplayName("should return error details as map")
        void shouldReturnErrorDetailsAsMap() {
            final Map<String, String> details = handler.objectFound(new DuplicateFoundException(INNER_MESSAGE));

            assertAll(
                () -> assertThat(details, is(notNullValue())),
                () -> assertThat(details.get("message"), equalTo("Object already exists for:" + INNER_MESSAGE))
            );
        }
    }

    @Nested
    @DisplayName("generalIllegal()")
    class GeneralIllegal {
        @Test
        @DisplayName("should return error details as map")
        void shouldReturnErrorDetailsAsMap() {
            final Map<String, String> details = handler.generalIllegal(new IllegalArgumentException(INNER_MESSAGE));

            assertAll(
                () -> assertThat(details, is(notNullValue())),
                () -> assertThat(details.get("message"), equalTo("Illegal Input:" + INNER_MESSAGE))
            );
        }
    }

    @Nested
    @DisplayName("handleIOException()")
    class HandleIOException {
        @Test
        @DisplayName("should return error details as map")
        void shouldReturnErrorDetailsAsMap() {
            final Map<String, String> details = handler.handleIOException(new IOException(INNER_MESSAGE));

            assertAll(
                () -> assertThat(details, is(notNullValue())),
                () -> assertThat(details.get("message"), equalTo("Unexpected Error: " + INNER_MESSAGE))
            );
        }
    }

    @Nested
    @DisplayName("caseTypeValidation()")
    class CaseTypeValidation {
        @Test
        @DisplayName("should return error details as map")
        void shouldReturnErrorDetailsAsMap() {
            final String error = handler.caseTypeValidation(
                new CaseTypeValidationException(new CaseTypeValidationResult("")));

            assertAll(
                () -> assertThat(error, is(notNullValue()))
            );
        }
    }

    @Test
    void elasticSearchInitialisationException() {
        Map<String, String> details = handler.elasticSearchInitialisationException(
            new ElasticSearchInitialisationException(new ArrayIndexOutOfBoundsException("test")));

        assertAll(
            () -> assertThat(details, is(notNullValue())),
            () -> assertThat(details.get("message"), containsString("ElasticSearch initialisation exception"))
        );
    }


}
