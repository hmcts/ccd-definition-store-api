package uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Factory;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

/**
 * A Matcher for comparing JSON. From:
 * https://github.com/sharfah/java-utils
 * Example usage:
 * <pre>
 * assertThat(new String[] {"foo", "bar"}, equalToJSON("[\"foo\", \"bar\"]"));
 * </pre>
 */
public class IsEqualJSON extends DiagnosingMatcher<Object> {

    private final String expectedJSON;
    private JSONCompareMode jsonCompareMode;
    private JSONComparator comparator;

    public IsEqualJSON(final String expectedJSON) {
        this.expectedJSON = expectedJSON;
        this.jsonCompareMode = JSONCompareMode.STRICT;
    }

    public IsEqualJSON(String expectedJSON, JSONComparator comparator) {
        this.expectedJSON = expectedJSON;
        this.jsonCompareMode = JSONCompareMode.STRICT;
        this.comparator = comparator;
    }

    /**
     * Changes this matcher's JSON compare mode to lenient.
     * @return this matcher
     */
    public IsEqualJSON leniently() {
        jsonCompareMode = JSONCompareMode.LENIENT;
        return this;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText(expectedJSON);
    }

    @Override
    protected boolean matches(final Object actual,
                              final Description mismatchDescription) {
        try {
            final String actualJSON = toJsonString(actual);

            if (comparator == null) {
                comparator = new DefaultComparator(jsonCompareMode);
            }

            final JSONCompareResult result = JSONCompare.compareJSON(expectedJSON, actualJSON, comparator);

            if (!result.passed()) {
                mismatchDescription.appendText(result.getMessage());
            }
            return result.passed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the specified object into a JSON string.
     * @param o the object to convert
     * @return the JSON string
     */
    private static String toJsonString(final Object o) {
        try {
            return o instanceof String ? (String) o : new ObjectMapper().writeValueAsString(o);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the specified file into a string.
     * @param path the path to read
     * @return the contents of the file
     */
    private static String getFileContents(final Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a matcher that matches when the examined object
     * is equal to the specified JSON string.
     * For example:
     * <pre>
     * assertThat(new String[] {"foo", "bar"},
     *            equalToJSON("[\"foo\", \"bar\"]"));
     * </pre>
     *
     * @param expectedJSON the expected JSON string
     * @return the JSON matcher
     */
    @Factory
    public static IsEqualJSON equalToJSON(final String expectedJSON) {
        return new IsEqualJSON(expectedJSON);
    }

    @Factory
    public static IsEqualJSON equalToJSON(final String expectedJSON, JSONComparator comparator) {
        return new IsEqualJSON(expectedJSON, comparator);
    }

    /**
     * Creates a matcher that matches when the examined object
     * is equal to the JSON in the specified file.
     * For example:
     * <pre>
     * assertThat(new String[] {"foo", "bar"},
     *            equalToJSONInFile(Paths.get("/tmp/foo.json"));
     * </pre>
     *
     * @param expectedPath the path containing the expected JSON
     * @return the JSON matcher
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @Factory
    public static IsEqualJSON equalToJSONInFile(final Path expectedPath) {
        return equalToJSON(getFileContents(expectedPath));
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @Factory
    public static IsEqualJSON equalToJSONInFile(final Path expectedPath, JSONComparator comparator) {
        return equalToJSON(getFileContents(expectedPath), comparator);
    }

    /**
     * Creates a matcher that matches when the examined object
     * is equal to the JSON contained in the file with the specified name.
     * For example:
     * <pre>
     * assertThat(new String[] {"foo", "bar"},
     *            equalToJSONInFile("/tmp/foo.json"));
     * </pre>
     *
     * @param expectedFileName the name of the file containing the expected JSON
     * @return the JSON matcher
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @Factory
    public static IsEqualJSON equalToJSONInFile(final String expectedFileName) {
        return equalToJSONInFile(Paths.get(expectedFileName));
    }
}
