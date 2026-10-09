package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.instanceOf;

class ImportAuditTest extends BaseTest {

    protected ImportAuditTest(AATHelper aat) {
        super(aat);
    }

    private final Supplier<RequestSpecification> asUser = asAutoTestCaseworker();

    @Test
    @DisplayName("Should return import audits")
    void shouldReturnImportAudits() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/import-audits")
            .then()
            .statusCode(200)
            .body("", instanceOf(List.class));
    }

    @Test
    @DisplayName("Should reject unauthenticated requests for import audits")
    void shouldRejectUnauthenticatedRequest() {
        RestAssured.given()
            .when()
            .get("/api/import-audits")
            .then()
            .statusCode(401);
    }
}
