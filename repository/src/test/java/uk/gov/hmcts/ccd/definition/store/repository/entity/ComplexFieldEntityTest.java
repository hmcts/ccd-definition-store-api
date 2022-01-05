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

class ComplexFieldEntityTest {

    @Test
    void shouldEntityUniqueInSetWhenIdIsNull() {
        ComplexFieldEntity complexFieldEntity1 = new ComplexFieldEntity();
        assertNull(complexFieldEntity1.getId());
        ComplexFieldEntity complexFieldEntity2 = new ComplexFieldEntity();
        assertNull(complexFieldEntity1.getId());
        assertNotEquals(complexFieldEntity1.getOid(), complexFieldEntity2.getOid());
        Set<ComplexFieldEntity> complexFieldEntitySet =
            new HashSet<>(Arrays.asList(complexFieldEntity1,complexFieldEntity2));
        assertEquals(2, complexFieldEntitySet.size());
    }

    @Test
    void shouldEntityUniqueInSetWhenObjectClonedAndIdIsNull() {
        ComplexFieldEntity complexFieldEntity1 = new ComplexFieldEntity();
        ComplexFieldEntity complexFieldEntityCloned = SerializationUtils.clone(complexFieldEntity1);
        assertEquals(complexFieldEntity1.getId(), complexFieldEntityCloned.getId());
        assertEquals(complexFieldEntity1.getOid(), complexFieldEntityCloned.getOid());
        Set<ComplexFieldEntity> complexFieldEntitySet =
            new HashSet<>(Arrays.asList(complexFieldEntity1,complexFieldEntityCloned));
        assertEquals(1, complexFieldEntitySet.size());
    }

    @Test
    void shouldEntityUniqueInSetWhenIdIsNotNull() {
        ComplexFieldEntity complexFieldEntity1 = new ComplexFieldEntity();
        complexFieldEntity1.setId(1);
        ComplexFieldEntity complexFieldEntity2 = new ComplexFieldEntity();
        complexFieldEntity2.setId(2);
        assertNotEquals(complexFieldEntity1.getOid(), complexFieldEntity2.getOid());
        Set<ComplexFieldEntity> complexFieldEntitySet =
            new HashSet<>(Arrays.asList(complexFieldEntity1, complexFieldEntity2));
        assertEquals(2, complexFieldEntitySet.size());
    }

    @Test
    void shouldEntityUniqueInSetWhenSameIdExists() {
        ComplexFieldEntity complexFieldEntity1 = new ComplexFieldEntity();
        complexFieldEntity1.setId(1);
        ComplexFieldEntity complexFieldEntity2 = new ComplexFieldEntity();
        complexFieldEntity2.setId(1);
        assertNotEquals(complexFieldEntity1.getOid(), complexFieldEntity2.getOid());
        Set<ComplexFieldEntity> complexFieldEntitySet =
            new HashSet<>(Arrays.asList(complexFieldEntity1,complexFieldEntity2));
        assertEquals(1, complexFieldEntitySet.size());
    }

    @Test
    void shouldEntityRemovedFromSetSuccessfully() {
        ComplexFieldEntity complexFieldEntity1 = new ComplexFieldEntity();
        complexFieldEntity1.setId(1);
        ComplexFieldEntity complexFieldEntity2 = new ComplexFieldEntity();
        complexFieldEntity2.setId(2);
        ComplexFieldEntity complexFieldEntity3 = new ComplexFieldEntity();
        complexFieldEntity2.setId(3);
        ComplexFieldEntity complexFieldEntityCloned = SerializationUtils.clone(complexFieldEntity1);
        Set<ComplexFieldEntity> complexFieldEntitySet = new HashSet<>(Arrays
            .asList(complexFieldEntity1,complexFieldEntity2, complexFieldEntity3, complexFieldEntityCloned));
        assertEquals(3, complexFieldEntitySet.size());
        assertTrue(complexFieldEntitySet.contains(complexFieldEntity2));
        complexFieldEntitySet.remove(complexFieldEntity2);
        assertEquals(2, complexFieldEntitySet.size());
        assertFalse(complexFieldEntitySet.contains(complexFieldEntity2));
    }
}
