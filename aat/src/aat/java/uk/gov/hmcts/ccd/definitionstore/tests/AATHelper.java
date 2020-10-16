package uk.gov.hmcts.ccd.definitionstore.tests;

import uk.gov.hmcts.ccd.definitionstore.tests.helper.S2SHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.helper.idam.IdamHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.helper.idam.OAuth2;

public enum AATHelper {

    INSTANCE;

    private final IdamHelper idamHelper;
    private final S2SHelper s2SHelper;

    AATHelper() {
        idamHelper = new IdamHelper(getIdamURL(), OAuth2.INSTANCE);
        s2SHelper = new S2SHelper(getS2SURL(), getGatewayServiceSecret(), getGatewayServiceName());
    }

    public String getTestUrl() {
        return Env.require("TEST_URL");
    }

    public String getIdamURL() {
        return Env.require("IDAM_URL");
    }

    public String getS2SURL() {
        return Env.require("S2S_URL");
    }

    public String getImporterAutoTestEmail() {
        return Env.require("CCD_IMPORT_AUTOTEST_EMAIL");
    }

    public String getImporterAutoTestPassword() {
        return Env.require("CCD_IMPORT_AUTOTEST_PASSWORD");
    }

    public String getCaseworkerAutoTestEmail() {
        return Env.require("CCD_CASEWORKER_AUTOTEST_EMAIL");
    }

    public String getCaseworkerAutoTestPassword() {
        return Env.require("CCD_CASEWORKER_AUTOTEST_PASSWORD");
    }

    public String getGatewayServiceName() {
        return Env.require("BEFTA_S2S_CLIENT_ID");
    }

    public String getGatewayServiceSecret() {
        return Env.require("BEFTA_S2S_CLIENT_SECRET");
    }

    public IdamHelper getIdamHelper() {
        return idamHelper;
    }

    public S2SHelper getS2SHelper() {
        return s2SHelper;
    }

    public String getElasticsearchBaseUri() {
        return Env.require("ELASTIC_SEARCH_SCHEME") + "://"
            + Env.require("ELASTIC_SEARCH_HOST") + ":"
            + Env.require("ELASTIC_SEARCH_PORT");
    }
}
