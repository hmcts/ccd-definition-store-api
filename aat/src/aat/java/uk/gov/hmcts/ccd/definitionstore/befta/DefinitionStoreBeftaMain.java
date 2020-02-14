package uk.gov.hmcts.ccd.definitionstore.befta;

import uk.gov.hmcts.befta.BeftaMain;

public class DefinitionStoreBeftaMain extends BeftaMain {

    public static void main(String[] args) {
        setTaAdapter(new DefinitionStoreTestAutomationAdapter());
        BeftaMain.main(args);
    }

}
