package uk.gov.hmcts.ccd.definition.store.elastic;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ElasticsearchTestUtils {

    private static final Pattern LEGACY_MESSAGE =
        Pattern.compile("Elasticsearch exception \\[type=(.*), reason=(.*)\\]");

    private ElasticsearchTestUtils() {
    }

    // Preserve existing error-message fixtures while using the HTTP client's real error type.
    public static ElasticsearchException exception(String message, int status) {
        Matcher matcher = LEGACY_MESSAGE.matcher(message);
        boolean structured = matcher.matches();
        return new ElasticsearchException("test", ErrorResponse.of(response -> response
            .status(status)
            .error(error -> error.type(structured ? matcher.group(1) : "unknown")
                .reason(structured ? matcher.group(2) : message))));
    }
}
