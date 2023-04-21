package uk.gov.hmcts.ccd.definitionstore.befta;

import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.befta.util.BeftaUtils;

import static uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore.VALID_CCD_TEST_DEFINITIONS_PATH;

public class DefinitionStoreTestAutomationAdapter extends DefaultTestAutomationAdapter {

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
            testDataLoader = new DataLoaderToDefinitionStore(this, VALID_CCD_TEST_DEFINITIONS_PATH) {

                @Override
                protected void createRoleAssignment(String resource, String filename) {
                    // Do not create role assignments.
                    BeftaUtils.defaultLog("Will NOT create role assignments!");
                }

            };

            BeftaUtils.defaultLog(String.format(
                "Copy valid def files generated from a JSON template to a temporary location for use in FTAs: '%s'",
                TEMPORARY_DEFINITION_FOLDER
            ));
            testDataLoader.getAllDefinitionFilesToLoadAt(VALID_CCD_TEST_DEFINITIONS_PATH, TEMPORARY_DEFINITION_FOLDER);
            BeftaUtils.defaultLog("Copy complete.\n");
        }
    }

}
