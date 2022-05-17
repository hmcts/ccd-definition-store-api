package uk.gov.hmcts.ccd.definition.store.rest.model;

import uk.gov.hmcts.ccd.definition.store.rest.configuration.AdminWebAuthorizationProperties;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class AdminWebAuthorization {

    private boolean canManageUserProfile;

    private boolean canImportDefinition;

    private boolean canManageUserRole;

    private boolean canManageDefinition;

    private boolean canManageWelshTranslation;

    private boolean canRetrieveWelshTranslation;

    public boolean getCanManageUserProfile() {
        return canManageUserProfile;
    }

    public void setCanManageUserProfile(final boolean canManageUserProfile) {
        this.canManageUserProfile = canManageUserProfile;
    }

    public boolean getCanImportDefinition() {
        return canImportDefinition;
    }

    public void setCanImportDefinition(final boolean canImportDefinition) {
        this.canImportDefinition = canImportDefinition;
    }

    public boolean getCanManageUserRole() {
        return canManageUserRole;
    }

    public void setCanManageUserRole(final boolean canManageUserRole) {
        this.canManageUserRole = canManageUserRole;
    }

    public boolean getCanManageDefinition() {
        return canManageDefinition;
    }

    public void setCanManageDefinition(final boolean canManageDefinition) {
        this.canManageDefinition = canManageDefinition;
    }

    public boolean getCanManageWelshTranslation() {
        return canManageWelshTranslation;
    }

    public void setCanManageWelshTranslation(final boolean canManageWelshTranslation) {
        this.canManageWelshTranslation = canManageWelshTranslation;
    }

    public boolean getCanRetrieveWelshTranslation() {
        return canRetrieveWelshTranslation;
    }

    public void setCanRetrieveWelshTranslation(final boolean canRetrieveWelshTranslation) {
        this.canRetrieveWelshTranslation = canRetrieveWelshTranslation;
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

            if (adminWebAuthorizationProperties.isEnabled()) {
                final List<String> idamRoles = asList(idamProperties.getRoles());
                adminWebAuthorization.setCanManageUserProfile(evaluateRole(
                    adminWebAuthorizationProperties.getManageUserProfile(), idamRoles));
                adminWebAuthorization.setCanImportDefinition(evaluateRole(
                    adminWebAuthorizationProperties.getImportDefinition(), idamRoles));
                adminWebAuthorization.setCanManageDefinition(evaluateRole(
                    adminWebAuthorizationProperties.getManageDefinition(), idamRoles));
                adminWebAuthorization.setCanManageUserRole(evaluateRole(
                    adminWebAuthorizationProperties.getManageUserRole(), idamRoles));
                adminWebAuthorization.setCanManageWelshTranslation(evaluateRole(
                    adminWebAuthorizationProperties.getManageWelshTranslation(), idamRoles));
                adminWebAuthorization.setCanRetrieveWelshTranslation(evaluateRole(
                    adminWebAuthorizationProperties.getRetrieveWelshTranslation(), idamRoles));
                return adminWebAuthorization;
            } else {
                adminWebAuthorization.setCanImportDefinition(true);
                adminWebAuthorization.setCanManageUserProfile(true);
                adminWebAuthorization.setCanManageUserRole(true);
                adminWebAuthorization.setCanManageDefinition(true);
                adminWebAuthorization.setCanRetrieveWelshTranslation(true);
                adminWebAuthorization.setCanManageWelshTranslation(true);
            }

            return adminWebAuthorization;
        }

        private boolean evaluateRole(final List<String> adminRoles, final List<String> idamRoles) {
            return intersectSets(new HashSet<>(adminRoles), idamRoles);
        }

        private boolean intersectSets(Set<String> s1, Collection<String> s2) {
            s1.retainAll(s2);
            return s1.size() > 0;
        }
    }
}
