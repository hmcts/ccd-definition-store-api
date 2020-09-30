package uk.gov.hmcts.ccd.definitionstore.tests.functional.elasticsearch;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.BaseTest;

import static org.hamcrest.CoreMatchers.equalTo;

abstract class ElasticsearchBaseTest extends BaseTest {

    private final Logger log = LoggerFactory.getLogger(ElasticsearchBaseTest.class);

    protected static final String DEFAULT_DOC_TYPE = "_doc";

    ElasticsearchBaseTest(AATHelper aat) {
        super(aat);
    }

    void deleteIndexAndAlias(String indexName, String indexAlias) {
        ValidatableResponse response = getIndexInformation(indexAlias);
        try {
            response.statusCode(200);
            deleteIndexAlias(indexName, indexAlias);
            deleteIndex(indexName);
        } catch (AssertionError e) {
            log.warn("{} index does not exist", indexName);
        }
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
            .then();
    }

    private RequestSpecification asElasticsearchApiUser() {
        return RestAssured.given(new RequestSpecBuilder()
            .setBaseUri(aat.getElasticsearchBaseUri())
            .build());
    }
}
