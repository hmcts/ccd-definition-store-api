package uk.gov.hmcts.ccd.definitionstore.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.lang.BooleanUtils;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import uk.gov.hmcts.ccd.definitionstore.tests.helper.idam.AuthenticatedUser;

import java.io.File;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public class DataSetUpExtension implements BeforeAllCallback {

    static final String IMPORTED = "imported";

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!BooleanUtils.isTrue(context.getRoot().getStore(GLOBAL).get(IMPORTED, Boolean.class))) {
            System.out.println("calling import again");
            createUserRoleAndImportDefinitions();
            context.getRoot().getStore(GLOBAL).put(IMPORTED, true);
        } else {
            System.out.println("imported already");
        }
    }

    private void createUserRoleAndImportDefinitions() {
        AATHelper aat = AATHelper.INSTANCE;
        RestAssured.baseURI = aat.getTestUrl();
        RestAssured.useRelaxedHTTPSValidation();
        String s2sToken = aat.getS2SHelper().getToken();

        createTestRole(aat, s2sToken);

        ImportDefinitions(aat, s2sToken);
    }

    private void ImportDefinitions(AATHelper aat, String s2sToken) {
        AuthenticatedUser importer = aat.getIdamHelper()
            .authenticate(aat.getImporterAutoTestEmail(),
                aat.getImporterAutoTestPassword());

        RestAssured.given()
            .header("Authorization", "Bearer " + importer.getAccessToken())
            .header("ServiceAuthorization", s2sToken)
            .multiPart(new File("src/resource/CCD_CNP_27.xlsx"))
            .expect()
            .statusCode(201)
            .when()
            .post("/import");
    }

    private void createTestRole(AATHelper aat, String s2sToken) {
        AuthenticatedUser caseworker = aat.getIdamHelper()
            .authenticate(aat.getCaseworkerAutoTestEmail(),
                aat.getCaseworkerAutoTestPassword());

        RestAssured.given()
            .header("Authorization", "Bearer " + caseworker.getAccessToken())
            .header("ServiceAuthorization", s2sToken)
            .contentType(ContentType.JSON)
            .body("{\n" +
                "\"role\": \"caseworker-autotest1\",\n" +
                " \"security_classification\": \"PUBLIC\"\n" +
                "}")
            .when()
            .put("/api/user-role")
            .then()
            .statusCode(anyOf(is(201),is(205)));
    }

}
