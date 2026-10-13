package uk.gov.hmcts.ccd.definition.store.elastic.exception.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticsearchError;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.elastic.ElasticsearchExceptionFactory.elasticsearchException;

class MaxFieldLimitErrorMessageBuilderTest {

    private MaxFieldLimitErrorMessageBuilder messageBuilder;

    private CaseTypeEntity caseType = new CaseTypeBuilder().withReference("CaseTypeId").build();

    @BeforeEach
    void setUp() {
        messageBuilder = new MaxFieldLimitErrorMessageBuilder();
    }

    @Test
    void shouldBuildMessageWhenReasonMatchesPattern() {
        ElasticsearchError error = new ElasticsearchError(elasticsearchException("illegal_argument_exception",
            "Limit of total fields [5000] in index [casetypeid_cases-000001] has been exceeded"), caseType);

        String result = messageBuilder.doBuildMessage(error);

        assertAll(
            () -> assertThat(result, is("Case type 'CaseTypeId' exceeds the limit of 5000 fields permitted by "
                + "Elasticsearch. To reduce the number, please configure fields for this case type that are not "
                + "required to be searched on to be non-searchable in the definition."))
        );
    }
}
