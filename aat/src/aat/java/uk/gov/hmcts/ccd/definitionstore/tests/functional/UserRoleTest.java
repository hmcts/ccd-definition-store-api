package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import java.util.Base64;
import java.util.UUID;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.equalTo;

class UserRoleTest extends BaseTest {

    protected UserRoleTest(AATHelper aat) {
        super(aat);
    }

    Supplier<RequestSpecification> asUser = asAutoTestCaseworker();

    private static final String EXISTING_ROLE = "caseworker-autotest1";

    @Test
    @DisplayName("Should retrieve an existing user role")
    void shouldRetrieveUserRole() {
        String encodedRole = Base64.getEncoder().encodeToString(EXISTING_ROLE.getBytes());

        asUser.get()
            .given()
            .queryParam("role", encodedRole)
            .when()
            .get("/api/user-role")
            .then()
            .statusCode(200)
            .body("role", equalTo(EXISTING_ROLE))
            .body("security_classification", equalTo("PUBLIC"));
    }

    @Test
    @DisplayName("Should return not found for an unknown user role")
    void shouldRejectUnknownUserRole() {
        String encodedRole = Base64.getEncoder().encodeToString("functional-role-does-not-exist".getBytes());

        asUser.get()
            .given()
            .queryParam("role", encodedRole)
            .when()
            .get("/api/user-role")
            .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should create a new user role")
    void shouldCreateUserRole() {
        String role = " functional-post-role-" + UUID.randomUUID() + " ";
        String trimmedRole = role.trim();

        boolean created = false;
        try {
            Response response = asUser.get()
                .given()
                .contentType(ContentType.JSON)
                .body(userRoleBody(role))
                .when()
                .post("/api/user-role");

            created = response.statusCode() == 201;
            response.then()
                .statusCode(201)
                .body("role", equalTo(trimmedRole))
                .body("security_classification", equalTo("PUBLIC"));
        } finally {
            if (created) {
                cleanupUserRole(trimmedRole);
            }
        }
    }

    @Test
    @DisplayName("Should create and then update an unknown user role with PUT")
    void shouldCreateAndUpdateUnknownUserRole() {
        String role = "functional-put-role-" + UUID.randomUUID();

        boolean created = false;
        try {
            Response response = asUser.get()
                .given()
                .contentType(ContentType.JSON)
                .body(userRoleBody(role))
                .when()
                .put("/api/user-role");

            created = response.statusCode() == 201;
            response.then()
                .statusCode(201)
                .body("role", equalTo(role))
                .body("security_classification", equalTo("PUBLIC"));

            asUser.get()
                .given()
                .contentType(ContentType.JSON)
                .body(userRoleBody(role, "PRIVATE"))
                .when()
                .put("/api/user-role")
                .then()
                .statusCode(205);

            getUserRole(role)
                .then()
                .statusCode(200)
                .body("role", equalTo(role))
                .body("security_classification", equalTo("PRIVATE"));
        } finally {
            if (created) {
                cleanupUserRole(role);
            }
        }
    }

    @Test
    @DisplayName("Should reject creating a duplicate user role")
    void shouldRejectDuplicateUserRole() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body(userRoleBody(EXISTING_ROLE))
            .when()
            .post("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject creating a user role without a security classification")
    void shouldRejectUserRoleWithoutSecurityClassification() {
        String role = "functional-invalid-role-" + UUID.randomUUID();

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"role\": \"" + role + "\"}")
            .when()
            .post("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject creating a user role without a role")
    void shouldRejectUserRoleWithoutRole() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"security_classification\": \"PUBLIC\"}")
            .when()
            .post("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject an invalid security classification")
    void shouldRejectInvalidSecurityClassification() {
        String role = "functional-invalid-classification-" + UUID.randomUUID();

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"role\": \"" + role + "\","
                + " \"security_classification\": \"INVALID\"}")
            .when()
            .post("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject updating a user role with an invalid security classification")
    void shouldRejectInvalidSecurityClassificationOnUpdate() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body(userRoleBody(EXISTING_ROLE, "INVALID"))
            .when()
            .put("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject updating a user role without a role")
    void shouldRejectUpdateWithoutRole() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"security_classification\": \"PUBLIC\"}")
            .when()
            .put("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject malformed JSON when creating a user role")
    void shouldRejectMalformedJsonWhenCreatingUserRole() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"role\": \"functional-malformed-json\"")
            .when()
            .post("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject an empty request body when creating a user role")
    void shouldRejectEmptyUserRoleRequestBody() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .when()
            .post("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject a non-JSON user role request")
    void shouldRejectUnsupportedUserRoleContentType() {
        asUser.get()
            .given()
            .contentType(ContentType.TEXT)
            .body(userRoleBody("functional-unsupported-content-type"))
            .when()
            .post("/api/user-role")
            .then()
            .statusCode(415);
    }

    @Test
    @DisplayName("Should reject an invalid live-from date when creating a user role")
    void shouldRejectInvalidLiveFromDate() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"role\": \"functional-invalid-live-from-" + UUID.randomUUID()
                + "\", \"security_classification\": \"PUBLIC\", \"live_from\": \"2026/01/01\"}")
            .when()
            .post("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject an invalid live-to date when creating a user role")
    void shouldRejectInvalidLiveToDate() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"role\": \"functional-invalid-live-to-" + UUID.randomUUID()
                + "\", \"security_classification\": \"PUBLIC\", \"live_to\": \"2026/12/31\"}")
            .when()
            .post("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject a blank user role")
    void shouldRejectBlankUserRole() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"role\": \"   \", \"security_classification\": \"PUBLIC\"}")
            .when()
            .post("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject an invalid Base64 role query parameter")
    void shouldRejectInvalidBase64Role() {
        asUser.get()
            .given()
            .queryParam("role", "%%%not-base64%%%")
            .when()
            .get("/api/user-role")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject a missing role query parameter")
    void shouldRejectMissingRoleQueryParameter() {
        asUser.get()
            .when()
            .get("/api/user-role")
            .then()
            .statusCode(400);
    }

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
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject unauthenticated GET user role requests")
    void shouldRejectUnauthenticatedGetUserRole() {
        String encodedRole = Base64.getEncoder().encodeToString(EXISTING_ROLE.getBytes());

        RestAssured.given()
            .queryParam("role", encodedRole)
            .when()
            .get("/api/user-role")
            .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Should reject unauthenticated POST user role requests")
    void shouldRejectUnauthenticatedPostUserRole() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(userRoleBody("functional-unauthenticated-role"))
            .when()
            .post("/api/user-role")
            .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Should reject unauthenticated PUT user role requests")
    void shouldRejectUnauthenticatedPutUserRole() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(userRoleBody(EXISTING_ROLE))
            .when()
            .put("/api/user-role")
            .then()
            .statusCode(401);
    }

    private Response getUserRole(String role) {
        String encodedRole = Base64.getEncoder().encodeToString(role.getBytes());
        return asUser.get()
            .given()
            .queryParam("role", encodedRole)
            .when()
            .get("/api/user-role");
    }

    private void cleanupUserRole(String role) {
        RestAssured.given()
            .queryParam("role", role)
            .when()
            .delete("/api/testing-support/cleanup-user-role")
            .then()
            .statusCode(200);
    }

    private String userRoleBody(String role) {
        return userRoleBody(role, "PUBLIC");
    }

    private String userRoleBody(String role, String securityClassification) {
        return String.format("{\"role\": \"%s\", \"security_classification\": \"%s\"}",
            role, securityClassification);
    }

}
