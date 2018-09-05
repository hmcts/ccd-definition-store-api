package uk.gov.hmcts.ccd.definition.store.domain;

import org.springframework.beans.factory.annotation.Value;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class ApplicationParams {

    @Value("${ccd.user-profile.host}")
    private String userProfileHost;

    @Value("${auth.idam.client.baseUrl}")
    private String idamHost;

    public String userProfilePutURL() {
        return userProfileHost + "/user-profile/users";
    }

    public String idamUserProfileURL() {
        return idamHost + "/details";
    }
}
