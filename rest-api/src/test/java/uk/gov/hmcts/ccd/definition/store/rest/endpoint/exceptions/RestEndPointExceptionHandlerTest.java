package uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import uk.gov.hmcts.ccd.definition.store.domain.exception.BadRequestException;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;

import javax.persistence.OptimisticLockException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;


public class RestEndPointExceptionHandlerTest {

    private RestEndPointExceptionHandler exceptionHandler;

    @Before
    public void setUp() {
        exceptionHandler = new RestEndPointExceptionHandler();
    }

    @Test
    public void handleExceptionShouldAggregateInnerMessages() {
        final RuntimeException exception = new RuntimeException("Outer message", new Exception("Inner message"));

        final ResponseEntity<Object> response = exceptionHandler.handleException(exception, mock(WebRequest.class));

        assertThat(response.getBody().toString(), equalTo("Outer message\nInner message"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void handleExceptionShouldStopMessageAggregationAtDepth5() {
        final RuntimeException exception = new RuntimeException("Depth 1",
            new Exception("Depth 2",
                new Exception("Depth 3",
                    new Exception("Depth 4",
                        new Exception("Depth 5",
                            new Exception("Depth 6"))))));

        final ResponseEntity<Object> response = exceptionHandler.handleException(exception, mock(WebRequest.class));

        assertThat(response.getBody().toString(), equalTo("Depth 1\nDepth 2\nDepth 3\nDepth 4\nDepth 5"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void handleConflictWhenOptimisticLockExceptionHappens() {
        final OptimisticLockException exception = new OptimisticLockException(
            "Outer message", new Exception("Inner message"));

        final ResponseEntity<Object> response = exceptionHandler.handleConflict(exception, mock(WebRequest.class));

        assertThat(response.getStatusCode(), equalTo(HttpStatus.CONFLICT));
        assertThat(response.getBody().toString(), equalTo("Outer message\nInner message"));
    }

    @Test
    public void handleNotFound() {
        final NotFoundException exception = new NotFoundException("Not found message");

        final ResponseEntity<Object> response = exceptionHandler.handleNotFound(exception, mock(WebRequest.class));

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
        assertThat(response.getBody().toString(), equalTo("Not found message"));
    }

    @Test
    public void handleBadRequest() {
        final BadRequestException exception = new BadRequestException("Invalid request");

        final ResponseEntity<Object> response = exceptionHandler.handleBadRequest(exception, mock(WebRequest.class));

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        assertThat(response.getBody().toString(), equalTo("Invalid request"));
    }
}
