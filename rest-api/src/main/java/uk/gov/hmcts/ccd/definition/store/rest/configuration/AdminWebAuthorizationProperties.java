package uk.gov.hmcts.ccd.definition.store.rest.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("admin-web.authorization")
public class AdminWebAuthorizationProperties {

    private List<String> manageUserProfile;

    public List<String> getManageUserProfile() {
        return manageUserProfile;
    }

    public void setManageUserProfile(final List<String> manageUserProfile) {
        this.manageUserProfile = manageUserProfile;
    }
}
