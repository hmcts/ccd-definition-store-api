package uk.gov.hmcts.ccd.definitionstore.befta;

import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;

public class HighLevelDataSetupApp extends DataLoaderToDefinitionStore {

    public HighLevelDataSetupApp(CcdEnvironment dataSetupEnvironment) {
        super(dataSetupEnvironment);
    }

    public static void main(String[] args) throws Throwable {
        main(HighLevelDataSetupApp.class, args);
    }

}
