package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import java.io.File;
import java.util.function.Supplier;

import static org.hamcrest.core.Is.is;

class ImportDefinitionTest extends BaseTest {

    protected ImportDefinitionTest(AATHelper aat) {
        super(aat);
    }

    @Test
    @DisplayName("Should import a valid definition")
    void shouldImportValidDefinition() {

        Supplier<RequestSpecification> asUser = asAutoTestImporter();
        Response post = asUser.get()
            .given()
            .multiPart(new File("src/resource/CCD_CNP_27.xlsx"))
            .when()
            .post("/import");

        System.out.println("import call response" + post.getBody().asString());

        Assert.assertThat(post.getStatusCode(), is(201));
    }

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
