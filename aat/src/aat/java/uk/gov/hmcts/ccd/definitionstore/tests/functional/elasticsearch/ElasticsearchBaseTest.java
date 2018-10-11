package uk.gov.hmcts.ccd.definitionstore.tests.functional.elasticsearch;

import static org.hamcrest.CoreMatchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

abstract class ElasticsearchBaseTest extends BaseTest {

    ElasticsearchBaseTest(AATHelper aat) {
        super(aat);
    }

    void deleteIndexAndAlias(String indexName, String indexAlias) {
        deleteIndexAlias(indexName, indexAlias);
        deleteIndex(indexName);
    }

    private void deleteIndexAlias(String indexName, String indexAlias) {
        asElasticsearchApiUser()
            .when()
            .delete(indexName + "/_alias/" + indexAlias)
            .then()
            .statusCode(200)
            .body("acknowledged", equalTo(true));
    }

    private void deleteIndex(String indexName) {
        // deletes the index
        asElasticsearchApiUser()
            .when()
            .delete(indexName)
            .then()
            .statusCode(200)
            .body("acknowledged", equalTo(true));
    }

    ValidatableResponse getIndexInformation(String indexAlias) {
        return asElasticsearchApiUser()
            .when()
            .get(indexAlias)
            .then()
            .statusCode(200);
    }

    private RequestSpecification asElasticsearchApiUser() {
        return RestAssured.given(new RequestSpecBuilder()
                                     .setBaseUri(aat.getElasticsearchBaseUri())
                                     .build());
    }
}
