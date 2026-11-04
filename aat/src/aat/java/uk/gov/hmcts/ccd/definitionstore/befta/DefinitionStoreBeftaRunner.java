package uk.gov.hmcts.ccd.definitionstore.befta;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import uk.gov.hmcts.befta.BeftaMain;

@RunWith(Cucumber.class)
@CucumberOptions(
    plugin = { "json:target/cucumber.json", "pretty" },
    glue = { "uk.gov.hmcts.befta.player", "uk.gov.hmcts.ccd.definitionstore.befta" },
    features = { "classpath:features" },
    tags = "(not @Ignore) or (not @groupaccess)"
)
public class DefinitionStoreBeftaRunner {

    private DefinitionStoreBeftaRunner() {
        // Hide Utility Class Constructor : Utility classes should not have a public or default constructor
        // (squid:S1118)
    }

    @BeforeClass
    public static void setUp() {
        var taAdapter = new DefinitionStoreTestAutomationAdapter();
        taAdapter.initialiseTestDataLoader();

        BeftaMain.setUp(taAdapter);
    }

    @AfterClass
    public static void tearDown() {
        BeftaMain.tearDown();
    }

}
