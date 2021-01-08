package uk.gov.hmcts.ccd.definition.store.elastic.exception;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ElasticsearchErrorTest {

    @Test
    void shouldCreateErrorObjectWithAllValues() {
        ElasticsearchStatusException exception =
            new ElasticsearchStatusException("Elasticsearch exception [type=TYPE, reason=REASON]",
                RestStatus.BAD_REQUEST);
        CaseTypeEntity caseType = new CaseTypeBuilder().withReference("CaseTypeId").build();

        ElasticsearchError result = new ElasticsearchError(exception, caseType);

        assertAll(
            () -> assertThat(result.getCaseType(), is(caseType)),
            () -> assertThat(result.getReason(), is("REASON")),
            () -> assertThat(result.getErrorType(), is("TYPE")),
            () -> assertThat(result.getCaseTypeReference(), is("CaseTypeId")),
            () -> assertThat(result.getException(), is(exception)),
            () -> assertThat(result.getMessage(), is("Elasticsearch exception [type=TYPE, reason=REASON]")),
            () -> assertThat(result.hasReason(), is(true))
        );
    }

    @Test
    void shouldCreateErrorObjectForMessageNotMatchingPattern() {
        ElasticsearchStatusException exception =
            new ElasticsearchStatusException("Unsupported message pattern", RestStatus.BAD_REQUEST);
        CaseTypeEntity caseType = new CaseTypeBuilder().withReference("CaseTypeId").build();

        ElasticsearchError result = new ElasticsearchError(exception, caseType);

        assertAll(
            () -> assertThat(result.getCaseType(), is(caseType)),
            () -> assertThat(result.getReason(), is(nullValue())),
            () -> assertThat(result.getErrorType(), is(nullValue())),
            () -> assertThat(result.getCaseTypeReference(), is("CaseTypeId")),
            () -> assertThat(result.getException(), is(exception)),
            () -> assertThat(result.getMessage(), is("Unsupported message pattern")),
            () -> assertThat(result.hasReason(), is(false))
        );
    }
}
