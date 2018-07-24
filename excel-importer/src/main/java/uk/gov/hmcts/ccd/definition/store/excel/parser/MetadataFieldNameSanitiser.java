package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataFieldNameSanitiser {

    private static final Pattern METADATA_PATTERN_IN_LABEL = Pattern.compile("\\[[^\\[]*]");
    private static final Pattern METADATA_FIELD_NAME_PATTERN = Pattern.compile("\\[(.*?)]");

    private MetadataFieldNameSanitiser() {}

    public static String sanitiseMetadataFieldNameInString(String label) {
        Matcher metadataPatternMatcher = METADATA_PATTERN_IN_LABEL.matcher(label);
        String result = label;
        while (metadataPatternMatcher.find()) {
            String fieldWithName = metadataPatternMatcher.group();
            Matcher matcher = METADATA_FIELD_NAME_PATTERN.matcher(fieldWithName);
            if (matcher.find()) {
                result = result.replace(fieldWithName, matcher.group(1).toLowerCase());
            }
        }

        return result;
    }

    public static String constructMetadataFieldName(String originalName) {
        return originalName == null ? null : String.join(originalName.toUpperCase(), "[", "]");
    }
}
