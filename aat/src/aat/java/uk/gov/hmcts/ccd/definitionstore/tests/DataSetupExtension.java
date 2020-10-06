package uk.gov.hmcts.ccd.definitionstore.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import uk.gov.hmcts.ccd.definitionstore.tests.helper.idam.AuthenticatedUser;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

/**
 * Custom Extension which executes setup code only once before all tests are started.
 * This is temporary solution until https://github.com/junit-team/junit5/issues/456 released
 */
public class DataSetupExtension implements BeforeAllCallback {

    static final String INITIALISED = "initialised";

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (BooleanUtils.isNotTrue(context.getRoot().getStore(GLOBAL).get(INITIALISED, Boolean.class))) {
            createUserRoleAndImportDefinitions();
            context.getRoot().getStore(GLOBAL).put(INITIALISED, true);
        }
    }

    private void createUserRoleAndImportDefinitions() {
        AATHelper aat = AATHelper.INSTANCE;
        RestAssured.baseURI = aat.getTestUrl();
        RestAssured.useRelaxedHTTPSValidation();
        String s2sToken = aat.getS2SHelper().getToken();

        createTestRole(aat, s2sToken);
    }

    private void createTestRole(AATHelper aat, String s2sToken) {
        AuthenticatedUser caseworker = aat.getIdamHelper()
            .authenticate(aat.getCaseworkerAutoTestEmail(),
                aat.getCaseworkerAutoTestPassword());

        RestAssured.given()
            .header("Authorization", "Bearer " + caseworker.getAccessToken())
            .header("ServiceAuthorization", s2sToken)
            .body("{\n"
                + "\"role\": \"caseworker-autotest1\",\n"
                + " \"security_classification\": \"PUBLIC\"\n"
                + "}")
            .contentType(ContentType.JSON)
            .when()
            .put("/api/user-role")
            .then()
            .statusCode(anyOf(is(201), is(205)));
    }

}
