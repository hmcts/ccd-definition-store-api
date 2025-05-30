package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnumUtilTest {

    @Test
    void testEnumValueExists() {
        assertEquals(SecurityClassification.PRIVATE,
                EnumUtil.getEnumFromString(SecurityClassification.class, "Private"));

    }

    @Test
    void testEnumValueDoesNotExistThrowsIllegalArgumentException() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            EnumUtil.getEnumFromString(SecurityClassification.class, "XXXX");
        });
        assertEquals("No enum constant uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.XXXX",
                e.getMessage());

    }

    @Test
    void testEnumConstantBlankThrowsIllegalArgumentException() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            EnumUtil.getEnumFromString(SecurityClassification.class, "   ");
        });
        assertEquals("No enum constant uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.   ",
                e.getMessage());

    }

    @Test
    void testEnumConstantNullThrowsIllegalArgumentException() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            EnumUtil.getEnumFromString(SecurityClassification.class, null);
        });
        assertEquals("Enum constant argument cannot be null",
                e.getMessage());

    }

    @Test
    void testClassArgumentNullThrowsIllegalArgumentException() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            EnumUtil.getEnumFromString(null, "AValue");
        });
        assertEquals("Class argument cannot be null",
                e.getMessage());
    }

}
