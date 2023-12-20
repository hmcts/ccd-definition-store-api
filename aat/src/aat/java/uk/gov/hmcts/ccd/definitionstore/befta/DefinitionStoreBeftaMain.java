package uk.gov.hmcts.ccd.definitionstore.befta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.BeftaMain;

public class DefinitionStoreBeftaMain extends BeftaMain {

    private static final Logger LOG = LoggerFactory.getLogger(DefinitionStoreBeftaMain.class);

    public static void main(String[] args) {
        try {
            var taAdapter = new DefinitionStoreTestAutomationAdapter();
            taAdapter.initialiseTestDataLoader();

            BeftaMain.main(args, taAdapter);
        } catch (Exception e) {
            LOG.info("JCDEBUG: Exception in DefinitionStoreBeftaMain.main: {} , {}", e.getMessage(), e.toString());
        }
    }

}
