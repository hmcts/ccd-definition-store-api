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

public class AccessTypeJurisdictionResultsTest {
    private final List<String> orgProfileIds = List.of(new String[]{"SOLICITOR_ORG", "SOLICITOR_ORG"});

    private AccessTypeRolesJurisdictionResults jurisdictionResults;

    private List<AccessTypeRolesJurisdictionResult> accessTypeRolesJurisdictions;
    private  AccessTypeRolesJurisdictionResult accessTypeRolesJurisdictionResult =
        new AccessTypeRolesJurisdictionResult();

    private AccessTypeRoleResult accessTypeRoleResult = new AccessTypeRoleResult();
    private List<AccessTypeRolesRoleResult> accessTypeRolesRoleResults = new ArrayList<>();

    private AccessTypeRolesRoleResult accessTypeRolesRoleResult = new AccessTypeRolesRoleResult();

    @Test
    public void shouldGetAccessTypeJurisdictionResults() {

        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();

        jurisdictionResults = Mockito.spy(new AccessTypeRolesJurisdictionResults());
        accessTypeRolesJurisdictions = Mockito.spy(new ArrayList<>());
        organisationProfileIds.setOrganisationProfileIds(orgProfileIds);

        // for each jurisdiction build access type Roles
        AccessTypeField accessType = new AccessTypeField();
        accessType.setOrganisationProfileId("SOLICITOR_ORG");
        assertThat(accessType.getOrganisationProfileId(), is(not(nullValue())));

        accessType.setAccessTypeId("AccessTypeId");
        assertThat(accessType.getAccessTypeId(), is(not(nullValue())));

        accessTypeRolesRoleResult.setGroupRoleName("NAME");
        accessTypeRolesRoleResult.setCaseTypeId("CaseTypeID");
        accessTypeRolesRoleResult.setOrganisationalRoleName("ORGROLENAME");
        accessTypeRolesRoleResult.setCaseGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");

        accessTypeRoleResult.setAccessDefault(false);
        assertThat(accessTypeRoleResult.getAccessDefault(), is(false));

        accessTypeRoleResult.setDisplay(true);
        assertThat(accessTypeRoleResult.getDisplay(), is(true));

        accessTypeRoleResult.setAccessMandatory(true);
        assertThat(accessTypeRoleResult.getAccessMandatory(), is(true));

        accessTypeRoleResult.setDescription("Testing");
        assertThat(accessTypeRoleResult.getDescription(), is(not(nullValue())));

        accessTypeRoleResult.setDisplayOrder(10);
        assertThat(accessTypeRoleResult.getDisplayOrder(), is(not(nullValue())));

        accessTypeRoleResult.setHint("Hint text");
        assertThat(accessTypeRoleResult.getHint(), is(not(nullValue())));

        accessTypeRolesRoleResults.add(accessTypeRolesRoleResult);

        accessTypeRoleResult.setRoles(accessTypeRolesRoleResults);

        accessTypeRolesJurisdictions.add(accessTypeRolesJurisdictionResult);
        jurisdictionResults.setJurisdictions(accessTypeRolesJurisdictions);

        jurisdictionResults.getJurisdictions();
        verify(jurisdictionResults).getJurisdictions();

        assertThat(jurisdictionResults.getJurisdictions(), is(not(nullValue())));

    }

}
