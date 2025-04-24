package uk.gov.hmcts.ccd.definition.store.excel.util.mapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ColumnNameTest {

    private static final ColumnName COLUMN_WITH_ALIAS = ColumnName.ACCESS_PROFILE;
    private static final ColumnName COLUMN_WITHOUT_ALIAS = ColumnName.FIELD_TYPE;

    @Test
    public void testRequiredColumnsForJurisdiction() {

        assertRequired(
                SheetName.JURISDICTION,
                Arrays.asList(ColumnName.NAME, ColumnName.ID));

    }

    @Test
    public void testRequiredColumnsForCaseEventToFields() {

        assertRequired(
                SheetName.CASE_EVENT_TO_FIELDS,
                Arrays.asList(ColumnName.CASE_FIELD_ID, ColumnName.CASE_TYPE_ID,
                        ColumnName.CASE_EVENT_ID, ColumnName.PAGE_ID));

    }

    private void assertRequired(SheetName sheetName, List<ColumnName> requiredColumns) {
        for (ColumnName columnName : ColumnName.values()) {
            boolean required = requiredColumns.contains(columnName);
            assertEquals(
                    ColumnName.isRequired(sheetName, columnName),
                    required,
                    String.format(
                            "Expected column %s on %s to be %s", columnName, sheetName,
                            required == true ? "required" : "not required"));
        }
    }

    @Test
    public void verifyTestColumnAliasConfig() {
        assertAll(
                () -> assertTrue(
                        COLUMN_WITH_ALIAS.getAliases().length > 0,
                        String.format(
                                "Expected column %s to have aliases", COLUMN_WITH_ALIAS)),
                () -> assertEquals(
                        0,
                        COLUMN_WITHOUT_ALIAS.getAliases().length,
                        String.format(
                                "Expected column %s to have no aliases", COLUMN_WITHOUT_ALIAS)));
    }

    @Test
    public void equalsColumnNameOrAlias_columnWithAlias_trueIfNameMatch() {
        assertNameOrAliasMatch(COLUMN_WITH_ALIAS, false);
    }

    @Test
    public void equalsColumnNameOrAlias_columnWithAlias_trueIfAliasMatch() {
        assertNameOrAliasMatch(COLUMN_WITH_ALIAS, true);
    }

    @Test
    public void equalsColumnNameOrAlias_columnWithAlias_falseIfNoMatch() {

        // ARRANGE
        ColumnName testColumn = COLUMN_WITH_ALIAS;
        String testValue = "NO_MATCH";

        // ACT
        boolean result = testColumn.equalsColumnNameOrAlias(testValue);

        // ASSERT
        assertFalse(
                result,
                String.format("Expected column %s to not match value: '%s'", testColumn.name(), testValue));
    }

    @Test
    public void equalsColumnNameOrAlias_columnWithoutAlias_trueIfNameMatch() {
        assertNameOrAliasMatch(COLUMN_WITHOUT_ALIAS, false);
    }

    @Test
    public void equalsColumnNameOrAlias_columnWithoutAlias_falseIfNoMatch() {

        // ARRANGE
        ColumnName testColumn = COLUMN_WITHOUT_ALIAS;
        String testValue = "NO_MATCH";

        // ACT
        boolean result = testColumn.equalsColumnNameOrAlias(testValue);

        // ASSERT
        assertFalse(
                result,
                String.format("Expected column %s to not match value: '%s'", testColumn.name(), testValue));
    }

    private void assertNameOrAliasMatch(ColumnName testColumn, boolean checkAlias) {

        // ARRANGE
        String testType = checkAlias ? "alias" : "name";
        String testValue = checkAlias ? testColumn.getAliases()[0] : testColumn.toString();
        String testValueUpperCase = testValue.toUpperCase();
        String testValueLowerCase = testValue.toLowerCase();
        String testValueMixedCase = toMixedCase(testValue);

        // ACT
        boolean result = testColumn.equalsColumnNameOrAlias(testValue);
        boolean resultUpperCase = testColumn.equalsColumnNameOrAlias(testValueUpperCase);
        boolean resultLowerCase = testColumn.equalsColumnNameOrAlias(testValueLowerCase);
        boolean resultMixedCase = testColumn.equalsColumnNameOrAlias(testValueMixedCase);

        // ASSERT
        assertAll(
                () -> assertTrue(
                        result,
                        String.format(
                                "Expected column %s to match on %s: '%s'", testColumn.name(), testType, testValue)),
                () -> assertTrue(
                        resultUpperCase,
                        String.format(
                                "Expected column %s to match on %s: '%s'", testColumn.name(), testType,
                                testValueUpperCase)),
                () -> assertTrue(
                        resultLowerCase,
                        String.format(
                                "Expected column %s to match on %s: '%s'", testColumn.name(), testType,
                                testValueLowerCase)),
                () -> assertTrue(
                        resultMixedCase,
                        String.format(
                                "Expected column %s to match on %s: '%s'", testColumn.name(), testType,
                                testValueMixedCase)));
    }

    private String toMixedCase(String value) {
        char[] charArray = value.toCharArray();

        return IntStream
                .range(0, charArray.length)
                .mapToObj(i -> i % 2 == 0 ? Character.toUpperCase(charArray[i]) : Character.toLowerCase(charArray[i]))
                .map(Object::toString)
                .collect(Collectors.joining());
    }

}
