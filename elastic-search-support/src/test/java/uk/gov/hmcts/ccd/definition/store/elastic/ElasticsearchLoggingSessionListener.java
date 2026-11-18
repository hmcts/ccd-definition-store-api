package uk.gov.hmcts.ccd.definition.store.elastic;

import org.elasticsearch.common.logging.LogConfigurator;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

public class ElasticsearchLoggingSessionListener implements LauncherSessionListener {

    @Override
    public void launcherSessionOpened(LauncherSession session) {
        // Unit tests can create Elasticsearch exceptions without a Spring context.
        LogConfigurator.configureESLogging();
    }
}
