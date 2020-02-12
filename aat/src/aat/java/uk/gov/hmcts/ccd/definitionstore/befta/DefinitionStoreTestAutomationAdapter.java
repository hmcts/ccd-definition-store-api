package uk.gov.hmcts.ccd.definitionstore.befta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import uk.gov.hmcts.befta.*;
import uk.gov.hmcts.befta.data.UserData;
import uk.gov.hmcts.befta.exception.FunctionalTestException;

public class DefinitionStoreTestAutomationAdapter extends DefaultTestAutomationAdapter {

    private Logger logger = LoggerFactory.getLogger(DefinitionStoreTestAutomationAdapter.class);

    private static final String[] TEST_DEFINITIONS_NEEDED_FOR_TA = {
        "src/aat/resources/CCD_BEFTA_JURISDICTION2.xlsx",
        "src/aat/resources/CCD_BEFTA_JURISDICTION3.xlsx"
    };

    private static final String[][] CCD_ROLES_NEEDED_FOR_TA = {

        { "caseworker-befta_jurisdiction_2", "PUBLIC" },
        { "caseworker-befta_jurisdiction_2-solicitor_1", "PUBLIC" },
        { "caseworker-befta_jurisdiction_2-solicitor_2", "PUBLIC" },
        { "caseworker-befta_jurisdiction_2-solicitor_3", "PUBLIC" },
        { "citizen", "PUBLIC" },

        { "caseworker-befta_jurisdiction_3", "PUBLIC" },
        { "caseworker-befta_jurisdiction_3-solicitor", "PUBLIC" }
    };

    @Override
    public void doLoadTestData() {
        RestAssured.useRelaxedHTTPSValidation();
        addCcdRoles();
        importDefinitions();
    }

    private void addCcdRoles() {
        logger.info("{} roles will be added to '{}'.", CCD_ROLES_NEEDED_FOR_TA.length,
            BeftaMain.getConfig().getTestUrl());
        for (String[] roleInfo : CCD_ROLES_NEEDED_FOR_TA) {
            try {
                logger.info("\n\nAdding CCD Role {}, {}...", roleInfo[0], roleInfo[1]);
                addCcdRole(roleInfo[0], roleInfo[1]);
                logger.info("\n\nAdded CCD Role {}, {}...", roleInfo[0], roleInfo[1]);
            } catch (Exception e) {
                logger.info("\n\nCouldn't adding CCD Role {}, {} - Exception: {}.\\n\\n", roleInfo[0], roleInfo[1], e);
            }
        }
    }

    private void addCcdRole(String role, String classification) {
        Map<String, String> ccdRoleInfo = new HashMap<>();
        ccdRoleInfo.put("role", role);
        ccdRoleInfo.put("security_classification", classification);
        Response response = asAutoTestImporter().given()
            .header("Content-type", "application/json").body(ccdRoleInfo).when()
            .put("/api/user-role");
        if (response.getStatusCode() / 100 != 2) {
            String message = "Import failed with response body: " + response.body().prettyPrint();
            message += "\nand http code: " + response.statusCode();
            throw new FunctionalTestException(message);
        }
    }

    private void importDefinitions() {
        logger.info("{} definition files will be uploaded to '{}'.", TEST_DEFINITIONS_NEEDED_FOR_TA.length,
            BeftaMain.getConfig().getTestUrl());
        for (String fileName : TEST_DEFINITIONS_NEEDED_FOR_TA) {
            try {
                logger.info("\n\nImporting {}...", fileName);
                importDefinition(fileName);
                logger.info("Imported {}.\n\n", fileName);
            } catch (Exception e) {
                logger.info("Couldn't import {} - Exception: {}.\n\n", fileName, e);
            }
        }
    }

    private void importDefinition(String file) {
        Response response = asAutoTestImporter().given().multiPart(new File(file)).when().post("/import");
        if (response.getStatusCode() != 201) {
            String message = "Import failed with response body: " + response.body().prettyPrint();
            message += "\nand http code: " + response.statusCode();
            throw new FunctionalTestException(message);
        }
    }

    private RequestSpecification asAutoTestImporter() {
        UserData caseworker = new UserData(BeftaMain.getConfig().getImporterAutoTestEmail(),
            BeftaMain.getConfig().getImporterAutoTestPassword());
        authenticate(caseworker);

        String s2sToken = getNewS2SToken();
        return RestAssured
            .given(new RequestSpecBuilder().setBaseUri(BeftaMain.getConfig().getTestUrl())
                .build())
            .header("Authorization", "Bearer " + caseworker.getAccessToken())
            .header("ServiceAuthorization", s2sToken);
    }

}
