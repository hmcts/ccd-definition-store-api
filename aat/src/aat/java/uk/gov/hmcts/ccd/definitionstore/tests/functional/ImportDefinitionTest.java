package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import java.io.File;
import java.util.function.Supplier;

class ImportDefinitionTest extends BaseTest {

    protected ImportDefinitionTest(AATHelper aat) {
        super(aat);
    }

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
    @DisplayName("Should Not import a definition with missing Permissions")
    void shouldNotImportInvalidDefinitionMissingComplexAuthorization() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27_Missing_Complex_Authorization.xlsx"))
            .expect()
            .statusCode(500)
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
    }


}
