package uk.gov.hmcts.ccd.definitionstore.befta;

import io.cucumber.java.Before;
import io.restassured.RestAssured;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.junit.AssumptionViolatedException;
import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.exception.FunctionalTestException;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.befta.util.BeftaUtils;
import uk.gov.hmcts.befta.util.ReflectionUtils;

import java.util.UUID;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore.VALID_CCD_TEST_DEFINITIONS_PATH;

public class DefinitionStoreTestAutomationAdapter extends DefaultTestAutomationAdapter {

    public static final String TEMPORARY_DEFINITION_FOLDER = "build/tmp/definition_files_copy";

    private DataLoaderToDefinitionStore testDataLoader;

    @Before("@groupaccess")
    public void skipGroupAccessTestsIfNotEnabled() {
        if (!ofNullable(System.getenv("GROUP_ACCESS_ENABLED")).map(Boolean::valueOf).orElse(false)) {
            throw new AssumptionViolatedException("Group Access not Enabled");
        }
    }

    @Before("@F-110")
    public void installRetrieveAccessTypesDiagnosticFilter() {
        BeftaUtils.defaultLog(String.format(
            "F-110 environment: TEST_URL=%s, DEFINITION_STORE_URL_BASE=%s, IDAM_API_URL_BASE=%s, S2S_URL_BASE=%s",
            environmentValue("TEST_URL"),
            environmentValue("DEFINITION_STORE_URL_BASE"),
            environmentValue("IDAM_API_URL_BASE"),
            environmentValue("S2S_URL_BASE")
        ));

        if (RestAssured.filters().stream().noneMatch(RetrieveAccessTypesDiagnosticFilter.class::isInstance)) {
            RestAssured.filters(new RetrieveAccessTypesDiagnosticFilter());
        }
    }

    @Override
    protected BeftaTestDataLoader buildTestDataLoader() {
        initialiseTestDataLoader();
        return this.testDataLoader;
    }

    @Override
    public synchronized Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        if (key.toString().equals("unique_import_job_id")) {
            return UUID.randomUUID().toString();
        } else if (key.toString().startsWith("no_dynamic_injection_")) {
            return key.toString().replace("no_dynamic_injection_","");
        } else if (key.toString().startsWith("approximately ")) {
            try {
                String actualSizeFromHeaderStr = (String) ReflectionUtils.deepGetFieldInObject(scenarioContext,
                    "testData.actualResponse.headers.Content-Length");
                String expectedSizeStr = key.toString().replace("approximately ", "");

                int actualSize =  Integer.parseInt(actualSizeFromHeaderStr);
                int expectedSize = Integer.parseInt(expectedSizeStr);

                if (Math.abs(actualSize - expectedSize) < (actualSize * 10 / 100)) {
                    return actualSizeFromHeaderStr;
                }
                return expectedSize;
            } catch (Exception e) {
                throw new FunctionalTestException("Problem checking acceptable response payload: ", e);
            }
        } else if (key.toString().startsWith("contains ")) {
            try {
                String actualValueStr = (String) ReflectionUtils.deepGetFieldInObject(scenarioContext,
                    "testData.actualResponse.body.__plainTextValue__");
                String expectedValueStr = key.toString().replace("contains ", "");

                if (actualValueStr.contains(expectedValueStr)) {
                    return actualValueStr;
                }
                BeftaUtils.defaultLog("Response body did not contain expected value. "
                    + "Expected fragment: " + expectedValueStr);
                BeftaUtils.defaultLog("Actual response body: " + actualValueStr);
                return "expectedValueStr " + expectedValueStr + " not present in response ";
            } catch (Exception e) {
                throw new FunctionalTestException("Problem checking acceptable response payload: ", e);
            }
        }
        return super.calculateCustomValue(scenarioContext, key);
    }

    public void initialiseTestDataLoader() {
        if (testDataLoader == null) {
            testDataLoader = new DataLoaderToDefinitionStore(this, VALID_CCD_TEST_DEFINITIONS_PATH) {

                @Override
                protected void createRoleAssignment(String resource, String filename) {
                    // Do not create role assignments.
                    BeftaUtils.defaultLog("Will NOT create role assignments!");
                }

            };

            BeftaUtils.defaultLog(String.format(
                "Copy valid def files generated from a JSON template to a temporary location for use in FTAs: '%s'",
                TEMPORARY_DEFINITION_FOLDER
            ));
            testDataLoader.getAllDefinitionFilesToLoadAt(VALID_CCD_TEST_DEFINITIONS_PATH, TEMPORARY_DEFINITION_FOLDER);
            BeftaUtils.defaultLog("Copy complete.\n");
        }
    }

    private String environmentValue(String name) {
        return ofNullable(System.getenv(name)).filter(value -> !value.trim().isEmpty()).orElse("<not set>");
    }

    private static class RetrieveAccessTypesDiagnosticFilter implements OrderedFilter {

        private static final String RETRIEVE_ACCESS_TYPES_URI = "/retrieve-access-types";

        @Override
        public int getOrder() {
            return HIGHEST_PRECEDENCE;
        }

        @Override
        public Response filter(FilterableRequestSpecification requestSpec,
                               FilterableResponseSpecification responseSpec,
                               FilterContext ctx) {
            if (!isRetrieveAccessTypesRequest(requestSpec)) {
                return ctx.next(requestSpec, responseSpec);
            }

            BeftaUtils.defaultLog(String.format(
                "Submitting F-110 request: method=%s, uri=%s, userDefinedPath=%s",
                valueOrNotAvailable(requestSpec.getMethod()),
                valueOrNotAvailable(requestSpec.getURI()),
                valueOrNotAvailable(requestSpec.getUserDefinedPath())
            ));
            return ctx.next(requestSpec, responseSpec);
        }

        private boolean isRetrieveAccessTypesRequest(FilterableRequestSpecification requestSpec) {
            String uri = requestSpec.getURI();
            String userDefinedPath = requestSpec.getUserDefinedPath();

            return "POST".equalsIgnoreCase(requestSpec.getMethod())
                && (RETRIEVE_ACCESS_TYPES_URI.equals(userDefinedPath)
                    || uri != null && uri.contains(RETRIEVE_ACCESS_TYPES_URI));
        }

        private String valueOrNotAvailable(String value) {
            return value == null ? "<not available>" : value;
        }
    }

}
