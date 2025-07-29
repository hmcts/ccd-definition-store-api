package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InjectedFieldTest {

    @Test
    @DisplayName("Should return false for non injected fields")
    void shouldReturnFalseForOtherFields() {
        final boolean injected = InjectedField.isInjectedField("TextField");

        assertFalse(injected);
    }

    @Test
    @DisplayName("Should return true for injected fields")
    void shouldReturnTrueForInjected() {
        final boolean injected = InjectedField.isInjectedField("[INJECTED_DATA.text]");

        assertTrue(injected);
    }


    @Test
    @DisplayName("Should return false for incorrect injected fields")
    void shouldReturnFalseForIncorrectInjected() {
        final boolean injected = InjectedField.isInjectedField("[INJECTED_DATA.text");

        assertFalse(injected);
    }
}
