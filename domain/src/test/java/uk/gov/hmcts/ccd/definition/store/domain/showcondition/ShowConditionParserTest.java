package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShowConditionParserTest {

    private final ShowConditionParser classUnderTest = new ShowConditionParser();

    @Test
    void testValidShowCondition() throws InvalidShowConditionException {

        assertShowCondition(classUnderTest.parseShowCondition("SomeField=\"Some String\""));
        assertShowCondition(classUnderTest.parseShowCondition("SomeField =\"Some String\""));
        assertShowCondition(classUnderTest.parseShowCondition("SomeField= \"Some String\""));
        assertShowCondition(classUnderTest.parseShowCondition(" SomeField=\"Some String\" "));
        assertShowCondition(classUnderTest.parseShowCondition("  SomeField = \"Some String\" "));
    }

    @Test
    void shouldParseComplexFieldCorrectly() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1.subType1.subType2= \"ABC AND XYZ\"  AND field2=\"some value\" ");

        assertThat(sc.getShowConditionExpression(),
            is("field1.subType1.subType2=\"ABC AND XYZ\" AND field2=\"some value\""));
        assertThat(sc.getFields(), hasItems("field1", "field2"));
        assertThat(sc.getFieldsWithSubtypes(), hasItems("field1.subType1.subType2"));
    }

    @Test
    void testInvalidShowConditionWithSubtypes() {
        assertInvalidShowCondition("aaa..bbb=\"some-value\"");
        assertInvalidShowCondition("aaa. .bbb=\"some-value\"");
        assertInvalidShowCondition("aaa. x.bbb=\"some-value\"");
        assertInvalidShowCondition("aaa.x .bbb=\"some-value\"");
        assertInvalidShowCondition("aaa.Some Field.bbb=\"some-value\"");
    }

    @Test
    void testInvalidShowCondition() {
        assertInvalidShowCondition("");
        assertInvalidShowCondition(" ");
        assertInvalidShowCondition("Some Field =\"Some String\"");
        assertInvalidShowCondition("SomeField=\"Some String");
        assertInvalidShowCondition("SomeField =Some String\"");
        assertInvalidShowCondition("SomeField \"Some String\"");
        assertInvalidShowCondition(" SomeField\"Some String\" ");
    }

    @Test
    void shouldThrowExceptionWhenShowConditionIsIncomplete() {
        assertThrows(InvalidShowConditionException.class, () -> classUnderTest
            .parseShowCondition("SomeField"));
    }

    @Test
    void shouldThrowExceptionWhenShowConditionIsEmpty() {
        assertThrows(InvalidShowConditionException.class, () -> classUnderTest
            .parseShowCondition(""));
    }

    @Test
    void shouldParseAndConditionsCorrectly() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "fieldA= \"ABC AND XYZ\"  AND fieldB=\"some value\" ");

        assertThat(sc.getShowConditionExpression(), is("fieldA=\"ABC AND XYZ\" AND fieldB=\"some value\""));
        assertThat(sc.getFields(), hasItems("fieldA", "fieldB"));
    }

    @Test
    void shouldParseAndConditionsCorrectlyWithBothAndOr() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "fieldOne= \"ABC AND XYZ\" AND fieldTwo=\"some value\" OR fieldThree=\"some value\"");

        assertThat(sc.getShowConditionExpression(),
            is("fieldOne=\"ABC AND XYZ\" AND fieldTwo=\"some value\" OR fieldThree=\"some value\""));
        assertThat(sc.getFields(), hasItems("fieldOne", "fieldTwo", "fieldThree"));
    }

    @Test
    void shouldParseAndConditionsCorrectlyWithBracketsForHierachy() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1= \"ABC AND XYZ\" AND (field2=\"some value\" OR field3=\"some value\")");

        assertThat(sc.getShowConditionExpression(),
            is("field1=\"ABC AND XYZ\" AND (field2=\"some value\" OR field3=\"some value\")"));
        assertThat(sc.getFields(), hasItems("field1", "field2", "field3"));
    }

    @Test
    void shouldParseAndConditionsCorrectlyWithBracketsInStringValue() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1= \"ABC AND XYZ\" AND field2=\"(some value)\" OR field3=\"some value\"");

        assertThat(sc.getShowConditionExpression(),
            is("field1=\"ABC AND XYZ\" AND field2=\"(some value)\" OR field3=\"some value\""));
        assertThat(sc.getFields(), hasItems("field1", "field2", "field3"));
    }

    @Test
    void shouldParseAndConditionsCorrectlyWithBracketsNested() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1=\"ABC AND XYZ\" AND ((field2=\"(some value)\" "
                + "OR field3=\"some value\") OR field4=\"some value\")");

        assertThat(sc.getShowConditionExpression(),
            is("field1=\"ABC AND XYZ\" AND ((field2=\"(some value)\" "
                + "OR field3=\"some value\") OR field4=\"some value\")"));
        assertThat(sc.getFields(), hasItems("field1", "field2", "field3", "field4"));
    }

    @Test
    void shouldParseAndConditionsCorrectlyWithBracketsNestedDoubleClose() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "(YesNoField=\"No\" AND (TextField=\"no\" OR TextAreaField=\"no\")) OR NumberField=\"1\"");

        assertThat(sc.getShowConditionExpression(),
            is("(YesNoField=\"No\" AND (TextField=\"no\" OR TextAreaField=\"no\")) OR NumberField=\"1\""));
        assertThat(sc.getFields(), hasItems("YesNoField", "TextField", "TextAreaField", "NumberField"));
    }

    @Test
    void shouldParseAndConditionsCorrectlyWithBracketsMixture() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1= \"ABC AND XYZ\" AND (field2=\"(some value)\" OR field3=\"some value\")");

        assertThat(sc.getShowConditionExpression(),
            is("field1=\"ABC AND XYZ\" AND (field2=\"(some value)\" OR field3=\"some value\")"));
        assertThat(sc.getFields(), hasItems("field1", "field2", "field3"));
    }

    @Test
    void shouldThrowExceptionWithBracketsMixtureWrongLocation() {
        assertInvalidShowCondition("field1= \"ABC AND XYZ\"  AND field2(=\"(some value)\" OR field3=\"some value\")");
    }

    @Test
    void shouldThrowExceptionWithBracketsMixtureMissMatchOpeningClosing() {
        assertInvalidShowCondition("field1= \"ABC AND XYZ\"  AND ((field2=\"(some value)\" OR field3=\"some value\")");
    }


    @Test
    void shouldThrowExceptionWhenAndConditionIsIncomplete() {
        assertThrows(InvalidShowConditionException.class, () -> classUnderTest
            .parseShowCondition("field1=\"ABC\" AND aa"));
    }

    @Test
    void shouldThrowExceptionWhenAndConditionIsInvalid() {
        assertThrows(InvalidShowConditionException.class, () -> classUnderTest
            .parseShowCondition(" AND field1=\"ABC\""));
    }

    @Test
    void shouldParseContainsWithSingleValueCorrectly() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition("field1 CONTAINS \"ABCDEFG\"");

        assertThat(sc.getShowConditionExpression(), is("field1CONTAINS\"ABCDEFG\""));
        assertThat(sc.getFields(), hasItems("field1"));
    }

    @Test
    void shouldParseContainsCorrectly() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition("field1 CONTAINS \"ABC,CDE,EFG,JKL\"");

        assertThat(sc.getShowConditionExpression(), is("field1CONTAINS\"ABC,CDE,EFG,JKL\""));
        assertThat(sc.getFields(), hasItems("field1"));
    }

    @Test
    void shouldParseMultipleContainsCorrectly() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1 CONTAINS \"ABC,CDE,EFG,JKL\" AND  field2 CONTAINS \"1,3,5,7,88\"");

        assertThat(sc.getShowConditionExpression(),
            is("field1CONTAINS\"ABC,CDE,EFG,JKL\" AND field2CONTAINS\"1,3,5,7,88\""));
        assertThat(sc.getFields(), hasItems("field1", "field2"));
    }

    @Test
    void shouldParseMixedEqualsANDContainsCorrectly() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1 = \"ABCDEFG\" AND  field2 CONTAINS \"1,3,5,7,88\"");

        assertThat(sc.getShowConditionExpression(),
            is("field1=\"ABCDEFG\" AND field2CONTAINS\"1,3,5,7,88\""));
        assertThat(sc.getFields(), hasItems("field1", "field2"));
    }

    @Test
    void shouldParseComplexFieldCorrectlyWithORCondition() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1.subType1.subType2= \"ABC OR XYZ\"  OR field2=\"some value\" ");

        assertThat(sc.getShowConditionExpression(),
            is("field1.subType1.subType2=\"ABC OR XYZ\" OR field2=\"some value\""));
        assertThat(sc.getFields(), hasItems("field1", "field2"));
        assertThat(sc.getFieldsWithSubtypes(), hasItems("field1.subType1.subType2"));
    }

    @Test
    void shouldParseMultipleContainsCorrectlyWithOR() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1 CONTAINS \"ABC,CDE,EFG,JKL\" OR  field2 CONTAINS"
                + " \"1,3,5,7,88\"");

        assertThat(sc.getShowConditionExpression(),
            is("field1CONTAINS\"ABC,CDE,EFG,JKL\" OR field2CONTAINS\"1,3,5,7,"
                + "88\""));
        assertThat(sc.getFields(), hasItems("field1", "field2"));
    }

    @Test
    void shouldThrowExceptionWhenAndConditionIsInvalidWithOR() {
        assertThrows(InvalidShowConditionException.class, () -> classUnderTest
            .parseShowCondition(" OR field1=\"ABC\""));
    }

    @Test
    void shouldParseORConditionsCorrectly() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1= \"ABC OR XYZ\"  OR field2=\"some value\" ");

        assertThat(sc.getShowConditionExpression(), is("field1=\"ABC OR XYZ\" OR field2=\"some value\""));
        assertThat(sc.getFields(), hasItems("field1", "field2"));
    }

    @Test
    void shouldParseORConditionsWithNotEqualCorrectly() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1!= \"ABC OR XYZ\"  OR field2!=\"some value\" ");

        assertThat(sc.getShowConditionExpression(), is("field1!=\"ABC OR XYZ\" OR field2!=\"some value\""));
        assertThat(sc.getFields(), hasItems("field1", "field2"));
    }

    @Test
    void shouldParseMixedEqualsORContainsCorrectly() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "field1 = \"ABCDEFG\" OR  field2 CONTAINS \"1,3,5,7,88\"");

        assertThat(sc.getShowConditionExpression(), is("field1=\"ABCDEFG\" OR field2CONTAINS\"1,3,5,7,88\""));
        assertThat(sc.getFields(), hasItems("field1", "field2"));
    }

    @Test
    void shouldThrowExceptionWhenWeMixBothAndOrOperators() {
        assertThrows(InvalidShowConditionException.class, () -> classUnderTest
            .parseShowCondition("field1='AB' AND field2='BC' OR field3='CD'"));
    }

    @Test
    void shouldParseInjectedFields() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition(
            "[INJECTED_DATA.test]=\"Test\"");

        assertThat(sc.getFields(), hasItems("[INJECTED_DATA.test]"));
        assertEquals(0, sc.getFieldsWithSubtypes().size());
    }

    private void assertShowCondition(ShowCondition showCondition) {
        assertEquals("SomeField=\"Some String\"", showCondition.getShowConditionExpression());
        assertTrue(showCondition.getFields().contains("SomeField"), "SomeField");
    }

    private void assertInvalidShowCondition(String invalidShowCondition) {
        try {
            classUnderTest.parseShowCondition(invalidShowCondition);
            fail("InvalidShowConditionException should have been thrown");
        } catch (InvalidShowConditionException e) {
            assertEquals(invalidShowCondition, e.getShowCondition());
        }
    }
}
