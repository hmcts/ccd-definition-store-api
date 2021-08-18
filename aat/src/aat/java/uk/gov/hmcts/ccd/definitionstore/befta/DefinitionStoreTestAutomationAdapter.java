package uk.gov.hmcts.ccd.definitionstore.befta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

import static uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore.VALID_CCD_TEST_DEFINITIONS_PATH;


public class DefinitionStoreTestAutomationAdapter extends DefaultTestAutomationAdapter {

    private static final Logger log = LoggerFactory.getLogger(DefinitionStoreTestAutomationAdapter.class);

    public static final String TEMPORARY_DEFINITION_FOLDER = "build/tmp/definition_files_copy";

    private DataLoaderToDefinitionStore testDataLoader;

    @Override
    protected BeftaTestDataLoader buildTestDataLoader() {
        initialiseTestDataLoader();
        return this.testDataLoader;
    }

    @Override
    public synchronized Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        if (key.toString().startsWith("no_dynamic_injection_")) {
            return key.toString().replace("no_dynamic_injection_","");
        }
        return super.calculateCustomValue(scenarioContext, key);
    }

    public void initialiseTestDataLoader() {
        if (testDataLoader == null) {
            testDataLoader = new DataLoaderToDefinitionStore(this, VALID_CCD_TEST_DEFINITIONS_PATH);

            log.info(
                "Copy valid def files generated from a JSON template to a temporary location for use in FTAs: '{}'",
                TEMPORARY_DEFINITION_FOLDER
            );
            testDataLoader.getAllDefinitionFilesToLoadAt(VALID_CCD_TEST_DEFINITIONS_PATH, TEMPORARY_DEFINITION_FOLDER);
            log.info("Copy complete.\n");
        }
    }

}
