package uk.gov.hmcts.ccd.definitionstore.befta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.util.BeftaUtils;

import java.util.Locale;

public class HighLevelDataSetupApp extends DataLoaderToDefinitionStore {

    private static final Logger LOG = LoggerFactory.getLogger(HighLevelDataSetupApp.class);

    public HighLevelDataSetupApp(CcdEnvironment dataSetupEnvironment) {
        super(dataSetupEnvironment);
    }

    public static void main(String[] args) throws Throwable {
        try {
            if (!args[0].toLowerCase(Locale.ENGLISH).equals("prod")) {
                main(HighLevelDataSetupApp.class, args);
            }
        }
        catch (Exception e) {
            LOG.info("JCDEBUG: Exception in HighLevelDataSetupApp.main: {} , {}", e.getMessage(), e.toString());
        }
    }

    @Override
    protected boolean shouldTolerateDataSetupFailure() {
        return true;
    }

    @Override
    public void createRoleAssignments() {
        // Do not create role assignments.
        BeftaUtils.defaultLog("Will NOT create role assignments!");
    }
}
