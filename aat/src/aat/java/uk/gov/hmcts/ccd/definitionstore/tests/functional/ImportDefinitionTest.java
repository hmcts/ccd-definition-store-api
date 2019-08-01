package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

class ImportDefinitionTest extends BaseTest {

    protected ImportDefinitionTest(AATHelper aat) {
        super(aat);
    }

    private Supplier<RequestSpecification> asUser = asAutoTestCaseworker();
    private static HashMap<String, String> caseTypeACLFromDefinitionFile =
        new HashMap<String, String>() {{
            put("AATPUBLIC", "PUBLIC");
            put("AATPRIVATE", "PRIVATE");
            put("AATRESTRICTED", "RESTRICTED");
        }};

    @Test
    @DisplayName("Should Not import an invalid definition")
    void shouldNotImportInvalidDefinition() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        Response response = asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Unsuccessful.xlsx"))
            .expect()
            .statusCode(400)
            .response()
            .when()
            .post("/import");
        assert (response.getBody().jsonPath().get("message").toString()
            .contains("At least one case field must be defined for case type"));
    }

    @Test
    @DisplayName("Missing SecurityType from CaseType tab")
    void shouldNotImportMissingSecurityTypeFromCaseTypeACL() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        Response response = asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Missing_SecurityType_from_CaseType.xlsx"))
            .expect()
            .statusCode(422)
            .when()
            .post("/import");

        assert (response.getBody().prettyPrint()
            .contains("Case Type with name 'Demo case' must have a Security Classification defined"));
    }

    @Test
    @DisplayName("Invalid SecurityType ACL in CaseType tab")
    void shouldNotImportInvalidCaseTypeACLInfo() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        Response response = asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Invalid_CaseType_ACL.xlsx"))
            .expect()
            .statusCode(422)
            .when()
            .post("/import");

        assert (response.getBody().prettyPrint()
            .contains("Case Type with name 'Demo case' must have a Security Classification defined"));
    }

    @Test
    @DisplayName("Missing SecurityType ACL row in CaseType tab")
    void shouldNotImportMissingCaseTypeACLInfo() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        Response response = asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Missing_CaseType_ACL_Info.xlsx"))
            .expect()
            .statusCode(400)
            .when()
            .post("/import");

        assert (response.getBody().prettyPrint()
            .contains("A definition must contain at least one Case Type"));
    }

    @Test
    @DisplayName("Should import valid Case Type info file.")
    void shouldImportValidCaseTypeACLInfoFile() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        Response response = asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_CaseType_Security_Classification_Test.xlsx"))
            .expect()
            .statusCode(201)
            .when()
            .post("/import");

        assert (response.getBody().prettyPrint()
            .equals("Case Definition data successfully imported"));
    }

    @Test
    @DisplayName("Should return the correct security classification for each case type.")
    void shouldReturnCorrectSecurityClassificationForCaseType() {

        HashMap<String, String> caseTypeACL = new HashMap<>();
        ArrayList<Map<String, String>> list = asUser.get()
            .given()
            .pathParam("jid", "AUTOTEST2")
            .contentType(ContentType.JSON)
            .when()
            .get("/api/data/jurisdictions/{jid}/case-type")
            .then()
            .statusCode(200)
            .extract()
            .path("");

        for (Map<String, String> map : list) {
            caseTypeACL.put(map.get("name"), map.get("security_classification"));
        }
        assert (caseTypeACLFromDefinitionFile.equals(caseTypeACL));
    }

    @Disabled("The response code should be 400 instead of 500. Code needs to be fixed.")
    @Test
    @DisplayName("Should Not import a definition with missing Permissions")
    void shouldNotImportInvalidDefinitionMissingComplexAuthorization() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Missing_Complex_Authorization.xlsx"))
            .expect()
            .statusCode(400)
            .when()
            .post("/import");
    }

    @Test
    @DisplayName("Should Not import a definition with missing CRUD permissions")
    void shouldNotImportDefinitionWithMissingCRUDPermissions() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Missing_CRUD_Permissions.xlsx"))
            .expect()
            .statusCode(400)
            .when()
            .post("/import");
    }

    @Test
    @DisplayName("Should Not import a definition with invalid CRUD permissions")
    void shouldNotImportDefinitionWithInvalidPermissions() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Invalid_CRUD_Permissions.xlsx"))
            .expect()
            .statusCode(422)
            .when()
            .post("/import");
    }
/*
    @Test
    @DisplayName("Should Not import a definition with invalid User Roles")
    void shouldNotImportDefinitionHavingInvalidUserRole() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Invalid_User_Role.xlsx"))
            .expect()
            .statusCode(422)
            .when()
            .post("/import");
    }*/
}
