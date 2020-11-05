package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;
import uk.gov.hmcts.ccd.definitionstore.tests.helper.ImportDefinitonDataSetup;

class ImportDefinitionTest extends BaseTest {

    protected ImportDefinitionTest(AATHelper aat) {
        super(aat);
    }

    private Supplier<RequestSpecification> asUser = asAutoTestCaseworker();

    @Disabled("The response code should be 400 instead of 500. Code needs to be fixed.")
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
    @DisplayName("Should Not import definition with invalid noc config for case type")
    void shouldNotImportDefinitionWithInvalidNocConfig() {
        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        Response response = asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_CaseType_Invalid_NoC_Config.xlsx"))
            .expect()
            .statusCode(400)
            .response()
            .when()
            .post("/import");
        assert (response.getBody().prettyPrint()
            .contains("Only one NoC config is allowed per case type(s) "
                + "AATPUBLIC,AATRESTRICTED"));
    }

    @Test
    @DisplayName("Should Not import definition with invalid case type id defined in noc config")
    void shouldNotImportDefinitionWithInvalidCaseTypeIdNocConfig() {
        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        Response response = asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_CaseType_Invalid_Case_Type_NoC_Config.xlsx"))
            .expect()
            .statusCode(400)
            .response()
            .when()
            .post("/import");
        assert (response.getBody().prettyPrint()
            .contains("Unknown Case Type(s) 'AATPUBLIC1' in worksheet 'NoticeOfChangeConfig'"));
    }

    @Disabled("This test case is breaking the master build. Marking it ignored to unblock the other developers."
        + "Fix will follow in next pull request")
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

    @Disabled("The response code should be 400 instead of 500. Code needs to be fixed.")
    @Test
    @DisplayName("Missing SecurityType ACL column in CaseType tab")
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
            .contains("At least one case field must be defined for case type"));
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
    void shouldNotImportDefinitionWithMissingCrudPermissions() {

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

    @Test
    @DisplayName("Should Not import a definition with invalid User Roles")
    void shouldNotImportDefinitionHavingInvalidUserRole() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Invalid_User_Role.xlsx"))
            .expect()
            .statusCode(400)
            .when()
            .post("/import");
    }

    @Disabled("This test case is breaking the master build. Marking it ignored to unblock the other developers."
        + "Fix will follow in next pull request")
    @Test
    @DisplayName("Should return the correct security classification for each case type.")
    void shouldReturnCorrectSecurityClassificationForCaseType() {
        long matchingACLRecords = 0;
        Set<String> caseTypeACLKeySet = new HashSet<String>() {{
                add("id");
                add("name");
                add("description");
                add("security_classification");
            }};

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

        ArrayList<Map<String, String>> caseTypeACL = asUser
            .get()
            .given()
            .pathParam("jid", "AUTOTEST1")
            .contentType(ContentType.JSON)
            .when()
            .get("/api/data/jurisdictions/{jid}/case-type")
            .then()
            .statusCode(200)
            .extract()
            .path("");

        caseTypeACL.forEach(map -> map.keySet().retainAll(caseTypeACLKeySet));

        for (Map<String, String> map : caseTypeACL) {
            matchingACLRecords = ImportDefinitonDataSetup.populateDefinitionCaseTypeData()
                .stream()
                .filter(definitionData -> definitionData.equals(map))
                .count();
        }
        assert (matchingACLRecords == 5);
    }
}
