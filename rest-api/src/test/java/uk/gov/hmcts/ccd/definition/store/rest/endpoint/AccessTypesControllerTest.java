package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapperImpl;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeField;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeJurisdictionResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeJurisdictionResults;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRoleField;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRoleResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.OrganisationProfileIds;
import uk.gov.hmcts.ccd.definition.store.rest.service.AccessTypesService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity.toCaseTypeLiteEntity;

@SpringBootTest(classes = {EntityToResponseDTOMapperImpl.class})
@ExtendWith(SpringExtension.class)
class AccessTypesControllerTest {

    private AccessTypesController controller;
    @Spy
    private  EntityToResponseDTOMapper entityToResponseDTOMapper = new EntityToResponseDTOMapperImpl();
    @MockitoBean
    private AccessTypesRepository accessTypesRepository;
    @MockitoBean
    private AccessTypeRolesRepository accessTypeRolesRepository;
    private MockMvc mockMvc;
    private final List<String> orgProfileIds = List.of(new String[]{"SOLICITOR_ORG", "SOLICITOR_ORG"});
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
    private AccessTypeRoleEntity accessTypeRoleEntity;
    @Mock
    private AccessTypeRoleEntity accessTypeRoleEntity1;
    @Mock
    private AccessTypeRoleEntity accessTypeRoleEntity2;
    @Mock
    private AccessTypeRoleEntity accessTypeRoleEntity3;
    @Mock
    private AccessTypeEntity accessTypeEntity;
    @Mock
    private AccessTypeEntity accessTypeEntity1;
    @Mock
    private AccessTypeEntity accessTypeEntity2;
    private AccessTypeJurisdictionResults jurisdictionResults;
    private List<AccessTypeJurisdictionResult> accessTypeRolesJurisdictions;

    private AccessTypesService accessTypesService;

    private final String retrieveAccessTypesURL = "/retrieve-access-types";

    @BeforeEach
    void setUp()  {
        openMocks(this);

        jurisdictionResults = Mockito.spy(new AccessTypeJurisdictionResults());
        accessTypeRolesJurisdictions = Mockito.spy(new  ArrayList<>());

        setUpAccessTypeData();

        accessTypesService = new AccessTypesService(entityToResponseDTOMapper,
            accessTypesRepository, accessTypeRolesRepository);
        this.controller = new AccessTypesController(accessTypesService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ControllerExceptionHandler())
            .build();
    }

    @DisplayName("Should set up the results that can be retrieved")
    @Test
    void getAccessTypeRolesJurisdictionResults() {

        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();
        organisationProfileIds.setOrganisationProfileIds(orgProfileIds);

        AccessTypeField accessTypeField = new AccessTypeField();

        accessTypeField.setAccessDefault(true);
        accessTypeField.setAccessMandatory(false);
        accessTypeField.setDescription("Test");
        accessTypeField.setHint("Test hint");
        accessTypeField.setAccessTypeId("AccessTypeId");
        accessTypeField.setDisplay(false);
        accessTypeField.setDisplayOrder(10);
        accessTypeField.setOrganisationProfileId("SOLICITOR_ORG");
        accessTypeField.setCaseTypeId(CASE_TYPE_REFERENCE);

        List<AccessTypeField> accessTypes = new ArrayList<>();
        accessTypes.add(accessTypeField);


        AccessTypeRoleField accessTypeRoleField = new AccessTypeRoleField();
        accessTypeRoleField.setGroupRoleName("NAME");
        accessTypeRoleField.setOrganisationalRoleName("ORGROLENAME");
        accessTypeRoleField.setCaseAccessGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");
        accessTypeRoleField.setCaseTypeId(CASE_TYPE_REFERENCE);
        accessTypeRoleField.setAccessTypeId("AccessTypeId");
        accessTypeRoleField.setOrganisationProfileId("SOLICITOR_ORG");
        accessTypeRoleField.setGroupAccessEnabled(true);

        // for each jurisdiction build access type Roles
        AccessTypeResult accessTypeResult = new AccessTypeResult();
        accessTypeResult.setOrganisationProfileId("SOLICITOR_ORG");
        accessTypeResult.setAccessTypeId("AccessTypeId");

        List<AccessTypeResult> accessTypeResults = new ArrayList<>();

        accessTypeResults.add(accessTypeResult);

        AccessTypeJurisdictionResult accessTypeRolesJurisdictionResult = new AccessTypeJurisdictionResult();
        accessTypeRolesJurisdictionResult.setAccessTypes(accessTypeResults);

        AccessTypeRoleResult accessTypeRoleResult = new AccessTypeRoleResult();
        accessTypeRoleResult.setGroupRoleName("NAME");
        accessTypeRoleResult.setCaseTypeId("CaseTypeidReference");
        accessTypeRoleResult.setOrganisationalRoleName("ORGROLENAME");
        accessTypeRoleResult.setCaseGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");

        List<AccessTypeRoleResult> accessTypeRoleResults = new ArrayList<>();
        accessTypeRoleResults.add(accessTypeRoleResult);

        accessTypeResult.setRoles(accessTypeRoleResults);

        accessTypeResults.add(accessTypeResult);
        accessTypeRolesJurisdictionResult.setAccessTypes(accessTypeResults);

        List<AccessTypeJurisdictionResult> accessTypeRolesJurisdictions = Mockito.spy(new  ArrayList<>());
        accessTypeResults.add(accessTypeResult);
        accessTypeRolesJurisdictionResult.setAccessTypes(accessTypeResults);
        accessTypeRolesJurisdictions.add(accessTypeRolesJurisdictionResult);
        jurisdictionResults.setJurisdictions(accessTypeRolesJurisdictions);

        doReturn(controller.retrieveAccessTypeRoles(organisationProfileIds)).when(accessTypeRolesJurisdictions).get(0);
        jurisdictionResults.getJurisdictions();
        verify(jurisdictionResults).getJurisdictions();
    }

    @DisplayName("Should post retrieve-access-types fail")
    @Test
    void shouldFailPostretrieve_access_types_Request() throws Exception {
        mockMvc.perform(post(retrieveAccessTypesURL)
                .contentType(TEXT_PLAIN)
                .content(""))
            .andExpect(status().is4xxClientError()).andReturn();
    }

    @DisplayName("Should post retrieve-access-types invalid request fail")
    @Test
    void shouldFailRetrieve_access_types_Invalid_Request() throws Exception {

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

        AccessTypeJurisdictionResults accessTypeJurisdictionResults =
            objmapper.readValue(mvcResult.getResponse().getContentAsString(),
            AccessTypeJurisdictionResults.class);

        assertFalse(
            accessTypeJurisdictionResults == null
            || accessTypeJurisdictionResults.getJurisdictions().isEmpty(),
            "accessTypeRolesJurisdictionResults is null or empty");

        AccessTypeJurisdictionResults finalAccessTypeJurisdictionResults = accessTypeJurisdictionResults;
        assertAll(
            () -> assertThat(finalAccessTypeJurisdictionResults.getJurisdictions().size(),
                is(2)),
            () -> greaterThan(finalAccessTypeJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypes().size()),
            () -> assertThat(finalAccessTypeJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypes().get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
        );

        AccessTypeJurisdictionResults jurisdictionResults =
            controller.retrieveAccessTypeRoles(organisationProfileIds);
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().size());
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().get(0).getAccessTypes().size());
        Assertions.assertEquals("BEFTA_MASTER", jurisdictionResults.getJurisdictions().get(0).getId());
        Assertions.assertEquals("BEFTA_MASTER2", jurisdictionResults.getJurisdictions().get(1).getId());

        checkJsonResultIsCorrect(finalAccessTypeJurisdictionResults, jurisdictionResults);
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

        AccessTypeJurisdictionResults accessTypeJurisdictionResults =
            objmapper.readValue(mvcResult.getResponse().getContentAsString(),
                AccessTypeJurisdictionResults.class);

        assertFalse(
            accessTypeJurisdictionResults == null
                || accessTypeJurisdictionResults.getJurisdictions().isEmpty(),
                "accessTypeRolesJurisdictionResults is null or empty");

        AccessTypeJurisdictionResults finalAccessTypeJurisdictionResults = accessTypeJurisdictionResults;
        assertAll(
            () -> assertThat(finalAccessTypeJurisdictionResults.getJurisdictions().size(),
                is(2)),
            () -> greaterThan(finalAccessTypeJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypes().size()),
            () -> assertThat(finalAccessTypeJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypes().get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
        );

        AccessTypeJurisdictionResults jurisdictionResults =
            controller.retrieveAccessTypeRoles(organisationProfileIds);
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().size());
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().get(0).getAccessTypes().size());
        Assertions.assertEquals("BEFTA_MASTER", jurisdictionResults.getJurisdictions().get(0).getId());
        Assertions.assertEquals("BEFTA_MASTER2", jurisdictionResults.getJurisdictions().get(1).getId());

        checkJsonResultIsCorrect(finalAccessTypeJurisdictionResults, jurisdictionResults);
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

        AccessTypeJurisdictionResults accessTypeJurisdictionResults =
            objmapper.readValue(mvcResult.getResponse().getContentAsString(),
                AccessTypeJurisdictionResults.class);

        assertFalse(
            accessTypeJurisdictionResults == null
                || accessTypeJurisdictionResults.getJurisdictions().isEmpty(),
                "accessTypeRolesJurisdictionResults is null or empty");

        AccessTypeJurisdictionResults finalAccessTypeJurisdictionResults = accessTypeJurisdictionResults;
        assertAll(
            () -> assertThat(finalAccessTypeJurisdictionResults.getJurisdictions().size(),
                is(2)),
            () -> greaterThan(finalAccessTypeJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypes().size()),
            () -> assertThat(finalAccessTypeJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypes().get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
        );

        AccessTypeJurisdictionResults jurisdictionResults =
            controller.retrieveAccessTypeRoles(organisationProfileIds);
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().size());
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().get(0).getAccessTypes().size());
        Assertions.assertEquals("BEFTA_MASTER", jurisdictionResults.getJurisdictions().get(0).getId());
        Assertions.assertEquals("BEFTA_MASTER2", jurisdictionResults.getJurisdictions().get(1).getId());

        checkJsonResultIsCorrect(finalAccessTypeJurisdictionResults, jurisdictionResults);
    }

    @Test
    void shouldHandleNull() throws Exception {

        ObjectMapper objmapper = new ObjectMapper();
        String request = objmapper.writeValueAsString(null);

        final MvcResult mvcResult = mockMvc.perform(post(retrieveAccessTypesURL)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isOk()).andReturn();

        Assertions.assertNotNull(mvcResult, "Response from post null");

        AccessTypeJurisdictionResults accessTypeJurisdictionResults =
            objmapper.readValue(mvcResult.getResponse().getContentAsString(),
                AccessTypeJurisdictionResults.class);

        assertFalse(
            accessTypeJurisdictionResults == null
                || accessTypeJurisdictionResults.getJurisdictions().isEmpty(),
                "accessTypeRolesJurisdictionResults is null or empty");

        AccessTypeJurisdictionResults finalAccessTypeJurisdictionResults = accessTypeJurisdictionResults;
        assertAll(
            () -> assertThat(finalAccessTypeJurisdictionResults.getJurisdictions().size(),
                is(2)),
            () -> greaterThan(finalAccessTypeJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypes().size()),
            () -> assertThat(finalAccessTypeJurisdictionResults.getJurisdictions().get(0)
                .getAccessTypes().get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
        );

        AccessTypeJurisdictionResults jurisdictionResults =
            controller.retrieveAccessTypeRoles(null);
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().size());
        Assertions.assertEquals(2, jurisdictionResults.getJurisdictions().get(0).getAccessTypes().size());
        Assertions.assertEquals(1, jurisdictionResults.getJurisdictions().get(1).getAccessTypes().size());
        Assertions.assertEquals(2,
            jurisdictionResults.getJurisdictions().get(0).getAccessTypes().get(0).getRoles().size());
        Assertions.assertEquals(1,
            jurisdictionResults.getJurisdictions().get(0).getAccessTypes().get(1).getRoles().size());
        Assertions.assertEquals(1,
            jurisdictionResults.getJurisdictions().get(1).getAccessTypes().get(0).getRoles().size());
        Assertions.assertEquals("BEFTA_MASTER", jurisdictionResults.getJurisdictions().get(0).getId());
        Assertions.assertEquals("BEFTA_MASTER2", jurisdictionResults.getJurisdictions().get(1).getId());

        checkJsonResultIsCorrect(finalAccessTypeJurisdictionResults, jurisdictionResults);
    }

    private void setUpAccessTypeData() {

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

        //add access types
        setupAccessTypeRolesEntity(accessTypeEntity, toCaseTypeLiteEntity(caseTypeEntity),
            "BEFTA_SOLICITOR_1", "SOLICITOR_ORG");
        setupAccessTypeRolesEntity(accessTypeEntity1, toCaseTypeLiteEntity(caseTypeEntity),
            "BEFTA_SOLICITOR_2", "OGD_DWP_PROFILE");
        setupAccessTypeRolesEntity(accessTypeEntity2, toCaseTypeLiteEntity(caseTypeEntity2),
            "BEFTA_SOLICITOR_2", "OGD_DWP_PROFILE2");

        //add access type roles
        setupAccessTypeRoleEntity(accessTypeRoleEntity, toCaseTypeLiteEntity(caseTypeEntity),
            "BEFTA_SOLICITOR_1", "SOLICITOR_ORG", "civil-solicitor");
        setupAccessTypeRoleEntity(accessTypeRoleEntity1, toCaseTypeLiteEntity(caseTypeEntity),
            "BEFTA_SOLICITOR_2", "OGD_DWP_PROFILE", "civil-solicitor");
        setupAccessTypeRoleEntity(accessTypeRoleEntity2, toCaseTypeLiteEntity(caseTypeEntity2),
            "BEFTA_SOLICITOR_2", "OGD_DWP_PROFILE2", "civil-solicitor");
        setupAccessTypeRoleEntity(accessTypeRoleEntity3, toCaseTypeLiteEntity(caseTypeEntity),
            "BEFTA_SOLICITOR_1", "SOLICITOR_ORG", "civil-solicitor");

        List<AccessTypeRoleEntity> result = new ArrayList<>();
        result.add(accessTypeRoleEntity);
        result.add(accessTypeRoleEntity1);
        result.add(accessTypeRoleEntity2);
        result.add(accessTypeRoleEntity3);

        List<AccessTypeEntity> resultAccessTypes = new ArrayList<>();
        resultAccessTypes.add(accessTypeEntity);
        resultAccessTypes.add(accessTypeEntity1);
        resultAccessTypes.add(accessTypeEntity2);

        when(accessTypeRolesRepository.findByOrganisationProfileIds(orgProfileIds)).thenReturn(result);
        when(accessTypeRolesRepository.findAllWithCaseTypeIds()).thenReturn(result);

        when(accessTypesRepository.findByOrganisationProfileIds(orgProfileIds)).thenReturn(resultAccessTypes);
        when(accessTypesRepository.findAllWithCaseTypeIds()).thenReturn(resultAccessTypes);

    }

    private void setupAccessTypeRolesEntity(AccessTypeEntity accessTypeEntity, CaseTypeLiteEntity caseType,
                                            String accessTypeId, String orgProfileId) {

        accessTypeEntity.setCaseType(caseType);
        when(accessTypeEntity.getCaseType()).thenReturn(caseType);
        accessTypeEntity.setAccessTypeId("default");
        when(accessTypeEntity.getAccessTypeId()).thenReturn(accessTypeId);
        accessTypeEntity.setAccessMandatory(true);
        accessTypeEntity.setAccessDefault(true);
        accessTypeEntity.setDisplay(true);
        accessTypeEntity.setOrganisationProfileId(orgProfileId);
        when(accessTypeEntity.getOrganisationProfileId()).thenReturn(orgProfileId);
        accessTypeEntity.setDescription("ACCESS CASES");
        accessTypeEntity.setLiveFrom(LocalDate.now());
        accessTypeEntity.setHint("Hint:User can work with all civil cases without needing to be "
            + "assigned to each case");
        accessTypeEntity.setDescription("Description:User can work with all civil cases without needing "
            + "to be assigned to each case");
        accessTypeEntity.setDisplayOrder(1);

        entityToResponseDTOMapper.map(accessTypeEntity);

    }

    private void setupAccessTypeRoleEntity(AccessTypeRoleEntity accessTypeRoleEntity, CaseTypeLiteEntity caseType,
                                           String accessTypeId, String orgProfileId, String roleName) {

        accessTypeRoleEntity.setCaseType(caseType);
        when(accessTypeRoleEntity.getCaseType()).thenReturn(caseType);
        accessTypeRoleEntity.setAccessTypeId("default");
        when(accessTypeRoleEntity.getAccessTypeId()).thenReturn(accessTypeId);
        accessTypeRoleEntity.setGroupAccessEnabled(true);
        accessTypeRoleEntity.setOrganisationProfileId(orgProfileId);
        when(accessTypeRoleEntity.getOrganisationProfileId()).thenReturn(orgProfileId);
        accessTypeRoleEntity.setOrganisationalRoleName(roleName);
        accessTypeRoleEntity.setLiveFrom(LocalDate.now());
        accessTypeRoleEntity.setGroupRoleName("[APPLICANTSOLICITORONE]");
        accessTypeRoleEntity.setCaseAssignedRoleField("applicant1OrganisationPolicy");
        accessTypeRoleEntity.setCaseAccessGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");

        entityToResponseDTOMapper.map(accessTypeRoleEntity);

    }


    private void checkJsonResultIsCorrect(AccessTypeJurisdictionResults expectedAccessTypeRolesJurisdictions,
                                          AccessTypeJurisdictionResults actualAccessTypeRolesJurisdictions)
        throws JsonProcessingException {
        ObjectMapper objectMapper  = new ObjectMapper();
        String actualJsonString = objectMapper.writeValueAsString(actualAccessTypeRolesJurisdictions);
        String expectedJsonString = objectMapper.writeValueAsString(expectedAccessTypeRolesJurisdictions);

        assertAll(
            () -> JSONAssert.assertEquals(expectedJsonString, actualJsonString, JSONCompareMode.LENIENT)
        );
    }
}
