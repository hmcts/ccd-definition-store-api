package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import java.util.function.Supplier;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class DisplayApiTest extends BaseTest {

    private static final String JURISDICTION = "AUTOTEST1";
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

    @Test
    @DisplayName("Should return banners for a list of jurisdictions")
    void shouldReturnBanners() {

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .queryParam("ids", JURISDICTION)
            .when()
            .get("/api/display/banners")
            .then()
            .statusCode(200)
            .body("banners", notNullValue());
    }

    @Test
    @DisplayName("Should return UI configs for a list of jurisdictions")
    void shouldReturnJurisdictionUiConfigs() {

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .queryParam("ids", JURISDICTION)
            .when()
            .get("/api/display/jurisdiction-ui-configs")
            .then()
            .statusCode(200)
            .body("configs", notNullValue());
    }

    @Test
    @DisplayName("Should return search cases result fields for a given Case Type")
    void shouldReturnSearchCasesResultFields() {

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .pathParam("ctid", CASE_TYPE)
            .when()
            .get("/api/display/search-cases-result-fields/{ctid}")
            .then()
            .statusCode(200)
            .body("case_type_id", equalTo(CASE_TYPE))
            .body("fields", notNullValue());
    }

}
