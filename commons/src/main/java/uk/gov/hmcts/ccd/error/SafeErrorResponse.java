package uk.gov.hmcts.ccd.error;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Map;

/** Generic public responses and private diagnostics shared by the controller advices. */
public final class SafeErrorResponse {
    private SafeErrorResponse() {
    }

    public static Map<String, String> body(HttpStatusCode status) {
        return body(status, HttpStatus.valueOf(status.value()).getReasonPhrase());
    }

    public static Map<String, String> body(HttpStatusCode status, String approvedMessage) {
        return Map.of("message", approvedMessage, "code", ErrorCode.fromStatus(status.value()).name());
    }

    public static void log(Logger logger, HttpStatusCode status, Exception exception) {
        if (status.is5xxServerError()) {
            logger.error("Request failed: code={} status={}",
                ErrorCode.fromStatus(status.value()), status.value(), exception);
        } else {
            logger.warn("Request failed: code={} status={}",
                ErrorCode.fromStatus(status.value()), status.value(), exception);
        }
    }
}
