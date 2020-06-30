package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnumUtilTest {


    @Test
    public void testEnumValueExists() {

        assertEquals(SecurityClassification.PRIVATE,
            EnumUtil.getEnumFromString(SecurityClassification.class, "Private")
        );

    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnumValueDoesNotExistThrowsIllegalArgumentException() {

        try {
            EnumUtil.getEnumFromString(SecurityClassification.class, "XXXX");
        } catch (IllegalArgumentException e) {
            assertEquals("No enum constant uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.XXXX",
                e.getMessage()
            );
            throw e;
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnumConstantBlankThrowsIllegalArgumentException() {

        try {
            EnumUtil.getEnumFromString(SecurityClassification.class, "   ");
        } catch (IllegalArgumentException e) {
            assertEquals("No enum constant uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.   ",
                e.getMessage()
            );
            throw e;
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnumConstantNullThrowsIllegalArgumentException() {

        try {
            EnumUtil.getEnumFromString(SecurityClassification.class, null);
        } catch (IllegalArgumentException e) {
            assertEquals("Enum constant argument cannot be null",
                e.getMessage()
            );
            throw e;
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testClassArgumentNullThrowsIllegalArgumentException() {

        try {
            EnumUtil.getEnumFromString(null, "AValue");
        } catch (IllegalArgumentException e) {
            assertEquals("Class argument cannot be null",
                e.getMessage()
            );
            throw e;
        }

    }

}
