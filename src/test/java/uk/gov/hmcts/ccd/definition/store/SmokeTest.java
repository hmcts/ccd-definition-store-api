package uk.gov.hmcts.ccd.definition.store;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Dummy smoke test class (for use till proper smoke tests exist). Required to pass the "Smoke Test" stage of the build
 * pipeline in Jenkins.
 */
class SmokeTest {

    @Tag("smoke")
    @Test
    void alwaysGreen() { 
        assertTrue(true);
    }
}
