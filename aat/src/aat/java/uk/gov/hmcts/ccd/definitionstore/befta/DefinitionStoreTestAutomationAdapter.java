package uk.gov.hmcts.ccd.definitionstore.befta;

import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.exception.FunctionalTestException;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.befta.util.BeftaUtils;
import uk.gov.hmcts.befta.util.ReflectionUtils;

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
        } else if (key.toString().startsWith("approximately ")) {
            try {
                String actualSizeFromHeaderStr = (String) ReflectionUtils.deepGetFieldInObject(scenarioContext,
                    "testData.actualResponse.headers.Content-Length");
                String expectedSizeStr = key.toString().replace("approximately ", "");

                int actualSize =  Integer.parseInt(actualSizeFromHeaderStr);
                int expectedSize = Integer.parseInt(expectedSizeStr);

                if (Math.abs(actualSize - expectedSize) < (actualSize * 10 / 100)) {
                    return actualSizeFromHeaderStr;
                }
                return expectedSize;
            } catch (Exception e) {
                throw new FunctionalTestException("Problem checking acceptable response payload: ", e);
            }
        } else if (key.toString().startsWith("contains ")) {
            try {
                String actualValueStr = (String) ReflectionUtils.deepGetFieldInObject(scenarioContext,
                    "testData.actualResponse.body.__plainTextValue__");
                String expectedValueStr = key.toString().replace("contains ", "");

                if (actualValueStr.contains(expectedValueStr)) {
                    return actualValueStr;
                }
                return "expectedValueStr " + expectedValueStr + " not present in response ";
            } catch (Exception e) {
                throw new FunctionalTestException("Problem checking acceptable response payload: ", e);
            }
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
