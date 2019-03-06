package uk.gov.hmcts.ccd.definition.store.accessmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("accessmanagement")
public class AccessManagementExportProperties {

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
