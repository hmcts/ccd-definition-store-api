package uk.gov.hmcts.ccd.definition.store.elastic.exception;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ElasticsearchError {

    private ElasticsearchException exception;
    private CaseTypeEntity caseType;
    private String message;
    private String errorType;
    private String reason;

    public ElasticsearchError(@NonNull ElasticsearchException exception, @NonNull CaseTypeEntity caseType) {
        initValues(exception, caseType);
    }

    public String getCaseTypeReference() {
        return caseType.getReference();
    }

    public boolean hasReason() {
        return reason != null;
    }

    private void initValues(ElasticsearchException exception, CaseTypeEntity caseType) {
        this.caseType = caseType;
        this.exception = exception;
        this.message = exception.getMessage();
        if (exception.error() != null) {
            this.errorType = exception.error().type();
            this.reason = exception.error().reason();
        }
    }
}
