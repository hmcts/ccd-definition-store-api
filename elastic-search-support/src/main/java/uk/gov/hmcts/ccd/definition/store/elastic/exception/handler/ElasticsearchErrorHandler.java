package uk.gov.hmcts.ccd.definition.store.elastic.exception.handler;

import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticsearchError;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;
import java.util.Optional;

/**
 * Converts native Elasticsearch exceptions to provide more informative errors to clients.
 */
@Service
public class ElasticsearchErrorHandler {

    private final List<ElasticsearchErrorMessageBuilder> errorMessageBuilders;

    @Autowired
    public ElasticsearchErrorHandler(List<ElasticsearchErrorMessageBuilder> errorMessageBuilders) {
        this.errorMessageBuilders = errorMessageBuilders;
    }

    public ElasticSearchInitialisationException createException(ElasticsearchStatusException exception, 
                                                                CaseTypeEntity caseType) {
        ElasticsearchError error = new ElasticsearchError(exception, caseType);

        return error.hasReason()
            ? new ElasticSearchInitialisationException(buildMessage(error), exception)
            : new ElasticSearchInitialisationException(exception);
    }

    private String buildMessage(ElasticsearchError error) {
        return getErrorMessageBuilder(error)
            .map(messageBuilder -> messageBuilder.doBuildMessage(error))
            .orElse(error.getMessage());
    }

    private Optional<ElasticsearchErrorMessageBuilder> getErrorMessageBuilder(ElasticsearchError error) {
        return errorMessageBuilders.stream()
            .filter(messageBuilder -> messageBuilder.canHandleError(error))
            .findFirst();
    }
}
