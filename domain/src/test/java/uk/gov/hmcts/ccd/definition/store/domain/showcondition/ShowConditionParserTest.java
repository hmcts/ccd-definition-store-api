package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ShowConditionParserTest {

    private final ShowConditionParser classUnderTest = new ShowConditionParser();

    @Test
    public void testValidShowCondition() throws InvalidShowConditionException {

        assertShowCondition(classUnderTest.parseShowCondition("SomeField=\"Some String\""));
        assertShowCondition(classUnderTest.parseShowCondition("SomeField =\"Some String\""));
        assertShowCondition(classUnderTest.parseShowCondition("SomeField= \"Some String\""));
        assertShowCondition(classUnderTest.parseShowCondition(" SomeField=\"Some String\" "));
        assertShowCondition(classUnderTest.parseShowCondition("  SomeField = \"Some String\" "));

    }

    @Test
    public void testInValidShowCondition() throws InvalidShowConditionException {

        assertInvalidShowCondition("SomeField=\"Some String");
        assertInvalidShowCondition("SomeField =Some String\"");
        assertInvalidShowCondition("SomeField \"Some String\"");
        assertInvalidShowCondition(" SomeField\"Some String\" ");

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
