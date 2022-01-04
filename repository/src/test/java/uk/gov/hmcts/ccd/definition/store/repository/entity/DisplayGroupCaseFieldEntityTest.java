package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisplayGroupCaseFieldEntityTest {

    @Test
    void shouldEntityUniqueInSetWhenIdIsNull() {
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity1 = new DisplayGroupCaseFieldEntity();
        assertNull(displayGroupCaseFieldEntity1.getId());
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity2 = new DisplayGroupCaseFieldEntity();
        assertNull(displayGroupCaseFieldEntity1.getId());
        assertNotEquals(displayGroupCaseFieldEntity1.getOid(), displayGroupCaseFieldEntity2.getOid());
        Set<DisplayGroupCaseFieldEntity> displayGroupCaseFieldEntitySet =
            new HashSet<>(Arrays.asList(displayGroupCaseFieldEntity1,displayGroupCaseFieldEntity2));
        assertEquals(2, displayGroupCaseFieldEntitySet.size());
    }

    @Test
    void shouldEntityUniqueInSetWhenObjectClonedAndIdIsNull() {
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity1 = new DisplayGroupCaseFieldEntity();
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntityCloned
            = SerializationUtils.clone(displayGroupCaseFieldEntity1);
        assertEquals(displayGroupCaseFieldEntity1.getId(), displayGroupCaseFieldEntityCloned.getId());
        assertEquals(displayGroupCaseFieldEntity1.getOid(), displayGroupCaseFieldEntityCloned.getOid());
        Set<DisplayGroupCaseFieldEntity> displayGroupCaseFieldEntitySet = new HashSet<>(Arrays
            .asList(displayGroupCaseFieldEntity1,displayGroupCaseFieldEntityCloned));
        assertEquals(1, displayGroupCaseFieldEntitySet.size());
    }

    @Test
    void shouldEntityUniqueInSetWhenIdIsNotNull() {
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity1 = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity1.setId(1);
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity2 = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity2.setId(2);
        assertNotEquals(displayGroupCaseFieldEntity1.getOid(), displayGroupCaseFieldEntity2.getOid());
        Set<DisplayGroupCaseFieldEntity> displayGroupCaseFieldEntitySet =
            new HashSet<>(Arrays.asList(displayGroupCaseFieldEntity1, displayGroupCaseFieldEntity2));
        assertEquals(2, displayGroupCaseFieldEntitySet.size());
    }

    @Test
    void shouldEntityUniqueInSetWhenSameIdExists() {
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity1 = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity1.setId(1);
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity2 = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity2.setId(1);
        assertNotEquals(displayGroupCaseFieldEntity1.getOid(), displayGroupCaseFieldEntity2.getOid());
        Set<DisplayGroupCaseFieldEntity> displayGroupCaseFieldEntitySet =
            new HashSet<>(Arrays.asList(displayGroupCaseFieldEntity1,displayGroupCaseFieldEntity2));
        assertEquals(1, displayGroupCaseFieldEntitySet.size());
    }

    @Test
    void shouldEntityRemovedFromSetSuccessfully() {
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity1 = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity1.setId(1);
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity2 = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity2.setId(2);
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity3 = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity2.setId(3);
        DisplayGroupCaseFieldEntity cloned = SerializationUtils.clone(displayGroupCaseFieldEntity1);
        Set<DisplayGroupCaseFieldEntity> displayGroupCaseFieldEntitySet = new HashSet<>(Arrays
            .asList(displayGroupCaseFieldEntity1, displayGroupCaseFieldEntity2,
                displayGroupCaseFieldEntity3, cloned));
        assertEquals(3, displayGroupCaseFieldEntitySet.size());
        assertTrue(displayGroupCaseFieldEntitySet.contains(displayGroupCaseFieldEntity2));
        displayGroupCaseFieldEntitySet.remove(displayGroupCaseFieldEntity2);
        assertEquals(2, displayGroupCaseFieldEntitySet.size());
        assertFalse(displayGroupCaseFieldEntitySet.contains(displayGroupCaseFieldEntity2));
    }

}
