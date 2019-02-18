package uk.gov.hmcts.ccd.definition.store.rest.model;

import uk.gov.hmcts.ccd.definition.store.rest.configuration.AdminWebAuthorizationProperties;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class AdminWebAuthorization implements Serializable {

    private boolean canManageUserProfile;


    public boolean getCanManageUserProfile() {
        return canManageUserProfile;
    }

    public void setCanManageUserProfile(final boolean canManageUserProfile) {
        this.canManageUserProfile = canManageUserProfile;
    }

    public static class AdminWebAuthorizationBuilder {
        private final AdminWebAuthorizationProperties adminWebAuthorizationProperties;
        private IdamProperties idamProperties;

        public AdminWebAuthorizationBuilder(AdminWebAuthorizationProperties adminWebAuthorizationProperties) {
            this.adminWebAuthorizationProperties = adminWebAuthorizationProperties;
        }

        public AdminWebAuthorizationBuilder withIdamProperties(IdamProperties idamProperties) {
            this.idamProperties = idamProperties;
            return this;
        }

        public AdminWebAuthorization build() {
            AdminWebAuthorization adminWebAuthorization = new AdminWebAuthorization();
            final List<String> idamRoles = asList(idamProperties.getRoles());
            adminWebAuthorization.setCanManageUserProfile(intersectSets(new HashSet<>(adminWebAuthorizationProperties.getManageUserProfile()), idamRoles));
            return adminWebAuthorization;
        }

        private boolean intersectSets(Set<String> s1, Collection<String> s2) {
            s1.retainAll(s2);
            return s1.size() > 0;
        }
    }
}
