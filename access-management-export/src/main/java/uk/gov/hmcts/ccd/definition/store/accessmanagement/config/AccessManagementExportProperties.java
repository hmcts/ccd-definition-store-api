package uk.gov.hmcts.ccd.definition.store.accessmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.sql.DataSource;

@ConfigurationProperties("accessmanagement")
public class AccessManagementExportProperties {

    private boolean enabled;
    private DataSource datasource;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DataSource getDatasource() {
        return datasource;
    }

    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }
}
