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

}
