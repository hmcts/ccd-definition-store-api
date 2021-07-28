package uk.gov.hmcts.ccd.definitionstore.befta;

import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

public class DefinitionStoreTestAutomationAdapter extends DefaultTestAutomationAdapter {

    public static final String DEFAULT_DEFINITIONS_PATH = "uk/gov/hmcts/ccd/test_definitions/valid";

    @Override
    protected BeftaTestDataLoader buildTestDataLoader() {
        return new DataLoaderToDefinitionStore(this,
            DataLoaderToDefinitionStore.VALID_CCD_TEST_DEFINITIONS_PATH);
    }

    @Override
    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        if (key.toString().startsWith("no_dynamic_injection_")) {
            return key.toString().replace("no_dynamic_injection_","");
        }
        return super.calculateCustomValue(scenarioContext, key);
    }
}
