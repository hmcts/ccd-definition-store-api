package uk.gov.hmcts.ccd.definitionstore.tests.functional.elasticsearch;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;

class ElasticsearchImportDefinitionTest extends ElasticsearchBaseTest {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchImportDefinitionTest.class);
    private static final String DEFINITION_FILE = "src/resource/CCD_CNP_27.xlsx";
    private static final String DEFINITION_FILE_WITH_NEW_FIELD = "src/resource/CCD_CNP_27_WithNewField.xlsx";

    private static final String CASE_INDEX_NAME = "mapper_cases-000001";
    private static final String CASE_INDEX_ALIAS = "mapper_cases";
    private static final String TEXT_FIELD_TYPE = "text";

    protected ElasticsearchImportDefinitionTest(AATHelper aat) {
        super(aat);
    }

    @BeforeAll
    static void setUp() {
        // stop execution of these tests if elasticsearch is not enabled
        boolean elasticsearchEnabled = ofNullable(System.getenv("ELASTIC_SEARCH_ENABLED")).map(Boolean::valueOf).orElse(false);
        assumeTrue(elasticsearchEnabled, () -> "Ignoring Elasticsearch tests, variable ELASTIC_SEARCH_ENABLED not set");
    }

    @BeforeEach
    void cleanUp() {
        deleteIndexAndAlias(CASE_INDEX_NAME, CASE_INDEX_ALIAS);
    }

    @Test
    @DisplayName("should import a valid definition multiple times and create Elasticsearch index, alias and field mappings")
    void shouldImportValidDefinitionMultipleTimes() {
        // invoke definition import
        asAutoTestImporter().get()
            .given()
            .multiPart(new File(DEFINITION_FILE))
            .expect()
            .statusCode(201)
            .when()
            .post("/import");

        verifyIndexAndFieldMappingsWithRetries(false);

        // invoke definition import second time
        asAutoTestImporter().get()
            .given()
            .multiPart(new File(DEFINITION_FILE_WITH_NEW_FIELD))
            .expect()
            .statusCode(201)
            .when()
            .post("/import");

        verifyIndexAndFieldMappingsWithRetries(true);
    }

    private void verifyIndexAndFieldMappingsWithRetries(boolean verifyNewFields) {
        with()
            .pollDelay(500, TimeUnit.MILLISECONDS)
            .and()
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .await("ES index verification")
            .atMost(60, TimeUnit.SECONDS)
            .until(() -> verifyIndexAndFieldMappings(verifyNewFields));
    }

    private boolean verifyIndexAndFieldMappings(boolean verifyNewFields) {
        try {
            ValidatableResponse response = getIndexInformation(CASE_INDEX_ALIAS);
            verifyIndexAndAlias(response);
            verifyCaseDataFields(response, verifyNewFields);
        } catch (AssertionError e) {
            log.info("Retrying Elasticsearch index api");
            return false;
        }
        return true;
    }

    private void verifyIndexAndAlias(ValidatableResponse response) {
        response
            .body("", hasKey(CASE_INDEX_NAME))
            .body(CASE_INDEX_NAME + ".aliases", hasKey(CASE_INDEX_ALIAS));
    }

    private void verifyCaseDataFields(ValidatableResponse response, boolean verifyNewFields) {
        response.root(CASE_INDEX_NAME + ".mappings." + DEFAULT_DOC_TYPE + ".properties.data.properties");
        if (verifyNewFields) {
            verifyFieldsAndType(response, TEXT_FIELD_TYPE, "NewTextField");
        } else {
            verifyFieldsDoNotOccurInResponse(response, "NewTextField");
        }
    }

    private void verifyFieldsAndType(ValidatableResponse response, String type, String... fields) {
        Stream.of(fields).forEach(field -> response
            .body("", hasKey(field))
            .body(field + ".type", is(type)));
    }

    private void verifyFieldsDoNotOccurInResponse(ValidatableResponse response, String... fields) {
        Stream.of(fields).forEach(field -> response.body("", not(hasKey(field))));
    }

}
