package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import java.util.function.Supplier;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

class CaseTypeTest extends BaseTest {

    private static final String JURISDICTION = "AUTOTEST1";
    private static final String CASE_TYPE = "AAT";

    protected CaseTypeTest(AATHelper aat) {
        super(aat);
    }

    Supplier<RequestSpecification> asUserWithUser = asAutoTestCaseworkerWithUser();
    Supplier<RequestSpecification> asUser = asAutoTestCaseworker();


    @Test
    @DisplayName("Should return case type definition")
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
            .statusCode(200);
    }

    @Test
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
