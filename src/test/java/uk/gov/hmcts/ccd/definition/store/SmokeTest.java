package uk.gov.hmcts.ccd.definition.store;

//import org.junit.Assert;
//import org.junit.Test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Dummy smoke test class (for use till proper smoke tests exist). Required to pass the "Smoke Test" stage of the build
 * pipeline in Jenkins.
 */
public class SmokeTest {

    @Test
    public void alwaysGreen() {
        Assertions.assertTrue(true);
    }

}
