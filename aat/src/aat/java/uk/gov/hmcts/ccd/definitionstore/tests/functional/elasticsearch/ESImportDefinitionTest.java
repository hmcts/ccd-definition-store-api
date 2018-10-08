package uk.gov.hmcts.ccd.definitionstore.tests.functional.elasticsearch;

import java.io.File;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

class ESImportDefinitionTest extends BaseTest {

    private static final String CASE_INDEX_NAME = "es_test_cases-000001";
    private static final String CASE_INDEX_ALIAS = "es_test_cases";
    private static final String TEXT_FIELD_TYPE = "text";
    private static final String DATE_FIELD_TYPE = "date";
    private static final String NUMBER_FIELD_TYPE = "double";
    private static final String KEYWORD_FIELD_TYPE = "keyword";

    protected ESImportDefinitionTest(AATHelper aat) {
        super(aat);
    }

    @BeforeAll
    static void setUp() {
        assumeTrue(null != System.getenv("ELASTICSEARCH_BASE_URI"), () -> "Ignoring Elasticsearch tests, variable ELASTICSEARCH_BASE_URI not set");
    }

    @AfterEach
    void deleteIndex() {
        // deletes the index
        asElasticsearchApiUser()
            .when()
            .delete(CASE_INDEX_NAME)
            .then()
            .statusCode(200)
            .body("acknowledged", equalTo(true));
    }

    @Nested
    @DisplayName("successful run")
    class SuccessfulRun {
        @Test
        @DisplayName("Should import a valid definition multiple times and create Elasticsearch index, alias and field mappings")
        void shouldImportValidDefinitionMultipleTimes() {
            // invoke definition import
            asAutoTestImporter().get()
                .given()
                .multiPart(new File("src/resource/CCD_TEST_ES.xlsx"))
                .expect()
                .statusCode(201)
                .when()
                .post("/import");

            verifyIndexAndFieldMappings(false);

            // invoke definition import second time
            asAutoTestImporter().get()
                .given()
                .multiPart(new File("src/resource/CCD_TEST_ES_WithNewField.xlsx"))
                .expect()
                .statusCode(201)
                .when()
                .post("/import");

            verifyIndexAndFieldMappings(true);
        }
    }

    @Nested
    @DisplayName("Failed run")
    class FailedRun {
        @Test
        @DisplayName("Should not import definition when Elasticsearch throws exception")
        void shouldNotImportDefinitionOnEsError() {
            // invoke definition import
            asAutoTestImporter().get()
                .given()
                .multiPart(new File("src/resource/CCD_TEST_ES.xlsx"))
                .expect()
                .statusCode(201)
                .when()
                .post("/import");

            verifyIndexAndFieldMappings(false);

            removeAlias();

            // invoking definition import second time should throw exception as alias does not exist
            asAutoTestImporter().get()
                .given()
                .multiPart(new File("src/resource/CCD_TEST_ES_WithNewField.xlsx"))
                .expect()
                .statusCode(503)
                .when()
                .post("/import");
        }

        private void removeAlias() {
            // deletes the index
            asElasticsearchApiUser()
                .given()
                .contentType(ContentType.JSON)
                .body("{\"actions\":[{\"remove\":{\"index\":\"es_test_cases-000001\",\"alias\":\"es_test_cases\"}}]}")
                .when()
                .post("_aliases")
                .then()
                .statusCode(200)
                .body("acknowledged", equalTo(true));

        }
    }

    private void verifyIndexAndFieldMappings(boolean verifyNewFields) {
        ValidatableResponse response = asElasticsearchApiUser()
            .when()
            .get(CASE_INDEX_ALIAS)
            .then()
            .statusCode(200);

        verifyIndexAndAlias(response);

        response.root(CASE_INDEX_NAME + ".mappings.case.properties.data.properties");

        verifyAddressField(response);
        verifyFieldsAndType(response, TEXT_FIELD_TYPE, "FixedListField", "MultiSelectListField", "PhoneUKField", "TextAreaField", "TextField");
        verifyFieldsAndType(response, DATE_FIELD_TYPE, "DateField", "DateTimeField");
        verifyFieldsAndType(response, NUMBER_FIELD_TYPE, "MoneyGBPField", "NumberField");
        verifyFieldsAndType(response, KEYWORD_FIELD_TYPE, "EmailField", "YesOrNoField");

        if (verifyNewFields) {
            verifyFieldsAndType(response, "text", "NewTextField");
        } else {
            verifyFieldsDoNotOccurInResponse(response, "NewTextField");
        }
    }

    private void verifyIndexAndAlias(ValidatableResponse responseBody) {
        responseBody
            .body("", hasKey(CASE_INDEX_NAME))
            .body(CASE_INDEX_NAME + ".aliases", hasKey(CASE_INDEX_ALIAS));
    }

    private void verifyAddressField(ValidatableResponse responseBody) {
        responseBody.appendRoot("AddressUKField.properties");
        verifyFieldsAndType(responseBody, TEXT_FIELD_TYPE, "AddressLine1", "PostCode", "Country");
        responseBody.detachRoot("AddressUKField.properties");
    }

    private void verifyFieldsAndType(ValidatableResponse responseBody, String type, String... fields) {
        Stream.of(fields).forEach(field -> responseBody
                                          .body("", hasKey(field))
                                          .body(field + ".type", is(type)));
    }

    private void verifyFieldsDoNotOccurInResponse(ValidatableResponse responseBody, String... fields) {
        Stream.of(fields).forEach(field -> responseBody.body("", not(hasKey(field))));
    }

    private RequestSpecification asElasticsearchApiUser() {
        return RestAssured.given(new RequestSpecBuilder()
                                     .setBaseUri(aat.getElasticsearchBaseUri())
                                     .build());
    }

}
