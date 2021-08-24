package uk.gov.hmcts.ccd.definitionstore.befta;

import uk.gov.hmcts.befta.BeftaMain;

public class DefinitionStoreBeftaMain extends BeftaMain {

    public static void main(String[] args) {

        var taAdapter = new DefinitionStoreTestAutomationAdapter();
        taAdapter.initialiseTestDataLoader();

        BeftaMain.main(args, taAdapter);
    }

}
