package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.definition.store.rest.service.AccessTypesService;

/**
 * Provider PACT verification for POST /retrieve-access-types (AccessTypesController). The most
 * likely future consumer is rpx-xui-manage-organisations, which calls this endpoint at runtime.
 *
 * <p>No consumer currently publishes a contract under this provider name; the test is
 * forward-ready and passes via {@literal @}IgnoreNoPactsToVerify until one appears.</p>
 */
@Provider("ccdDefinitionStoreAPI_accessTypes")
@PactBroker(url = "${PACT_BROKER_FULL_URL:http://localhost:9292}",
    consumerVersionSelectors = {@VersionSelector(tag = "${PACT_BRANCH_NAME:Dev}")},
    providerTags = "${pactbroker.providerTags:master}",
    enablePendingPacts = "${pactbroker.enablePending:true}")
@IgnoreNoPactsToVerify
@ExtendWith(SpringExtension.class)
public class AccessTypesProviderTest {

    @Mock
    private AccessTypesService accessTypesService;


    @BeforeEach
    void before(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(new AccessTypesController(accessTypesService));
        if (context != null) {
            context.setTarget(testTarget);
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }


    @State("Access types exist for organisation profiles")
    public void accessTypesExist() {
        // State setup (mock accessTypesService) to be completed when a consumer contract is published.
    }
}
