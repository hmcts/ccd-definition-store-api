package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Checks that real parser failures retain their approved text at the HTTP boundary. */
public final class ValidationResponseAssertions {
    private ValidationResponseAssertions() {
    }

    public static void assertValidationResponse(RuntimeException failure) {
        var mvc = MockMvcBuilders.standaloneSetup(new FailureController(failure))
            .setControllerAdvice(new RestResponseEntityExceptionHandler(null)).build();
        failure.addSuppressed(new IllegalStateException("PRIVATE technical diagnostic"));
        assertDoesNotThrow(() -> mvc.perform(get("/validation"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(content().string(failure.getMessage())));
    }

    @RestController
    public static class FailureController {
        private final RuntimeException failure;

        public FailureController(RuntimeException failure) {
            this.failure = failure;
        }

        @GetMapping("/validation")
        public void fail() {
            throw failure;
        }
    }
}
