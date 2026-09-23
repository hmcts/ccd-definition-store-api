package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import java.util.function.Supplier;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

class IdamProfileTest extends BaseTest {

    protected IdamProfileTest(AATHelper aat) {
        super(aat);
    }

    private final Supplier<RequestSpecification> asUser = asAutoTestCaseworker();

    @Test
    @DisplayName("Should return admin web authorization for the logged-in user")
    void shouldReturnAdminWebAuthorization() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/idam/adminweb/authorization")
            .then()
            .statusCode(200)
            .body("canManageUserProfile", notNullValue())
            .body("canImportDefinition", notNullValue())
            .body("canManageUserRole", notNullValue())
            .body("canManageDefinition", notNullValue())
            .body("canManageWelshTranslation", notNullValue())
            .body("canLoadWelshTranslation", notNullValue());
    }

    @Test
    @DisplayName("Should return the IDAM profile for the logged-in user")
    void shouldReturnIdamProfile() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/idam/profile")
            .then()
            .statusCode(200)
            .body("email", equalTo(aat.getCaseworkerAutoTestEmail()))
            .body("id", notNullValue())
            .body("roles", not(empty()));
    }

    @Test
    @DisplayName("Should return the IDAM profile roles for the logged-in user")
    void shouldReturnIdamProfileRoles() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/idam/profile/roles")
            .then()
            .statusCode(200)
            .body("email", equalTo(aat.getCaseworkerAutoTestEmail()))
            .body("id", notNullValue())
            .body("roles", not(empty()));
    }
}
