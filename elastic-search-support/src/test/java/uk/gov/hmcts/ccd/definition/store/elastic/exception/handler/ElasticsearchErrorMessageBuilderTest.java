package uk.gov.hmcts.ccd.definition.store.elastic.exception.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticsearchError;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ccd.definition.store.elastic.ElasticsearchExceptionFactory.elasticsearchException;

class ElasticsearchErrorMessageBuilderTest {

    private ElasticsearchErrorMessageBuilder messageBuilder;

    private CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

    @BeforeEach
    void setUp() {
        messageBuilder = new TestElasticsearchErrorMessageBuilder(".*REASON.*", "MESSAGE");
    }

    @Test
    void shouldBuildMessage() {
        ElasticsearchError error = elasticsearchError("SOME REASON");

        String result = messageBuilder.doBuildMessage(error);

        assertAll(
            () -> assertThat(result, is("MESSAGE"))
        );
    }

    @Test
    void shouldThrowExceptionWhenErrorDoesNotContainCompatibleReason() {
        ElasticsearchError error = elasticsearchError("INVALID");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> messageBuilder.doBuildMessage(error));

        assertAll(
            () -> assertThat(exception.getMessage(),
                is("This error message builder does not support errors with the provided reason 'INVALID'."))
        );
    }

    @Test
    void shouldReturnTrueWhenBuilderCanHandleError() {
        ElasticsearchError error = elasticsearchError("A REASON THAT MEETS THE DEFINED PATTERN");

        boolean result = messageBuilder.canHandleError(error);

        assertAll(
            () -> assertThat(result, is(true))
        );
    }

    @Test
    void shouldReturnFalseWhenBuilderCanNotHandleError() {
        ElasticsearchError error = elasticsearchError("INVALID");

        boolean result = messageBuilder.canHandleError(error);

        assertAll(
            () -> assertThat(result, is(false))
        );
    }

    private ElasticsearchError elasticsearchError(String reason) {
        return new ElasticsearchError(
            elasticsearchException("TYPE", reason),
            caseTypeEntity);
    }
}
