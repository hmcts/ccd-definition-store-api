package uk.gov.hmcts.ccd.definitionstore.tests.functional;

import io.restassured.RestAssured;
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

    @Test
    @DisplayName("Should reject creating a draft without a jurisdiction")
    void shouldRejectDraftWithoutJurisdiction() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"description\": \"Invalid draft\","
                + " \"author\": \"functional-test\"}")
            .when()
            .post("/api/draft")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject malformed JSON when creating a draft")
    void shouldRejectMalformedDraftJson() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"jurisdiction\": {\"id\": \"AUTOTEST1\"")
            .when()
            .post("/api/draft")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject an empty draft request body")
    void shouldRejectEmptyDraftRequestBody() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .when()
            .post("/api/draft")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject malformed JSON when saving a draft")
    void shouldRejectMalformedDraftSaveJson() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"jurisdiction\": {\"id\": \"AUTOTEST1\"")
            .when()
            .put("/api/draft/save")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject saving a draft without a description")
    void shouldRejectDraftSaveWithoutDescription() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"jurisdiction\": {\"id\": \"AUTOTEST1\"},"
                + " \"author\": \"definition-store-functional-tests\","
                + " \"version\": 1}")
            .when()
            .put("/api/draft/save")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject saving a draft without an author")
    void shouldRejectDraftSaveWithoutAuthor() {
        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .body("{\"jurisdiction\": {\"id\": \"AUTOTEST1\"},"
                + " \"description\": \"Invalid draft\","
                + " \"version\": 1}")
            .when()
            .put("/api/draft/save")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject a non-numeric draft version")
    void shouldRejectNonNumericDraftVersion() {
        asUser.get()
            .given()
            .pathParam("jurisdiction", JURISDICTION)
            .pathParam("version", "not-a-number")
            .when()
            .delete("/api/draft/{jurisdiction}/{version}")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject retrieving a draft without a jurisdiction")
    void shouldRejectDraftLookupWithoutJurisdiction() {
        asUser.get()
            .when()
            .get("/api/draft")
            .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should reject unauthenticated draft requests")
    void shouldRejectUnauthenticatedDraftRequest() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(definitionBody(DESCRIPTION))
            .when()
            .post("/api/draft")
            .then()
            .statusCode(401);

        RestAssured.given()
            .queryParam("jurisdiction", JURISDICTION)
            .when()
            .get("/api/drafts")
            .then()
            .statusCode(401);

        RestAssured.given()
            .queryParam("jurisdiction", JURISDICTION)
            .queryParam("version", Integer.MAX_VALUE)
            .when()
            .get("/api/draft")
            .then()
            .statusCode(401);

        RestAssured.given()
            .pathParam("jurisdiction", JURISDICTION)
            .pathParam("version", Integer.MAX_VALUE)
            .when()
            .delete("/api/draft/{jurisdiction}/{version}")
            .then()
            .statusCode(401);
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
