package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class RestResponseEntityExceptionHandlerTest {

    private RestResponseEntityExceptionHandler exceptionHandler;

    @Before
    public void setUp() {
        exceptionHandler = new RestResponseEntityExceptionHandler(null);
    }

    @Test
    public void handleBadRequest_shouldAggregateInnerMessages() throws Exception {
        final RuntimeException exception = new RuntimeException("Outer message", new Exception("Inner message"));

        final ResponseEntity<Object> response = exceptionHandler.handleBadRequest(exception, mock(WebRequest.class));

        assertThat(response.getBody().toString(), equalTo("Outer message\nInner message"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void handleBadRequest_shouldStopMessageAggregationAtDepth5() throws Exception {
        final RuntimeException exception = new RuntimeException("Depth 1",
            new Exception("Depth 2",
                new Exception("Depth 3",
                    new Exception("Depth 4",
                        new Exception("Depth 5",
                            new Exception("Depth 6"))))));

        final ResponseEntity<Object> response = exceptionHandler.handleBadRequest(exception, mock(WebRequest.class));

        assertThat(response.getBody().toString(), equalTo("Depth 1\nDepth 2\nDepth 3\nDepth 4\nDepth 5"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

}
