package uk.gov.hmcts.ccd.definition.store.rest.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("admin-web.authorization")
public class AdminWebAuthorizationProperties {

    private boolean enabled;

    private List<String> manageUserProfile;

    private List<String> manageUserRole;

    private List<String> manageDefinition;

    private List<String> importDefinition;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getManageUserProfile() {
        return manageUserProfile;
    }

    public void setManageUserProfile(final List<String> manageUserProfile) {
        this.manageUserProfile = manageUserProfile;
    }

    public List<String> getManageUserRole() {
        return manageUserRole;
    }

    public void setManageUserRole(final List<String> manageUserRole) {
        this.manageUserRole = manageUserRole;
    }

    public List<String> getManageDefinition() {
        return manageDefinition;
    }

    public void setManageDefinition(final List<String> manageDefinition) {
        this.manageDefinition = manageDefinition;
    }

    public List<String> getImportDefinition() {
        return importDefinition;
    }

    public void setImportDefinition(final List<String> importDefinition) {
        this.importDefinition = importDefinition;
    }
}
