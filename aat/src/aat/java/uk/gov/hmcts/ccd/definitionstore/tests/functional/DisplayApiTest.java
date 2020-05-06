package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;
import java.util.function.Supplier;

class DisplayApiTest extends BaseTest {

    private static final String CASE_TYPE = "AAT";
    private static final String EVENT = "START_PROGRESS";


    protected DisplayApiTest(AATHelper aat) {
        super(aat);
    }

    Supplier<RequestSpecification> asUser = asAutoTestCaseworker();

    @Test
    @DisplayName("Should return the UI definition for the search inputs for a given Case Type")
    void shouldReturnUIDefinitionforSearchInput() {

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .pathParam("ctid", CASE_TYPE)
            .when()
            .get(
                "/api/display/search-input-definition/{ctid}")
            .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Should return the UI definition for the search result fields for a given Case Type")
    void shouldReturnUIDefinitionforSearchResultFields() {

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .pathParam("ctid", CASE_TYPE)
            .when()
            .get(
                "/api/display/search-result-definition/{ctid}")
            .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Should return Case Tab Collection for a given Case Type")
    void shouldReturnCaseTabCollection() {

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .pathParam("ctid", CASE_TYPE)
            .when()
            .get(
                "/api/display/tab-structure/{ctid}")
            .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Should return Case Wizard Page Collection for a given Case Type")
    void shouldReturnCaseWizardPageCollection() {

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .pathParam("ctid", CASE_TYPE)
            .pathParam("etid", EVENT)
            .when()
            .get(
                "/api/display/wizard-page-structure/case-types/{ctid}/event-triggers/{etid}")
            .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Should return UI definition for the work basket inputs for a given Case Type")
    void shouldReturnWorkBasketInput() {

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .pathParam("ctid", CASE_TYPE)
            .when()
            .get(
                "/api/display/work-basket-input-definition/{ctid}")
            .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Should return UI definition for the work basket for a given Case Type")
    void shouldReturnWorkBasket() {

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .pathParam("ctid", CASE_TYPE)
            .when()
            .get(
                "/api/display/work-basket-definition/{ctid}")
            .then()
            .statusCode(200);
    }

}
