package uk.gov.hmcts.ccd.definition.store.elastic.exception.handler;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticsearchError;

import java.util.regex.Matcher;

@Component
public class FieldTypeMappingErrorMessageBuilder extends ElasticsearchErrorMessageBuilder {

    private static final String REASON_PATTERN =
        "mapper \\[(.+)\\] of different type, current_type \\[(.+)\\], merged_type \\[(.+)\\]";
    private static final String DATA_PREFIX = "data.";
    private static final String DATA_CLASSIFICATION_PREFIX = "data_classification.";

    private static final String SERVICE_DEFINED_FIELD_TEMPLATE = "Field '%s' in case type '%s' does not match the "
        + "field type of the field with this id in the previous definition version. Please check the field type "
        + "change is intended and request an Elasticsearch reindex. "
        + "Previous Elasticsearch type was '%s', new type is '%s'.";
    private static final String CCD_METADATA_FIELD_TEMPLATE = "Field '%s' in case type '%s' does not match the "
        + "expected Elasticsearch type. Please request an Elasticsearch reindex for this case type. "
        + "Previous Elasticsearch type was '%s', new type is '%s'.";

    public FieldTypeMappingErrorMessageBuilder() {
        super(REASON_PATTERN);
    }

    @Override
    protected String buildMessage(ElasticsearchError error) {
        Matcher m = getReasonMatcher(error.getReason());
        String fieldPath = m.group(1);
        return String.format(
            isServiceDefinedField(fieldPath) ? SERVICE_DEFINED_FIELD_TEMPLATE : CCD_METADATA_FIELD_TEMPLATE,
            fieldPath, error.getCaseTypeReference(), m.group(2), m.group(3));
    }

    private boolean isServiceDefinedField(String path) {
        return path.startsWith(DATA_PREFIX) || path.startsWith(DATA_CLASSIFICATION_PREFIX);
    }
}
