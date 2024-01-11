package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapperImpl;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.OrganisationProfileIds;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesRoleResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResults;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesField;
import uk.gov.hmcts.ccd.definition.store.repository.model.Version;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {EntityToResponseDTOMapperImpl.class})
@ExtendWith(SpringExtension.class)
public class AccessTypeRolesControllerTest {

    private AccessTypeRolesController controller;

    @Spy
    private  EntityToResponseDTOMapper entityToResponseDTOMapper = new EntityToResponseDTOMapperImpl();

    @MockBean
    private AccessTypeRolesRepository accessTypeRolesRepository;

    private MockMvc mockMvc;

    private final List<String> orgProfileIds = List.of(new String[]{"SOLICITOR_ORG", "SOLICITOR_ORG"});

    @Mock
    private JurisdictionEntity jurisdiction = new JurisdictionEntity();
    @Mock
    private  CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
    @Mock
    private  CaseTypeEntity caseTypeEntity1 = new CaseTypeEntity();
    @Mock
    private CaseTypeEntity caseTypeEntity2 = new CaseTypeEntity();

    private static final String CASE_TYPE_REFERENCE = "get-test";
    private static final String CASE_TYPE_REFERENCE_1 = "get-test1";
    private static final String CASE_TYPE_REFERENCE_2 = "get-test2";
    @Mock
    private AccessTypeRolesEntity accessTypeRolesEntity;

    @Mock
    private AccessTypeRolesEntity accessTypeRolesEntity1;
    @Mock
    private AccessTypeRolesEntity accessTypeRolesEntity2;

    private AccessTypeRolesJurisdictionResults jurisdictionResults;
    private List<AccessTypeRolesJurisdictionResult> accessTypeRolesJurisdictions;

    @BeforeEach
    void setUp()  {
        openMocks(this);

        jurisdictionResults = Mockito.spy(new AccessTypeRolesJurisdictionResults());
        accessTypeRolesJurisdictions = Mockito.spy(new  ArrayList<>());

        setUpAccessTypeRoleData();

        controller = new AccessTypeRolesController(entityToResponseDTOMapper, accessTypeRolesRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ControllerExceptionHandler())
            .build();
    }

    @Test
    public void getAccessTypeRolesJurisdictionResults() {
        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();
        organisationProfileIds.setOrganisationProfileIds(orgProfileIds);

        AccessTypeRolesResult  accessTypeRolesResult = new AccessTypeRolesResult();

        // for each jurisdiction build access type Roles
        accessTypeRolesResult.setOrganisationProfileId("SOLICITOR_ORG");
        accessTypeRolesResult.setAccessTypeId("AccessTypeId");
        accessTypeRolesResult.setAccessMandatory(Boolean.TRUE);
        accessTypeRolesResult.setAccessDefault(Boolean.TRUE);
        accessTypeRolesResult.setDisplay(Boolean.TRUE);
        accessTypeRolesResult.setDisplayOrder(10);
        accessTypeRolesResult.setDescription("DESCRIPTION");
        accessTypeRolesResult.setHint("Hint");

        List<AccessTypeRolesResult> accessTypeRolesResults = new ArrayList<>();

        accessTypeRolesResults.add(accessTypeRolesResult);

        AccessTypeRolesJurisdictionResult accessTypeRolesJurisdictionResult = new AccessTypeRolesJurisdictionResult();
        accessTypeRolesJurisdictionResult.setAccessTypeRoles(accessTypeRolesResults);

        AccessTypeRolesRoleResult accessTypeRolesRoleResult = new AccessTypeRolesRoleResult();

        accessTypeRolesRoleResult.setGroupRoleName("NAME");
        accessTypeRolesRoleResult.setCaseTypeId("CaseTypeidReference");
        accessTypeRolesRoleResult.setOrganisationalRoleName("ORGROLENAME");
        accessTypeRolesRoleResult.setCaseGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");

        List<AccessTypeRolesRoleResult> accessTypeRolesRoleResults = new ArrayList<>();

        accessTypeRolesRoleResults.add(accessTypeRolesRoleResult);

        accessTypeRolesResult.setRoles(accessTypeRolesRoleResults);

        accessTypeRolesResults.add(accessTypeRolesResult);
        accessTypeRolesJurisdictionResult.setAccessTypeRoles(accessTypeRolesResults);
        accessTypeRolesJurisdictions.add(accessTypeRolesJurisdictionResult);
        jurisdictionResults.setJurisdictions(accessTypeRolesJurisdictions);

        doReturn(controller.retrieveAccessTypeRoles(organisationProfileIds)).when(accessTypeRolesJurisdictions).get(0);
        List<AccessTypeRolesJurisdictionResult> results = jurisdictionResults.getJurisdictions();
        verify(jurisdictionResults).getJurisdictions();
    }

    @DisplayName("Should post retrieve-access-types fail")
    @Test
    public void shouldFailPostretrieve_access_types_Request() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/api/retrieve-access-types")
                .contentType(TEXT_PLAIN)
                .content(""))
            .andExpect(status().is4xxClientError()).andReturn();
    }

    @DisplayName("should return AccessTypeRolesField List depending on OrganisationProfileIds")
    @Test
    void shouldHandleACollection() throws Exception {
        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();
        organisationProfileIds.setOrganisationProfileIds(orgProfileIds);

        ObjectMapper objmapper = new ObjectMapper();
        String response = objmapper.writeValueAsString(new AccessTypeRolesField());
        String request = objmapper.writeValueAsString(organisationProfileIds);

        final MvcResult mvcResult = mockMvc.perform(post("/api/retrieve-access-types")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isOk()).andReturn();

        Assertions.assertNotNull(mvcResult, "Response from post null");

        AccessTypeRolesJurisdictionResults accessTypeRolesJurisdictionResults =
            objmapper.readValue(mvcResult.getResponse().getContentAsString(),
            AccessTypeRolesJurisdictionResults.class);

        assertFalse("accessTypeRolesJurisdictionResults is null or empty",
            accessTypeRolesJurisdictionResults == null
            || accessTypeRolesJurisdictionResults.getJurisdictions().isEmpty());

        AccessTypeRolesJurisdictionResults finalAccessTypeRolesJurisdictionResults = accessTypeRolesJurisdictionResults;
        assertAll(
            () -> MatcherAssert.assertThat(finalAccessTypeRolesJurisdictionResults.getJurisdictions().size(),
                is(4)),
            () -> greaterThan(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().size()),
            () -> MatcherAssert.assertThat(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
        );

        AccessTypeRolesJurisdictionResults jurisdictionResults =
            controller.retrieveAccessTypeRoles(organisationProfileIds);
        Assertions.assertEquals(4, jurisdictionResults.getJurisdictions().size());
    }

    @DisplayName("should return AccessTypeRolesField List when OrganisationProfileIds not specified")
    @Test
    void shouldHandleEmptyOrganisationProfileIds() throws Exception {
        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();

        ObjectMapper objmapper = new ObjectMapper();
        String response = objmapper.writeValueAsString(new AccessTypeRolesField());
        String request = objmapper.writeValueAsString(organisationProfileIds);

        final MvcResult mvcResult = mockMvc.perform(post("/api/retrieve-access-types")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isOk()).andReturn();

        Assertions.assertNotNull(mvcResult, "Response from post null");

        AccessTypeRolesJurisdictionResults accessTypeRolesJurisdictionResults =
            objmapper.readValue(mvcResult.getResponse().getContentAsString(),
                AccessTypeRolesJurisdictionResults.class);

        assertFalse("accessTypeRolesJurisdictionResults is null or empty",
            accessTypeRolesJurisdictionResults == null
                || accessTypeRolesJurisdictionResults.getJurisdictions().isEmpty());

        AccessTypeRolesJurisdictionResults finalAccessTypeRolesJurisdictionResults = accessTypeRolesJurisdictionResults;
        assertAll(
            () -> MatcherAssert.assertThat(finalAccessTypeRolesJurisdictionResults.getJurisdictions().size(),
                is(4)),
            () -> greaterThan(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().size()),
            () -> MatcherAssert.assertThat(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
        );

        AccessTypeRolesJurisdictionResults jurisdictionResults =
            controller.retrieveAccessTypeRoles(organisationProfileIds);
        Assertions.assertEquals(4, jurisdictionResults.getJurisdictions().size());
    }

    private CaseTypeEntity createCaseType(String caseTypeId, String jurisdictionPostfix) {

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setName("test-" + jurisdictionPostfix);

        Version version = new Version();

        caseTypeEntity.setVersion(version.getNumber());
        caseTypeEntity.setJurisdiction(jurisdiction);
        caseTypeEntity.setName(caseTypeId);
        return caseTypeEntity;
    }

    private void setupAccessTypeRolesEntity(AccessTypeRolesEntity accessTypeRolesEntity, String caseTypeId,
                                            String organisationProfileId) {

        //String caseTypeId = "get-test";
        CaseTypeEntity caseTypeReturned = createCaseType(caseTypeId, caseTypeId);

        //accessTypeRolesField = createAccessTypeRolesField(caseTypeId );
        accessTypeRolesEntity.setCaseTypeId(caseTypeReturned);
        accessTypeRolesEntity.setAccessTypeId("default");
        accessTypeRolesEntity.setAccessMandatory(true);
        accessTypeRolesEntity.setAccessDefault(true);
        accessTypeRolesEntity.setGroupAccessEnabled(true);
        accessTypeRolesEntity.setDisplay(true);
        accessTypeRolesEntity.setOrganisationProfileId(organisationProfileId);
        accessTypeRolesEntity.setDescription("ACCESS CASES");
        accessTypeRolesEntity.setOrganisationalRoleName("civil-solicitor");
        accessTypeRolesEntity.setLiveFrom(LocalDate.now());
        accessTypeRolesEntity.setHint("Hint:User can work with all civil cases without needing to be "
            + "assigned to each case");
        accessTypeRolesEntity.setDescription("Description:User can work with all civil cases without needing "
            + "to be assigned to each case");
        accessTypeRolesEntity.setDisplayOrder(1);
        accessTypeRolesEntity.setGroupRoleName("[APPLICANTSOLICITORONE]");
        accessTypeRolesEntity.setCaseAssignedRoleField("applicant1OrganisationPolicy");
        accessTypeRolesEntity.setCaseAccessGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");

        AccessTypeRolesField accessTypeRolesField;

        accessTypeRolesField = entityToResponseDTOMapper.map(accessTypeRolesEntity);

    }

    void setUpAccessTypeRoleData() {

        String caseTypeId = CASE_TYPE_REFERENCE;//"get-test";
        setupAccessTypeRolesEntity(accessTypeRolesEntity, caseTypeId, "SOLICITOR_ORG");

        caseTypeId = CASE_TYPE_REFERENCE_1;//"get-test1";
        setupAccessTypeRolesEntity(accessTypeRolesEntity1, caseTypeId, "SOLICITOR_ORG");

        caseTypeId = CASE_TYPE_REFERENCE_2;//"get-test2";
        setupAccessTypeRolesEntity(accessTypeRolesEntity1, caseTypeId, "SOLICITOR_ORG");

        when(accessTypeRolesEntity.getCaseTypeId()).thenReturn(caseTypeEntity);
        when(caseTypeEntity.getJurisdiction()).thenReturn(jurisdiction);
        when(caseTypeEntity.getJurisdiction().getId()).thenReturn(1);
        when(accessTypeRolesEntity.getOrganisationProfileId()).thenReturn("SOLICITOR_ORG");
        when(accessTypeRolesEntity.getAccessTypeId()).thenReturn("default");

        List<AccessTypeRolesEntity> result = new ArrayList<>();

        result.add(accessTypeRolesEntity);

        when(accessTypeRolesEntity1.getCaseTypeId()).thenReturn(caseTypeEntity1);
        when(caseTypeEntity1.getJurisdiction()).thenReturn(jurisdiction);
        when(caseTypeEntity1.getJurisdiction().getId()).thenReturn(1);
        when(accessTypeRolesEntity1.getOrganisationProfileId()).thenReturn("SOLICITOR_ORG");
        when(accessTypeRolesEntity1.getAccessTypeId()).thenReturn("default");
        result.add(accessTypeRolesEntity1);

        when(accessTypeRolesEntity2.getCaseTypeId()).thenReturn(caseTypeEntity2);
        when(caseTypeEntity2.getJurisdiction()).thenReturn(jurisdiction);
        when(caseTypeEntity2.getJurisdiction().getId()).thenReturn(1);
        when(accessTypeRolesEntity2.getOrganisationProfileId()).thenReturn("SOLICITOR_ORG");
        when(accessTypeRolesEntity2.getAccessTypeId()).thenReturn("default");
        result.add(accessTypeRolesEntity2);

        result.add(accessTypeRolesEntity);
        when(accessTypeRolesRepository.findByOrganisationProfileIds(orgProfileIds)).thenReturn(result);

        when(accessTypeRolesRepository.findAllWithCaseTypeIds()).thenReturn(result);
    }
}
