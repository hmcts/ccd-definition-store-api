package uk.gov.hmcts.ccd.definitionstore.tests;

import io.restassured.RestAssured;
import org.apache.commons.lang.BooleanUtils;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import uk.gov.hmcts.ccd.definitionstore.tests.helper.idam.AuthenticatedUser;

import java.io.File;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public class DataImportExtension implements BeforeAllCallback {

    static final String IMPORTED = "imported";

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!BooleanUtils.isTrue(context.getStore(GLOBAL).get(IMPORTED, Boolean.class))) {
            importDefinitions();
            context.getStore(GLOBAL).put(IMPORTED, true);
            System.out.println("calling import again");
        } else {
            System.out.println("imported already");
        }
    }

    private void importDefinitions() {
        AATHelper aat = AATHelper.INSTANCE;
        RestAssured.baseURI = aat.getTestUrl();
        RestAssured.useRelaxedHTTPSValidation();

        AuthenticatedUser caseworker = aat.getIdamHelper()
            .authenticate(aat.getImporterAutoTestEmail(),
                aat.getImporterAutoTestPassword());

        String s2sToken = aat.getS2SHelper()
            .getToken();

         RestAssured.given()
            .header("Authorization", "Bearer " + caseworker.getAccessToken())
            .header("ServiceAuthorization", s2sToken)
            .multiPart(new File("src/resource/CCD_CNP_27.xlsx"))
            .expect()
            .statusCode(201)
            .when()
            .post("/import");
    }
}
