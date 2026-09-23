package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import java.util.function.Supplier;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class DraftDefinitionTest extends BaseTest {

    private static final String JURISDICTION = "AUTOTEST1";
    private static final String DESCRIPTION = "Functional test draft definition";
    private static final String UPDATED_DESCRIPTION = "Updated functional test draft definition";

    protected DraftDefinitionTest(AATHelper aat) {
        super(aat);
    }

    private final Supplier<RequestSpecification> asUser = asAutoTestCaseworker();

    @Test
    @DisplayName("Should create a draft definition")
    void shouldCreateDraftDefinition() {
        Integer version = null;

        try {
            Response createdDraft = asUser.get()
                .given()
                .contentType(ContentType.JSON)
                .body(definitionBody(DESCRIPTION))
                .when()
                .post("/api/draft");

            createdDraft.then()
                .statusCode(201)
                .body("jurisdiction.id", equalTo(JURISDICTION))
                .body("description", equalTo(DESCRIPTION))
                .body("version", notNullValue());

            version = createdDraft.path("version");
        } finally {
            deleteDraft(version);
        }
    }

    @Test
    @DisplayName("Should retrieve a draft definition")
    void shouldRetrieveDraftDefinition() {
        Integer version = createDraft(DESCRIPTION);

        try {
            asUser.get()
                .given()
                .queryParam("jurisdiction", JURISDICTION)
                .queryParam("version", version)
                .when()
                .get("/api/draft")
                .then()
                .statusCode(200)
                .body("jurisdiction.id", equalTo(JURISDICTION))
                .body("version", equalTo(version))
                .body("description", equalTo(DESCRIPTION));
        } finally {
            deleteDraft(version);
        }
    }

    @Test
    @DisplayName("Should retrieve draft definitions for a jurisdiction")
    void shouldRetrieveDraftDefinitions() {
        Integer version = createDraft(DESCRIPTION);

        try {
            asUser.get()
                .given()
                .queryParam("jurisdiction", JURISDICTION)
                .when()
                .get("/api/drafts")
                .then()
                .statusCode(200)
                .body("findAll { draft -> draft.version == " + version + " }[0].description",
                    equalTo(DESCRIPTION));
        } finally {
            deleteDraft(version);
        }
    }

    @Test
    @DisplayName("Should save a draft definition")
    void shouldSaveDraftDefinition() {
        Integer version = createDraft(DESCRIPTION);

        try {
            asUser.get()
                .given()
                .contentType(ContentType.JSON)
                .body(definitionBody(UPDATED_DESCRIPTION, version))
                .when()
                .put("/api/draft/save")
                .then()
                .statusCode(200)
                .body("jurisdiction.id", equalTo(JURISDICTION))
                .body("version", equalTo(version))
                .body("description", equalTo(UPDATED_DESCRIPTION));
        } finally {
            deleteDraft(version);
        }
    }

    @Test
    @DisplayName("Should delete a draft definition")
    void shouldDeleteDraftDefinition() {
        Integer version = createDraft(DESCRIPTION);

        asUser.get()
            .given()
            .pathParam("jurisdiction", JURISDICTION)
            .pathParam("version", version)
            .when()
            .delete("/api/draft/{jurisdiction}/{version}")
            .then()
            .statusCode(204);
    }

    @Test
    @DisplayName("Should return bad request when deleting an unknown draft version")
    void shouldRejectUnknownDraftVersion() {
        asUser.get()
            .given()
            .pathParam("jurisdiction", JURISDICTION)
            .pathParam("version", Integer.MAX_VALUE)
            .when()
            .delete("/api/draft/{jurisdiction}/{version}")
            .then()
            .statusCode(400);
    }

    private Integer createDraft(String description) {
        Response createdDraft = asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body(definitionBody(description))
            .when()
            .post("/api/draft");

        createdDraft.then()
            .statusCode(201)
            .body("jurisdiction.id", equalTo(JURISDICTION))
            .body("version", notNullValue());

        return createdDraft.path("version");
    }

    private void deleteDraft(Integer version) {
        if (version != null) {
            asUser.get()
                .given()
                .pathParam("jurisdiction", JURISDICTION)
                .pathParam("version", version)
                .when()
                .delete("/api/draft/{jurisdiction}/{version}")
                .then()
                .statusCode(204);
        }
    }

    private String definitionBody(String description) {
        return definitionBody(description, null);
    }

    private String definitionBody(String description, Integer version) {
        String versionProperty = version == null ? "" : String.format(",\n  \"version\": %d", version);
        return String.format("{\n"
            + "  \"jurisdiction\": {\"id\": \"%s\"},\n"
            + "  \"description\": \"%s\",\n"
            + "  \"author\": \"definition-store-functional-tests\",\n"
            + "  \"case_types\": \"AAT\",\n"
            + "  \"data\": {\"Data\": {\"Field1\": \"Value1\"}}%s\n"
            + "}", JURISDICTION, description, versionProperty);
    }
}
