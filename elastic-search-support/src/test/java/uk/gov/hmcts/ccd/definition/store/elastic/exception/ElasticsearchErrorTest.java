package uk.gov.hmcts.ccd.definition.store.elastic.exception;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.ElasticsearchExceptionFactory.elasticsearchException;

class ElasticsearchErrorTest {

    @Test
    void shouldCreateErrorObjectWithAllValues() {
        ElasticsearchException exception = elasticsearchException("TYPE", "REASON");
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
    void shouldCreateErrorObjectForMessageNotMatchingPattern() {
        ElasticsearchException exception = mock(ElasticsearchException.class);
        when(exception.getMessage()).thenReturn("Unsupported message pattern");
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
