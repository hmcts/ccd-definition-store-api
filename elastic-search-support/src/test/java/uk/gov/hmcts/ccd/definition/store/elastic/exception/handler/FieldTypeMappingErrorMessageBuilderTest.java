package uk.gov.hmcts.ccd.definition.store.elastic.exception.handler;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticsearchError;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class FieldTypeMappingErrorMessageBuilderTest {

    private FieldTypeMappingErrorMessageBuilder messageBuilder;

    private CaseTypeEntity caseType = new CaseTypeBuilder().withReference("CaseTypeId").build();

    @BeforeEach
    void setUp() {
        messageBuilder = new FieldTypeMappingErrorMessageBuilder();
    }

    @Test
    void shouldBuildMessageForDataField() {
        ElasticsearchError error = createNativeEsError("data.FieldName", "date", "keyword");

        String result = messageBuilder.doBuildMessage(error);

        assertAll(
            () -> assertThat(result, is("Field 'data.FieldName' in case type 'CaseTypeId' does not match the field "
                + "type of the field with this id in the previous definition version. Please check the field type "
                + "change is intended and request an Elasticsearch reindex. "
                + "Previous Elasticsearch type was 'date', new type is 'keyword'."))
        );
    }

    @Test
    void shouldBuildMessageForDataClassificationField() {
        ElasticsearchError error = createNativeEsError("data_classification.FieldName", "date", "keyword");

        String result = messageBuilder.doBuildMessage(error);

        assertAll(
            () -> assertThat(result, is("Field 'data_classification.FieldName' in case type 'CaseTypeId' does not "
                + "match the field type of the field with this id in the previous definition version. Please check the "
                + "field type change is intended and request an Elasticsearch reindex. "
                + "Previous Elasticsearch type was 'date', new type is 'keyword'."))
        );
    }

    @Test
    void shouldBuildMessageForNonServiceDefinedField() {
        ElasticsearchError error = createNativeEsError("security_classification", "text", "keyword");

        String result = messageBuilder.doBuildMessage(error);

        assertAll(
            () -> assertThat(result, is("Field 'security_classification' in case type 'CaseTypeId' does not "
                + "match the expected Elasticsearch type. Please request an Elasticsearch reindex for this case type. "
                + "Previous Elasticsearch type was 'text', new type is 'keyword'."))
        );
    }

    private ElasticsearchError createNativeEsError(String mapperName, String currentType, String mergedType) {
        return new ElasticsearchError(
            new ElasticsearchStatusException(String.format("Elasticsearch exception [type=illegal_argument_exception, "
            + "reason=mapper [%s] of different type, current_type [%s], merged_type [%s]]",
                mapperName, currentType, mergedType), RestStatus.BAD_REQUEST),
            caseType);
    }
}
