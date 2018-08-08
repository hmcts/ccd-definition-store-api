package uk.gov.hmcts.ccd.definition.store.domain.showcondition;


import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
    void testInValidShowCondition() throws InvalidShowConditionException {

        assertInvalidShowCondition("SomeField=\"Some String");
        assertInvalidShowCondition("SomeField =Some String\"");
        assertInvalidShowCondition("SomeField \"Some String\"");
        assertInvalidShowCondition(" SomeField\"Some String\" ");

    }

    @Test
    void testAndConditions() throws InvalidShowConditionException {
        ShowCondition sc = classUnderTest.parseShowCondition("field1= \"ABC AND XYZ\"  .AND. field2=\"some value\" ");

        assertThat(sc.getShowConditionExpression(), is("field1=\"ABC AND XYZ\" .AND. field2=\"some value\""));
        assertThat(sc.getFields(), hasItems("field1", "field2"));
    }

    private void assertShowCondition(ShowCondition showCondition) {
        assertEquals("SomeField=\"Some String\"", showCondition.getShowConditionExpression());
        assertEquals("SomeField", showCondition.getFields().get(0));
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
