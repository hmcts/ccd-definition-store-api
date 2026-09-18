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
import uk.gov.hmcts.ccd.definition.store.domain.service.CaseRoleService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionService;
import uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles.RoleToAccessProfileService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;

/**
 * Provider PACT verification for the case definition data endpoints (CaseDefinitionController):
 * case type schema, jurisdictions, case types by jurisdiction, case roles and access profiles.
 *
 * <p>No consumer currently publishes a contract under this provider name; the test is
 * forward-ready and passes via {@literal @}IgnoreNoPactsToVerify until one appears.</p>
 */
@Provider("ccdDefinitionStoreAPI_caseDefinition")
@PactBroker(url = "${PACT_BROKER_FULL_URL:http://localhost:9292}",
    consumerVersionSelectors = {@VersionSelector(tag = "${PACT_BRANCH_NAME:Dev}")},
    providerTags = "${pactbroker.providerTags:master}",
    enablePendingPacts = "${pactbroker.enablePending:true}")
@IgnoreNoPactsToVerify
@ExtendWith(SpringExtension.class)
public class CaseDefinitionProviderTest {

    @Mock
    private CaseTypeService caseTypeService;

    @Mock
    private JurisdictionService jurisdictionService;

    @Mock
    private CaseRoleService caseRoleService;

    @Mock
    private RoleToAccessProfileService roleToAccessProfilesService;


    @BeforeEach
    void before(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(new CaseDefinitionController(caseTypeService, jurisdictionService, caseRoleService,
            roleToAccessProfilesService));
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


    @State("A case type schema exists")
    public void caseTypeSchemaExists() {
        // State setup (mock caseTypeService) to be completed when a consumer contract is published.
    }

    @State("Case roles exist for a case type")
    public void caseRolesExist() {
        // State setup (mock caseRoleService) to be completed when a consumer contract is published.
    }

    @State("Jurisdictions exist")
    public void jurisdictionsExist() {
        // State setup (mock jurisdictionService) to be completed when a consumer contract is published.
    }
}
