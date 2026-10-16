package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

class ImportAuditTest extends BaseTest {

    protected ImportAuditTest(AATHelper aat) {
        super(aat);
    }

    private final Supplier<RequestSpecification> asUser = asAutoTestCaseworker();

    @Test
    @DisplayName("Should return import audits")
    void shouldReturnImportAudits() throws IOException {
        File definitionFile = new File("build/tmp/definition_files_copy/BEFTA_MASTER.xlsx");
        String uploadedFilename = "BEFTA_MASTER_" + UUID.randomUUID() + ".xlsx";
        File uploadedDefinitionFile = new File(definitionFile.getParent(), uploadedFilename);

        try {
            Files.copy(definitionFile.toPath(), uploadedDefinitionFile.toPath());

            asAutoTestImporter().get()
                .given()
                .multiPart(uploadedDefinitionFile)
                .when()
                .post("/import")
                .then()
                .statusCode(201);

            with()
                .pollInterval(1, SECONDS)
                .await()
                .atMost(15, SECONDS)
                .untilAsserted(() -> asUser.get()
                    .given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/import-audits")
                    .then()
                    .statusCode(200)
                    .body("", instanceOf(List.class))
                    .body("findAll { audit -> audit.filename.endsWith('" + uploadedFilename + "') }.size()",
                        equalTo(1)));
        } finally {
            Files.deleteIfExists(uploadedDefinitionFile.toPath());
        }
    }

    @Test
    @DisplayName("Should reject unauthenticated requests for import audits")
    void shouldRejectUnauthenticatedRequest() {
        RestAssured.given()
            .when()
            .get("/api/import-audits")
            .then()
            .statusCode(401);
    }
}
