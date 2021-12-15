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

class StateACLEntityTest {

    @Test
    void shouldEntityUniqueInSetWhenIdIsNull() {
        StateACLEntity stateACLEntity1 = new StateACLEntity();
        assertNull(stateACLEntity1.getId());
        StateACLEntity stateACLEntity2 = new StateACLEntity();
        assertNull(stateACLEntity1.getId());
        assertNotEquals(stateACLEntity1.getOid(), stateACLEntity2.getOid());
        Set<StateACLEntity> stateACLEntitySet = new HashSet<>(Arrays.asList(stateACLEntity1,stateACLEntity2));
        assertEquals(2, stateACLEntitySet.size());
    }

    @Test
    void shouldEntityUniqueInSetWhenObjectClonedAndIdIsNull() {
        StateACLEntity stateACLEntity1 = new StateACLEntity();
        StateACLEntity stateACLEntityCloned = SerializationUtils.clone(stateACLEntity1);
        assertEquals(stateACLEntity1.getId(), stateACLEntityCloned.getId());
        assertEquals(stateACLEntity1.getOid(), stateACLEntityCloned.getOid());
        Set<StateACLEntity> stateACLEntitySet = new HashSet<>(Arrays.asList(stateACLEntity1,stateACLEntityCloned));
        assertEquals(1, stateACLEntitySet.size());
    }

    @Test
    void shouldEntityUniqueInSetWhenIdIsNotNull() {
        StateACLEntity stateACLEntity1 = new StateACLEntity();
        stateACLEntity1.setId(1);
        StateACLEntity stateACLEntity2 = new StateACLEntity();
        stateACLEntity2.setId(2);
        assertNotEquals(stateACLEntity1.getOid(), stateACLEntity2.getOid());
        Set<StateACLEntity> stateACLEntitySet = new HashSet<>(Arrays.asList(stateACLEntity1, stateACLEntity2));
        assertEquals(2, stateACLEntitySet.size());
    }

    @Test
    void shouldEntityUniqueInSetWhenSameIdExists() {
        StateACLEntity stateACLEntity1 = new StateACLEntity();
        stateACLEntity1.setId(1);
        StateACLEntity stateACLEntity2 = new StateACLEntity();
        stateACLEntity2.setId(1);
        assertNotEquals(stateACLEntity1.getOid(), stateACLEntity2.getOid());
        Set<StateACLEntity> stateACLEntitySet = new HashSet<>(Arrays.asList(stateACLEntity1,stateACLEntity2));
        assertEquals(1, stateACLEntitySet.size());
    }

    @Test
    void shouldEntityRemovedFromSetSuccessfully() {
        StateACLEntity stateACLEntity1 = new StateACLEntity();
        stateACLEntity1.setId(1);
        StateACLEntity stateACLEntity2 = new StateACLEntity();
        stateACLEntity2.setId(2);
        StateACLEntity stateACLEntity3 = new StateACLEntity();
        stateACLEntity2.setId(3);
        StateACLEntity stateACLEntityCloned = SerializationUtils.clone(stateACLEntity1);
        Set<StateACLEntity> stateACLEntitySet =
            new HashSet<>(Arrays.asList(stateACLEntity1,stateACLEntity2, stateACLEntity3, stateACLEntityCloned));
        assertEquals(3, stateACLEntitySet.size());
        assertTrue(stateACLEntitySet.contains(stateACLEntity2));
        stateACLEntitySet.remove(stateACLEntity2);
        assertEquals(2, stateACLEntitySet.size());
        assertFalse(stateACLEntitySet.contains(stateACLEntity2));
    }

}
