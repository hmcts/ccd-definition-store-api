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

    private List<AccessTypeJurisdictionResult> accessTypeJurisdictions;
    private  AccessTypeJurisdictionResult accessTypeJurisdictionResult =
        new AccessTypeJurisdictionResult();
    private List<AccessTypeResult> accessTypeResults = new ArrayList<>();
    private AccessTypeResult  accessTypeResult = new AccessTypeResult();
    private List<AccessTypeRoleResult> accessTypeRoleResults = new ArrayList<>();
    private AccessTypeRoleResult accessTypeRoleResult = new AccessTypeRoleResult();

    @Test
    public void shouldGetAccessTypeRolesJurisdictionResults() {

        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();

        jurisdictionResults = Mockito.spy(new AccessTypeRolesJurisdictionResults());
        accessTypeJurisdictions = Mockito.spy(new ArrayList<>());
        organisationProfileIds.setOrganisationProfileIds(orgProfileIds);

        // for each jurisdiction build access type Roles
        accessTypeResult.setOrganisationProfileId("SOLICITOR_ORG");
        accessTypeResult.setAccessTypeId("AccessTypeId");
        accessTypeResult.setAccessMandatory(Boolean.TRUE);
        accessTypeResult.setAccessDefault(Boolean.TRUE);
        accessTypeResult.setDisplay(Boolean.TRUE);
        accessTypeResult.setDisplayOrder(10);
        accessTypeResult.setDescription("DESCRIPTION");
        accessTypeResult.setHint("Hint");

        accessTypeResults.add(accessTypeResult);
        accessTypeJurisdictionResult.setAccessTypes(accessTypeResults);

        accessTypeRoleResult.setGroupRoleName("NAME");
        accessTypeRoleResult.setCaseTypeId("CaseTypeID");
        accessTypeRoleResult.setOrganisationalRoleName("ORGROLENAME");
        accessTypeRoleResult.setCaseGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");

        accessTypeRoleResults.add(accessTypeRoleResult);
        accessTypeResult.setRoles(accessTypeRoleResults);

        accessTypeJurisdictionResult.setAccessTypes(accessTypeResults);
        accessTypeJurisdictions.add(accessTypeJurisdictionResult);
        jurisdictionResults.setJurisdictions(accessTypeJurisdictions);

        jurisdictionResults.getJurisdictions();
        verify(jurisdictionResults).getJurisdictions();

        assertThat(jurisdictionResults.getJurisdictions(), is(not(nullValue())));

    }
}
