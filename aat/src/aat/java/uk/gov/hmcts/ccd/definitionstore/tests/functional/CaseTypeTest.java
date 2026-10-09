package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.RestAssured;
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

class CaseTypeTest extends BaseTest {

    private static final String JURISDICTION = "AUTOTEST1";
    private static final String CASE_TYPE = "AAT";

    protected CaseTypeTest(AATHelper aat) {
        super(aat);
    }

    Supplier<RequestSpecification> asUserWithUser = asAutoTestCaseworkerWithUser();
    Supplier<RequestSpecification> asUser = asAutoTestCaseworker();

    @Test
    @DisplayName("should return case type definition")
    void shouldReturnCaseTypeDefinition() {
        asUserWithUser.get()
            .given()
            .pathParam("jid", JURISDICTION)
            .pathParam("ctid", CASE_TYPE)
            .contentType(ContentType.JSON)
            .when()
            .get(
                "/api/data/caseworkers/{user}/jurisdictions/{jid}/case-types/{ctid}")
            .then()
            .statusCode(200)
            .rootPath("case_fields")
            .assertThat()
            .body("findAll{case_fields->case_fields.label == \"A `Complex` field\"}[0].complexACLs[0]",
                not(empty()))
            .body("findAll{case_fields->case_fields.label == \"A `Collection` of `Text` fields\"}[0].complexACLs",
                empty())
            .body("findAll{case_fields->case_fields.label == \"A `AddressUK` field\"}[0].complexACLs",
                empty());
    }

    @Test
    @DisplayName("Should reject unauthenticated case role requests")
    void shouldRejectUnauthenticatedCaseRoleRequests() {
        RestAssured.given()
            .pathParam("uid", "caseworker")
            .pathParam("jid", JURISDICTION)
            .pathParam("ctid", CASE_TYPE)
            .when()
            .get("/api/data/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/roles")
            .then()
            .statusCode(401);

        RestAssured.given()
            .pathParam("uid", "caseworker")
            .pathParam("jid", JURISDICTION)
            .pathParam("ctid", CASE_TYPE)
            .when()
            .get("/api/data/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/access/profile/roles")
            .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Should reject requests for an unknown case type")
    void shouldRejectUnknownCaseTypeRoleRequests() {
        asUser.get()
            .given()
            .pathParam("uid", "caseworker-autotest1")
            .pathParam("jid", JURISDICTION)
            .pathParam("ctid", "case-type-does-not-exist")
            .when()
            .get("/api/data/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/roles")
            .then()
            .statusCode(404)
            .body("message", equalTo("Object Not Found for:case-type-does-not-exist"));

        asUser.get()
            .given()
            .pathParam("uid", "caseworker-autotest1")
            .pathParam("jid", JURISDICTION)
            .pathParam("ctid", "case-type-does-not-exist")
            .when()
            .get("/api/data/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/access/profile/roles")
            .then()
            .statusCode(404)
            .body("message", equalTo("Object Not Found for:case-type-does-not-exist"));
    }

    //Removed @Test annotation to temporarily remove test
    @DisplayName("Should return case types as a list with optional jurisdiction filter")
    void shouldReturnCaseTypesWithJurisdictionFilter() {

        asUser.get()
            .given()
            .pathParam("jid", JURISDICTION)
            .contentType(ContentType.JSON)
            .when()
            .get(
                "/api/data/jurisdictions/{jid}/case-type")
            .then()
            .statusCode(200);
    }

}
