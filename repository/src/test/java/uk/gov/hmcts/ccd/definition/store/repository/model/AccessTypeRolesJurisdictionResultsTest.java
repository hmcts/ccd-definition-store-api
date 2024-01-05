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
    private List<AccessTypeRolesResult> accessTypeRolesResults = new ArrayList<AccessTypeRolesResult>();
    private AccessTypeRolesResult  accessTypeRolesResult = new AccessTypeRolesResult();
    private List<AccessTypeRolesRoleResult> accessTypeRolesRoleResults = new ArrayList<AccessTypeRolesRoleResult>();
    private AccessTypeRolesRoleResult accessTypeRolesRoleResult = new AccessTypeRolesRoleResult();

    @Test
    public void shouldGetAccessTypeRolesJurisdictionResults() {

        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();

        jurisdictionResults = Mockito.spy(new AccessTypeRolesJurisdictionResults());
        accessTypeRolesJurisdictions = Mockito.spy(new ArrayList<AccessTypeRolesJurisdictionResult>());
        organisationProfileIds.setOrganisationProfileIds(orgProfileIds);

        // for each jurisdiction build access type Roles
        accessTypeRolesResult.setOrganisationProfileId("SOLICITOR_ORG");
        accessTypeRolesResult.setAccessTypeId("AccessTypeId");
        accessTypeRolesResult.setAccessMandatory(Boolean.TRUE);
        accessTypeRolesResult.setAccessDefault(Boolean.TRUE);
        accessTypeRolesResult.setDisplay(Boolean.TRUE);
        accessTypeRolesResult.setDisplayOrder(10);
        accessTypeRolesResult.setDescription("DESCRIPTION");
        accessTypeRolesResult.setHint("Hint");

        accessTypeRolesResults.add(accessTypeRolesResult);
        accessTypeRolesJurisdictionResult.setAccessTypeRoles(accessTypeRolesResults);

        accessTypeRolesRoleResult.setGroupRoleName("NAME");
        /***** Set with getIdOfCaseType Saved previously from casetypeId before it is copied and is = null when
         * case is copied the id is null as (Property "id") has no write accessor
         * ******/
        accessTypeRolesRoleResult.setCaseTypeId("IdOfCaseType");
        accessTypeRolesRoleResult.setOrganisationalRoleName("ORGROLENAME");
        accessTypeRolesRoleResult.setCaseGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");

        accessTypeRolesRoleResults.add(accessTypeRolesRoleResult);

        accessTypeRolesResult.setRoles(accessTypeRolesRoleResults);

        accessTypeRolesResults.add(accessTypeRolesResult);
        accessTypeRolesJurisdictionResult.setAccessTypeRoles(accessTypeRolesResults);
        accessTypeRolesJurisdictions.add(accessTypeRolesJurisdictionResult);
        jurisdictionResults.setJurisdictions(accessTypeRolesJurisdictions);

        //doReturn(controller.retrieveAccessTypeRoles(organisationProfileIds))
        // .when(accessTypeRolesJurisdictions).get(0);
        jurisdictionResults.getJurisdictions();
        verify(jurisdictionResults).getJurisdictions();

        assertThat(jurisdictionResults.getJurisdictions(), is(not(nullValue())));

    }
}
