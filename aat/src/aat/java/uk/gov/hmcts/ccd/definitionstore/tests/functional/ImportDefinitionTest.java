package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
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

    Supplier<RequestSpecification> asUser = asAutoTestCaseworker();
    static HashMap<String, String> caseTypeACLFromDefinitionFile =
        new HashMap<String, String>() {{
            put("AATPUBLIC", "PUBLIC");
            put("AATPRIVATE", "PRIVATE");
            put("AATRESTRICTED", "RESTRICTED");
        }};

    // Success case - valid import file is verified as part of setup in DataSetupExtension.

    @Test
    @DisplayName("Should Not import an invalid definition")
    void shouldNotImportInvalidDefinition() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Unsuccessful.xlsx"))
            .expect()
            .statusCode(400)
            .when()
            .post("/import");
    }

    @Test
    @DisplayName("Missing SecurityType from CaseType tab")
    void shouldNotImportMissingSecurityTypeFromCaseTypeACL() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Missing_SecurityType_from_CaseType.xlsx"))
            .expect()
            .statusCode(422)
            .when()
            .post("/import");
    }

    @Test
    @DisplayName("Invalid SecurityType ACL in CaseType tab")
    void shouldNotImportInvalidCaseTypeACLInfo() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Invalid_CaseType_ACL.xlsx"))
            .expect()
            .statusCode(422)
            .when()
            .post("/import");
    }

    @Test
    @DisplayName("Missing SecurityType ACL row in CaseType tab")
    void shouldNotImportMissingCaseTypeACLInfo() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Missing_CaseType_ACL_Info.xlsx"))
            .expect()
            .statusCode(400)
            .when()
            .post("/import");
    }

    @Test
    @DisplayName("Should import valid Case Type info file.")
    void shouldImportValidCaseTypeACLInfoFile() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_CaseType_Security_Classification_Test.xlsx"))
            .expect()
            .statusCode(201)
            .when()
            .post("/import");
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
}
