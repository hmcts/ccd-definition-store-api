package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public abstract class UserInfoMixin {
    @JsonCreator
    public UserInfoMixin(
        @JsonProperty("sub") String sub,
        @JsonProperty("uid") String uid,
        @JsonProperty("name") String name,
        @JsonProperty("given_name") String givenName,
        @JsonProperty("family_name") String familyName,
        @JsonProperty("roles") List<String> roles
    ) {
        // Constructor for deserialization
    }

}
