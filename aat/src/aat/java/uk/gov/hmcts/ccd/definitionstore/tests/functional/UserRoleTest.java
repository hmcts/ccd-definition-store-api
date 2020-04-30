package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import java.util.function.Supplier;

class UserRoleTest extends BaseTest {

    protected UserRoleTest(AATHelper aat) {
        super(aat);
    }

    Supplier<RequestSpecification> asUser = asAutoTestCaseworker();

    // Create a user role case is covered as part of setup in DataSetupExtension

    @Test
    @DisplayName("Should update a user profile")
    void shouldUpdateUserProfile() {
        String userProfile = "{\n"
            + "\"role\": \"caseworker-autotest1\",\n"
            + " \"security_classification\": \"PUBLIC\"\n"
            + "}";
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body(userProfile)
            .when()
            .put(
                "/api/user-role")
            .then()
            .statusCode(205);
    }

    @Test
    @DisplayName("Should not update / create a user profile")
    void shouldNotCreateOrUpdateUserProfile() {

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\n"
                + "\"role\": \"caseworker-autotest1\",\n"
                + " \"security_classificationsss\": \"PUBLIC\"\n"
                + "}")
            .when()
            .put(
                "/api/user-role")
            .then()
            .statusCode(409);
    }

}
