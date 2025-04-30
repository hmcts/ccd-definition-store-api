package uk.gov.hmcts.ccd.definition.store.repository.model;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.verify;

public class AccessTypeJurisdictionResultsTest {
    private final List<String> orgProfileIds = List.of(new String[]{"SOLICITOR_ORG", "SOLICITOR_ORG"});

    private AccessTypeJurisdictionResults jurisdictionResults;

    private List<AccessTypeJurisdictionResult> accessTypeRolesJurisdictions;
    private AccessTypeJurisdictionResult accessTypeRolesJurisdictionResult =
        new AccessTypeJurisdictionResult();
    private List<AccessTypeResult> accessTypeResults = new ArrayList<>();
    private AccessTypeResult accessTypeResult = new AccessTypeResult();
    private List<AccessTypeRoleResult> accessTypeRoleResults = new ArrayList<>();

    private AccessTypeRoleResult accessTypeRoleResult = new AccessTypeRoleResult();

    @Test
    public void shouldGetAccessTypeRolesJurisdictionResults() {

        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();

        jurisdictionResults = Mockito.spy(new AccessTypeJurisdictionResults());
        accessTypeRolesJurisdictions = Mockito.spy(new ArrayList<>());
        organisationProfileIds.setOrganisationProfileIds(orgProfileIds);

        // for each jurisdiction build access types
        accessTypeResult.setOrganisationProfileId("SOLICITOR_ORG");
        accessTypeResult.setAccessTypeId("AccessTypeId");

        accessTypeResults.add(accessTypeResult);
        accessTypeRolesJurisdictionResult.setAccessTypes(accessTypeResults);

        accessTypeRoleResult.setGroupRoleName("NAME");
        accessTypeRoleResult.setCaseTypeId("CaseTypeID");
        accessTypeRoleResult.setOrganisationalRoleName("ORGROLENAME");
        accessTypeRoleResult.setCaseGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");

        accessTypeRoleResults.add(accessTypeRoleResult);

        accessTypeResult.setRoles(accessTypeRoleResults);

        accessTypeResults.add(accessTypeResult);
        accessTypeRolesJurisdictionResult.setAccessTypes(accessTypeResults);
        accessTypeRolesJurisdictions.add(accessTypeRolesJurisdictionResult);
        jurisdictionResults.setJurisdictions(accessTypeRolesJurisdictions);

        jurisdictionResults.getJurisdictions();
        verify(jurisdictionResults).getJurisdictions();

        assertThat(jurisdictionResults.getJurisdictions(), is(not(nullValue())));

    }
}
