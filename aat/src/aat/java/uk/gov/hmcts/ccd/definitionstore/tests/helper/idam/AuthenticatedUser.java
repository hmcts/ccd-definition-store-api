package uk.gov.hmcts.ccd.definitionstore.tests.helper.idam;

import java.util.List;

public class AuthenticatedUser {

    private final String id;
    private final String email;
    private final String accessToken;
    private final List<String> roles;

    public AuthenticatedUser(String id, String email, String accessToken, List<String> roles) {
        this.id = id;
        this.email = email;
        this.accessToken = accessToken;
        this.roles = roles;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public List<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "AuthenticatedUser{"
            + "id='" + id + '\''
            + ", email='" + email + '\''
            + ", accessToken='" + accessToken + '\''
            + ", roles=" + roles
            + '}';
    }
}
