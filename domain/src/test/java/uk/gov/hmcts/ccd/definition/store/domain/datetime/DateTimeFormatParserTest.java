package uk.gov.hmcts.ccd.definition.store.domain.datetime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateTimeFormatParserTest {

    private DateTimeFormatParser parser;
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("VzOXxZ");

    @BeforeEach
    void setUp() {
        parser = new DateTimeFormatParser();
    }

    @ParameterizedTest
    @ArgumentsSource(ValidDateTimeFormatArgumentsProvider.class)
    void shouldParseValidFormat(String dateTimeFormat) throws InvalidDateTimeFormatException {
        parser.parseDateTimeFormat(dateTimeFormat, DEFAULT_PATTERN);
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidDateTimeFormatArgumentsProvider.class)
    void shouldFailToParseInvalidFormat(String dateTimeFormat) {
        assertThrows(InvalidDateTimeFormatException.class, () -> {
            parser.parseDateTimeFormat(dateTimeFormat, DEFAULT_PATTERN);
        });
    }

    @Test
    void shouldParseValidFormatWithProvidedPattern() throws InvalidDateTimeFormatException {
        final String dateTimeFormat = "yyyyMMddHHmmssSSS";
        final Pattern pattern = Pattern.compile("VzOXxZ");
        parser.parseDateTimeFormat(dateTimeFormat, pattern);
    }

    @Test
    void shouldFailIfValidFormatDoesNotMeetProvidedPattern() {
        final String dateTimeFormat = "HHmmss V";
        final Pattern pattern = Pattern.compile("VzOXxZ");
        assertThrows(InvalidDateTimeFormatException.class, () -> {
            parser.parseDateTimeFormat(dateTimeFormat, pattern);
        });
    }

    private static class ValidDateTimeFormatArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("HHmmss"),
                Arguments.of("ddMMyyyy"),
                Arguments.of("EEEE, MMMM dd, yyyy, h:mm:ss a"),
                Arguments.of("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                Arguments.of("'Escaped Text ####'")
            );
        }
    }

    private static class InvalidDateTimeFormatArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("HHHmmmsss"),
                Arguments.of("DDDD"),
                Arguments.of("###")
            );
        }
    }
}
