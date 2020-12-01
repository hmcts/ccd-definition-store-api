package uk.gov.hmcts.ccd.definitionstore.befta;

import uk.gov.hmcts.befta.BeftaMain;
import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultBeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.TestDataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

public class DefinitionStoreTestAutomationAdapter extends DefaultTestAutomationAdapter {

    public static final String DEFAULT_DEFINITIONS_PATH = "uk/gov/hmcts/befta/dse/ccd/definitions/valid";

    private TestDataLoaderToDefinitionStore loader = new TestDataLoaderToDefinitionStore(this, DEFAULT_DEFINITIONS_PATH,
        BeftaMain.getConfig().getTestUrl());

    @Override
    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        if (key.toString().startsWith("no_dynamic_injection_")) {
            return key.toString().replace("no_dynamic_injection_","");
        }
        return super.calculateCustomValue(scenarioContext, key);
    }


    @Override
    protected BeftaTestDataLoader buildTestDataLoader() {
        return new DefaultBeftaTestDataLoader() {
            @Override
            public void doLoadTestData() {
                DefinitionStoreTestAutomationAdapter.this.loader.addCcdRoles();
                DefinitionStoreTestAutomationAdapter.this.loader.importDefinitions();
            }
        };
    }

}
