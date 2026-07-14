package uk.gov.hmcts.ccd.definition.store.elastic;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Utilities for CCD case indices named {@code {baseIndexName}-000001}, {@code -000002}, etc.
 */
public final class VersionedCaseIndexHelper {

    public static final String FIRST_INDEX_SUFFIX = "-000001";

    private VersionedCaseIndexHelper() {
    }

    public static String firstVersionIndexName(String baseIndexName) {
        return baseIndexName + FIRST_INDEX_SUFFIX;
    }

    public static Pattern versionedIndexNamePattern(String baseIndexName) {
        return Pattern.compile(Pattern.quote(baseIndexName) + "-(\\d+)$");
    }

    public static Optional<String> findLatestVersionedIndex(Collection<String> indexNames, String baseIndexName) {
        Pattern pattern = versionedIndexNamePattern(baseIndexName);
        return indexNames.stream()
            .filter(indexName -> pattern.matcher(indexName).matches())
            .max(Comparator.comparingInt(indexName -> versionNumber(pattern, indexName)));
    }

    private static int versionNumber(Pattern pattern, String indexName) {
        var matcher = pattern.matcher(indexName);
        if (!matcher.matches()) {
            return -1;
        }
        return Integer.parseInt(matcher.group(1));
    }
}
