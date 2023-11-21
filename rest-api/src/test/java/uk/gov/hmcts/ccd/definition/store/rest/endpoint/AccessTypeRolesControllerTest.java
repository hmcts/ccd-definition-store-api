package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
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
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesResult;
import uk.gov.hmcts.ccd.definition.store.rest.service.AccessTypeRolesService;

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
    private final List<String> orgProfileIds = List.of(new String[]{"SOLICITOR_ORG", "OGD_DWP_PROFILE"});
    @Mock
    private JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
    @Mock
    private JurisdictionEntity jurisdictionEntity2 = new JurisdictionEntity();
    @Mock
    private  CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
    @Mock
    private  CaseTypeEntity caseTypeEntity2 = new CaseTypeEntity();
    private static final String CASE_TYPE_REFERENCE = "get-test";
    private static final String JURISDICTION = "BEFTA_MASTER";
    private static final String JURISDICTION2 = "BEFTA_MASTER2";
    @Mock
    private AccessTypeRolesEntity accessTypeRolesEntity;
    @Mock
    private AccessTypeRolesEntity accessTypeRolesEntity1;
    @Mock
    private AccessTypeRolesEntity accessTypeRolesEntity2;
    private AccessTypeRolesJurisdictionResults jurisdictionResults;

    private AccessTypeRolesService accessTypeRolesService;

    private final String retrieveAccessTypesURL = "/retrieve-access-types";

    @BeforeEach
    void setUp()  {
        openMocks(this);

        jurisdictionResults = Mockito.spy(new AccessTypeRolesJurisdictionResults());

        setUpAccessTypeRoleData();

        accessTypeRolesService = new AccessTypeRolesService(entityToResponseDTOMapper,
            accessTypeRolesRepository);
        this.controller = new AccessTypeRolesController(accessTypeRolesService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ControllerExceptionHandler())
            .build();
    }

    @DisplayName("Should set up the results that can be retreived")
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

        List<AccessTypeRolesJurisdictionResult> accessTypeRolesJurisdictions = Mockito.spy(new  ArrayList<>());
        accessTypeRolesJurisdictions.add(accessTypeRolesJurisdictionResult);
        jurisdictionResults.setJurisdictions(accessTypeRolesJurisdictions);

        doReturn(controller.retrieveAccessTypeRoles(organisationProfileIds)).when(accessTypeRolesJurisdictions).get(0);
        List<AccessTypeRolesJurisdictionResult> results = jurisdictionResults.getJurisdictions();
        verify(jurisdictionResults).getJurisdictions();
    }

    @DisplayName("Should post retrieve-access-types fail")
    @Test
    public void shouldFailPostretrieve_access_types_Request() throws Exception {
        mockMvc.perform(post(retrieveAccessTypesURL)
                .contentType(TEXT_PLAIN)
                .content(""))
            .andExpect(status().is4xxClientError()).andReturn();
    }

    @DisplayName("Should post retrieve-access-types invalid request fail")
    @Test
    public void shouldFailRetrieve_access_types_Invalid_Request() throws Exception {

        ObjectMapper objmapper = new ObjectMapper();
        String request = objmapper.writeValueAsString("{\"organisation_profile_ids\": [\"sdsads\",]}");

        mockMvc.perform(post(retrieveAccessTypesURL)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isBadRequest()).andReturn();
    }

    @DisplayName("should return AccessTypeRolesField List depending on OrganisationProfileIds")
    @Test
    void shouldHandleACollection() throws Exception {
        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();
        organisationProfileIds.setOrganisationProfileIds(orgProfileIds);

        ObjectMapper objmapper = new ObjectMapper();
        String request = objmapper.writeValueAsString(organisationProfileIds);

        final MvcResult mvcResult = mockMvc.perform(post(retrieveAccessTypesURL)
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
                is(2)),
            () -> greaterThan(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().size()),
            () -> MatcherAssert.assertThat(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
        );

        AccessTypeRolesJurisdictionResults jurisdictionResults =
            controller.retrieveAccessTypeRoles(organisationProfileIds);
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().size());
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().get(0).getAccessTypeRoles().size());
        Assertions.assertEquals("BEFTA_MASTER", jurisdictionResults.getJurisdictions().get(0).getId());
        Assertions.assertEquals("BEFTA_MASTER2", jurisdictionResults.getJurisdictions().get(1).getId());

        checkJsonResultIsCorrect(finalAccessTypeRolesJurisdictionResults, jurisdictionResults);
    }

    @DisplayName("should return AccessTypeRolesField List when OrganisationProfileIds not specified")
    @Test
    void shouldHandleOrganisationProfileIdsNotSpecified() throws Exception {
        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();

        ObjectMapper objmapper = new ObjectMapper();
        String request = objmapper.writeValueAsString(organisationProfileIds);

        final MvcResult mvcResult = mockMvc.perform(post(retrieveAccessTypesURL)
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
                is(2)),
            () -> greaterThan(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().size()),
            () -> MatcherAssert.assertThat(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
        );

        AccessTypeRolesJurisdictionResults jurisdictionResults =
            controller.retrieveAccessTypeRoles(organisationProfileIds);
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().size());
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().get(0).getAccessTypeRoles().size());
        Assertions.assertEquals("BEFTA_MASTER", jurisdictionResults.getJurisdictions().get(0).getId());
        Assertions.assertEquals("BEFTA_MASTER2", jurisdictionResults.getJurisdictions().get(1).getId());

        checkJsonResultIsCorrect(finalAccessTypeRolesJurisdictionResults, jurisdictionResults);
    }

    @DisplayName("should return AccessTypeRolesField List when OrganisationProfileIds is specified but is empty")
    @Test
    void shouldHandleEmptyOrganisationProfileIds() throws Exception {
        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();
        organisationProfileIds.setOrganisationProfileIds(List.of(new String[]{}));

        ObjectMapper objmapper = new ObjectMapper();
        String request = objmapper.writeValueAsString(organisationProfileIds);

        final MvcResult mvcResult = mockMvc.perform(post(retrieveAccessTypesURL)
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
                is(2)),
            () -> greaterThan(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().size()),
            () -> MatcherAssert.assertThat(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
        );

        AccessTypeRolesJurisdictionResults jurisdictionResults =
            controller.retrieveAccessTypeRoles(organisationProfileIds);
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().size());
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().get(0).getAccessTypeRoles().size());
        Assertions.assertEquals("BEFTA_MASTER", jurisdictionResults.getJurisdictions().get(0).getId());
        Assertions.assertEquals("BEFTA_MASTER2", jurisdictionResults.getJurisdictions().get(1).getId());

        checkJsonResultIsCorrect(finalAccessTypeRolesJurisdictionResults, jurisdictionResults);
    }

    @DisplayName("should return AccessTypeRolesField List when request body is not specified")
    @Test
    void shouldHandleNullRequestBody() throws Exception {

        ObjectMapper objmapper = new ObjectMapper();
        String request = objmapper.writeValueAsString(null);

        final MvcResult mvcResult = mockMvc.perform(post(retrieveAccessTypesURL)
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
                is(2)),
            () -> greaterThan(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().size()),
            () -> MatcherAssert.assertThat(finalAccessTypeRolesJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypeRoles().get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
        );

        AccessTypeRolesJurisdictionResults jurisdictionResults =
            controller.retrieveAccessTypeRoles(null);
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().size());
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().get(0).getAccessTypeRoles().size());
        Assertions.assertEquals("BEFTA_MASTER", jurisdictionResults.getJurisdictions().get(0).getId());
        Assertions.assertEquals("BEFTA_MASTER2", jurisdictionResults.getJurisdictions().get(1).getId());

        checkJsonResultIsCorrect(finalAccessTypeRolesJurisdictionResults, jurisdictionResults);
    }

    private void  setUpAccessTypeRoleData() {

        //setup case type 1
        when(caseTypeEntity.getReference()).thenReturn(CASE_TYPE_REFERENCE);
        when(jurisdictionEntity.getReference()).thenReturn(JURISDICTION);
        when(jurisdictionEntity.getId()).thenReturn(1);
        when(caseTypeEntity.getJurisdiction()).thenReturn(jurisdictionEntity);

        //setup case type 2 with same case tyoe as 1 but with a different jurisdiction
        when(caseTypeEntity2.getReference()).thenReturn(CASE_TYPE_REFERENCE);
        when(jurisdictionEntity2.getReference()).thenReturn(JURISDICTION2);
        when(jurisdictionEntity2.getId()).thenReturn(2);
        when(caseTypeEntity2.getJurisdiction()).thenReturn(jurisdictionEntity2);

        setupAccessTypeRolesEntity(accessTypeRolesEntity, caseTypeEntity);
        setupAccessTypeRolesEntity(accessTypeRolesEntity1, caseTypeEntity);
        setupAccessTypeRolesEntity(accessTypeRolesEntity2, caseTypeEntity2);

        List<AccessTypeRolesEntity> result = new ArrayList<>();

        result.add(accessTypeRolesEntity);
        result.add(accessTypeRolesEntity1);
        result.add(accessTypeRolesEntity2);

        when(accessTypeRolesRepository.findByOrganisationProfileIds(orgProfileIds)).thenReturn(result);
        when(accessTypeRolesRepository.findAllWithCaseTypeIds()).thenReturn(result);

    }

    private void setupAccessTypeRolesEntity(AccessTypeRolesEntity accessTypeRolesEntity, CaseTypeEntity caseTypeId) {

        accessTypeRolesEntity.setCaseTypeId(caseTypeId);
        when(accessTypeRolesEntity.getCaseTypeId()).thenReturn(caseTypeId);
        accessTypeRolesEntity.setAccessTypeId("default");
        accessTypeRolesEntity.setAccessMandatory(true);
        accessTypeRolesEntity.setAccessDefault(true);
        accessTypeRolesEntity.setGroupAccessEnabled(true);
        accessTypeRolesEntity.setDisplay(true);
        accessTypeRolesEntity.setOrganisationProfileId("SOLICITOR_ORG");
        when(accessTypeRolesEntity.getOrganisationProfileId()).thenReturn("SOLICITOR_ORG");
        accessTypeRolesEntity.setDescription("ACCESS CASES");
        accessTypeRolesEntity.setOrganisationalRoleName("civil-solicitor");
        accessTypeRolesEntity.setLiveFrom(LocalDate.now());
        accessTypeRolesEntity.setHint("Hint:User can work with all civil cases without needing to be "
            + "assigned to each case");
        accessTypeRolesEntity.setDescription("Description:User can work with all civil cases without needing "
            + "to be assigned to each case");
        accessTypeRolesEntity.setDisplayOrder(1);
        accessTypeRolesEntity.setGroupRoleName("[APPLICANTSOLICITORROLE]");
        accessTypeRolesEntity.setCaseAssignedRoleField("applicant1OrganisationPolicy");
        accessTypeRolesEntity.setCaseAccessGroupIdTemplate("BEFTA_MASTER:CIVIL:all:CIVIL:AS1:$ORGID$");

        entityToResponseDTOMapper.map(accessTypeRolesEntity);

    }

    private void checkJsonResultIsCorrect(AccessTypeRolesJurisdictionResults expectedAccessTypeRolesJurisdictions,
                                          AccessTypeRolesJurisdictionResults actualAccessTypeRolesJurisdictions)
        throws JsonProcessingException {
        ObjectMapper objectMapper  = new ObjectMapper();
        String actualJsonString = objectMapper.writeValueAsString(actualAccessTypeRolesJurisdictions);
        String expectedJsonString = objectMapper.writeValueAsString(expectedAccessTypeRolesJurisdictions);

        assertAll(
            () -> JSONAssert.assertEquals(expectedJsonString, actualJsonString, JSONCompareMode.LENIENT)
        );
    }
}
