package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum GlobalCaseRole {
    CREATOR("[CREATOR]"),
    COLLABORATOR("[COLLABORATOR]");

    private final String role;

    GlobalCaseRole(String role) {
        this.role = role;
    }

    public static List<String> all() {
        return Arrays.stream(GlobalCaseRole.values())
            .map(GlobalCaseRole::getRole)
            .collect(Collectors.toList());
    }

    public String getRole() {
        return role;
    }
}
