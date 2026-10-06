package uk.gov.hmcts.ccd.definition.store.elastic.exception;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorResponse;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticsearchTestUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElasticsearchErrorTest {

    @Test
    void shouldCreateErrorObjectWithAllValues() {
        ElasticsearchException exception =
            ElasticsearchTestUtils.exception("Elasticsearch exception [type=TYPE, reason=REASON]",
                400);
        CaseTypeEntity caseType = new CaseTypeBuilder().withReference("CaseTypeId").build();

        ElasticsearchError result = new ElasticsearchError(exception, caseType);

        assertAll(
            () -> assertThat(result.getCaseType(), is(caseType)),
            () -> assertThat(result.getReason(), is("REASON")),
            () -> assertThat(result.getErrorType(), is("TYPE")),
            () -> assertThat(result.getCaseTypeReference(), is("CaseTypeId")),
            () -> assertThat(result.getException(), is(exception)),
            () -> assertThat(result.getMessage(), is(exception.getMessage())),
            () -> assertThat(result.hasReason(), is(true))
        );
    }

    @Test
    void shouldCreateErrorObjectWithoutReason() {
        ElasticsearchException exception =
            new ElasticsearchException("test", ErrorResponse.of(response -> response
                .status(400).error(error -> error.type("unknown"))));
        CaseTypeEntity caseType = new CaseTypeBuilder().withReference("CaseTypeId").build();

        ElasticsearchError result = new ElasticsearchError(exception, caseType);

        assertAll(
            () -> assertThat(result.getCaseType(), is(caseType)),
            () -> assertThat(result.getReason(), is(nullValue())),
            () -> assertThat(result.getErrorType(), is("unknown")),
            () -> assertThat(result.getCaseTypeReference(), is("CaseTypeId")),
            () -> assertThat(result.getException(), is(exception)),
            () -> assertThat(result.getMessage(), is(exception.getMessage())),
            () -> assertThat(result.hasReason(), is(false))
        );
    }

    @Test
    void rejectsMissingExceptionOrCaseType() {
        var exception = ElasticsearchTestUtils.exception("Mapping rejected", 400);
        var caseType = new CaseTypeBuilder().withReference("CaseTypeId").build();

        assertAll(
            () -> assertThrows(NullPointerException.class, () -> new ElasticsearchError(null, caseType)),
            () -> assertThrows(NullPointerException.class, () -> new ElasticsearchError(exception, null))
        );
    }

}
