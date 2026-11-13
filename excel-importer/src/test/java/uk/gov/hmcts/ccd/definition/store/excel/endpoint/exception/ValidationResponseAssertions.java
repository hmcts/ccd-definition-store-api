package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.excel.parser.SpreadsheetParsingException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** Checks that real parser failures retain their approved text at the HTTP boundary. */
public final class ValidationResponseAssertions {
    private ValidationResponseAssertions() {
    }

    public static void assertValidationResponse(RuntimeException failure) {
        assertDoesNotThrow(() -> mvc(failure).perform(get("/validation"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(content().string(failure.getMessage())));
    }

    public static void assertImportValidationResponse(SpreadsheetParsingException failure, String expectedMessage) {
        assertEquals(expectedMessage, failure.getMessage());
        assertDoesNotThrow(() -> mvc(failure).perform(get("/validation"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value(expectedMessage))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/import"))
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.trace").doesNotExist())
            .andExpect(content().string(not(containsString("PRIVATE technical diagnostic")))));
    }

    private static MockMvc mvc(RuntimeException failure) {
        failure.addSuppressed(new IllegalStateException("PRIVATE technical diagnostic"));
        return MockMvcBuilders.standaloneSetup(new FailureController(failure))
            .setControllerAdvice(new RestResponseEntityExceptionHandler(null)).build();
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
