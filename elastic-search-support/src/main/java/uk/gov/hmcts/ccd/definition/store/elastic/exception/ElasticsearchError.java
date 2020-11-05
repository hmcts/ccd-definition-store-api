package uk.gov.hmcts.ccd.definition.store.elastic.exception;

import lombok.Getter;
import lombok.NonNull;
import org.elasticsearch.ElasticsearchStatusException;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class ElasticsearchError {

    private static final Pattern MESSAGE_PATTERN =
        Pattern.compile("Elasticsearch exception \\[type=(.*), reason=(.*)\\.*]");

    private ElasticsearchStatusException exception;
    private CaseTypeEntity caseType;
    private String message;
    private String errorType;
    private String reason;

    private ElasticsearchError() { 
    }

    public ElasticsearchError(@NonNull ElasticsearchStatusException exception, @NonNull CaseTypeEntity caseType) {
        initValues(exception, caseType);
    }

    public String getCaseTypeReference() {
        return caseType.getReference();
    }

    public boolean hasReason() {
        return reason != null;
    }

    private void initValues(ElasticsearchStatusException exception, CaseTypeEntity caseType) {
        this.caseType = caseType;
        this.exception = exception;
        this.message = exception.getMessage();
        Matcher matcher = MESSAGE_PATTERN.matcher(message);
        if (matcher.matches()) {
            this.errorType = matcher.group(1);
            this.reason = matcher.group(2);
        }
    }
}
