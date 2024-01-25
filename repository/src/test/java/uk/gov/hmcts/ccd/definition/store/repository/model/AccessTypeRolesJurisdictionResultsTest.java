package uk.gov.hmcts.ccd.definition.store.repository.model;

import org.junit.Test;
import org.mockito.Mockito;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.verify;

public class AccessTypeRolesJurisdictionResultsTest {
    private final List<String> orgProfileIds = List.of(new String[]{"SOLICITOR_ORG", "SOLICITOR_ORG"});

    private AccessTypeRolesJurisdictionResults jurisdictionResults;

    private List<AccessTypeRolesJurisdictionResult> accessTypeRolesJurisdictions;
    private  AccessTypeRolesJurisdictionResult accessTypeRolesJurisdictionResult =
        new AccessTypeRolesJurisdictionResult();
    private List<AccessTypeRoleResult> accessTypeRoleResults = new ArrayList<>();
    private AccessTypeRoleResult accessTypeRoleResult = new AccessTypeRoleResult();
    private List<AccessTypeRolesRoleResult> accessTypeRolesRoleResults = new ArrayList<>();
    private AccessTypeRolesRoleResult accessTypeRolesRoleResult = new AccessTypeRolesRoleResult();

    @Test
    public void shouldGetAccessTypeRolesJurisdictionResults() {

        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();

        jurisdictionResults = Mockito.spy(new AccessTypeRolesJurisdictionResults());
        accessTypeRolesJurisdictions = Mockito.spy(new ArrayList<>());
        organisationProfileIds.setOrganisationProfileIds(orgProfileIds);

        // for each jurisdiction build access type Roles
        accessTypeRoleResult.setOrganisationProfileId("SOLICITOR_ORG");
        accessTypeRoleResult.setAccessTypeId("AccessTypeId");

        accessTypeRoleResults.add(accessTypeRoleResult);
        accessTypeRolesJurisdictionResult.setAccessTypeRoles(accessTypeRoleResults);

        accessTypeRolesRoleResult.setGroupRoleName("NAME");
        accessTypeRolesRoleResult.setCaseTypeId("CaseTypeID");
        accessTypeRolesRoleResult.setOrganisationalRoleName("ORGROLENAME");
        accessTypeRolesRoleResult.setCaseGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");

        accessTypeRolesRoleResults.add(accessTypeRolesRoleResult);

        accessTypeRoleResult.setRoles(accessTypeRolesRoleResults);

        accessTypeRoleResults.add(accessTypeRoleResult);
        accessTypeRolesJurisdictionResult.setAccessTypeRoles(accessTypeRoleResults);
        accessTypeRolesJurisdictions.add(accessTypeRolesJurisdictionResult);
        jurisdictionResults.setJurisdictions(accessTypeRolesJurisdictions);

        jurisdictionResults.getJurisdictions();
        verify(jurisdictionResults).getJurisdictions();

        assertThat(jurisdictionResults.getJurisdictions(), is(not(nullValue())));

    }
}
