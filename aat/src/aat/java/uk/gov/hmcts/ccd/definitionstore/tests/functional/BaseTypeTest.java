package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import java.util.function.Supplier;

@Tag("smoke")
class BaseTypeTest extends BaseTest {

    protected BaseTypeTest(AATHelper aat) {
        super(aat);
    }

    Supplier<RequestSpecification> asUser = asAutoTestCaseworker();

    @Test
    @DisplayName("Should return all valid base types")
    void shouldReturnBaseType() {

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .when()
            .get(
                "/api/base-types")
            .then()
            .statusCode(200);
    }
}
