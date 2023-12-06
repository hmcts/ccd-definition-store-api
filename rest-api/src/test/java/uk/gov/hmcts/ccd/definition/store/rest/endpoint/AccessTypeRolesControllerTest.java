package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.domain.service.accesstyperoles.AccessTypeRolesService;

import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


public class AccessTypeRolesControllerTest {

    private static final String URL = "https://api/retrieve-access-types";

    private AccessTypeRolesController controller;
    @Mock
    private AccessTypeRolesService accessTypeRolesService;
    @Mock
    private EntityToResponseDTOMapper entityToResponseDTOMapper;
    @Mock
    private AccessTypeRolesRepository accessTypeRolesRepository;


    private MockMvc mockMvc;
    /*
        @Mock
        private DefinitionService definitionService;

        private Definition definition;

        private Definition def2;

        private Definition def3;
    */
    List<String> orgProfileIds = List.of(new String[]{"organisationProfileId_1", "organisationProfileId_2"});

    private OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();

    private AccessTypeRolesEntity accessTypeRolesEntity = new AccessTypeRolesEntity();
    private AccessTypeRolesField accessTypeRolesField = new AccessTypeRolesField();

    private AccessTypeRolesEntity accessTypeRolesEntity1 = new AccessTypeRolesEntity();
    private AccessTypeRolesField accessTypeRolesField1 = new AccessTypeRolesField();

    private AccessTypeRolesEntity accessTypeRolesEntity2 = new AccessTypeRolesEntity();
    private AccessTypeRolesField accessTypeRolesField2 = new AccessTypeRolesField();

    @BeforeEach
    void setUp() throws IOException {
        initMocks(this);

/*
        () -> assertThat(accessTypeRolesEntity.getCaseTypeId().getReference(), is(CASE_TYPE_ID_1)),
            () -> assertThat(accessTypeRolesEntity.getAccessTypeId(), is("access id")),
            () -> assertThat(accessTypeRolesEntity.getOrganisationProfileId(), is("ProfileID")),
            () -> assertThat(accessTypeRolesEntity.getDescription(), is(ACCESSTYPEROLES_DESCRIPTION)),
            () -> assertThat(accessTypeRolesEntity.getHint(), is("Hint")),
            () -> assertThat(accessTypeRolesEntity.getDisplayOrder(), is(1)),
            () -> assertThat(accessTypeRolesEntity.getOrganisationalRoleName(), is("Name")),
            () -> assertThat(accessTypeRolesEntity.getGroupRoleName(), is("Group role name")),
            () -> assertThat(accessTypeRolesEntity.getOrganisationPolicyField(), is("Policy Field Name")),
            () -> assertThat(accessTypeRolesEntity.getCaseAccessGroupIdTemplate(), is("Case Group ID Template")),
            () -> assertThat(accessTypeRolesEntity.getLiveFrom(), is(LocalDate.of(2023, Month.FEBRUARY, 12))),
*/

        String caseTypeId = "get-test";
        accessTypeRolesEntity = createAccessTypeRolesEntity(caseTypeId, "SOLICITOR_ORG");
        List<AccessTypeRolesEntity> entityList = new ArrayList<>();
        entityList.add(accessTypeRolesEntity);

        caseTypeId = "get-test1";
        accessTypeRolesEntity1 = createAccessTypeRolesEntity(caseTypeId, "organisationProfileId_1");
        entityList.add(accessTypeRolesEntity1);

        caseTypeId = "get-test2";
        accessTypeRolesEntity2 = createAccessTypeRolesEntity(caseTypeId, "organisationProfileId_2");
        entityList.add(accessTypeRolesEntity2);

        accessTypeRolesService.saveAll(entityList);

        doReturn(orgProfileIds)
            .when(accessTypeRolesRepository)
            .findByOrganisationProfileIds(orgProfileIds);
        doReturn(accessTypeRolesField).when(entityToResponseDTOMapper).map(accessTypeRolesEntity);
        doReturn(accessTypeRolesField2).when(entityToResponseDTOMapper).map(accessTypeRolesEntity2);
        doReturn(accessTypeRolesField2).when(entityToResponseDTOMapper).map(accessTypeRolesEntity2);

        controller = new AccessTypeRolesController(entityToResponseDTOMapper, accessTypeRolesRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ControllerExceptionHandler())
            .build();
    }

    @DisplayName("Should post retrieve-access-types fail")
    @Test
    public void shouldFailPostretrieve_access_types_Request() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/api/retrieve-access-types")
                .contentType(TEXT_PLAIN)
                .content(""))
            .andExpect(status().is4xxClientError()).andReturn();
    }

    @Test
    @DisplayName("Should handle empty accessTypeRoles collection")
    void shouldHandleEmptyCollection() throws Exception {
        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();
        List<AccessTypeRolesField> expected = new ArrayList<>();

        ObjectMapper objmapper = new ObjectMapper();
        final MvcResult mvcResult = mockMvc.perform(post("/api/retrieve-access-types")
                .contentType(APPLICATION_JSON)
                .content(objmapper.writeValueAsString(organisationProfileIds)))
            .andExpect(status().isOk()).andReturn();

        assertThat(mvcResult.getResponse().getContentAsString(), is("[]"));

        List<AccessTypeRolesField> result = controller.getAccessTypeRoles(organisationProfileIds);
        assertEquals(expected, result);
    }

    @DisplayName("should return AccessTypeRolesField List")
    @Test
    void findByOrganisationProfileIds() throws Exception {
        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();
        organisationProfileIds.setOrganisationProfileIds(orgProfileIds);

        List<AccessTypeRolesField> expected = new ArrayList<>();

        ObjectMapper objmapper = new ObjectMapper();
        final MvcResult mvcResult = mockMvc.perform(post("/api/retrieve-access-types")
                .contentType(APPLICATION_JSON)
                .content(objmapper.writeValueAsString(organisationProfileIds)))
            .andExpect(status().isOk()).andReturn();

        assertThat(mvcResult.getResponse().getContentAsString(), is("[]"));
        assertAll(
            () -> assertThat(mvcResult.getResponse().getContentAsString(), is(3)),
            //() -> assertThat(mvcResult.get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
            () -> assertThat(mvcResult.getResponse().getContentAsString(), containsString(orgProfileIds.get(0)))
        );

        List<AccessTypeRolesField> result = controller.getAccessTypeRoles(organisationProfileIds);
        assertEquals(expected, result);
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

    private AccessTypeRolesEntity createAccessTypeRolesEntity(String caseTypeId, String organisationProfileId) {
        AccessTypeRolesEntity accessTypeRolesEntity = new AccessTypeRolesEntity();
        AccessTypeRolesField accessTypeRolesField = new AccessTypeRolesField();

        //String caseTypeId = "get-test";
        CaseTypeEntity caseTypeReturned = createCaseType(caseTypeId, caseTypeId);

        //accessTypeRolesField = createAccessTypeRolesField(caseTypeId );
        accessTypeRolesEntity.setCaseTypeId(caseTypeReturned);
        accessTypeRolesEntity.setAccessTypeId("default");
        accessTypeRolesEntity.setOrganisationProfileId(organisationProfileId);
        accessTypeRolesEntity.setDescription("ACCESS CASES");
        accessTypeRolesEntity.setAccessMandatory(true);
        accessTypeRolesEntity.setAccessDefault(true);
        accessTypeRolesEntity.setOrganisationalRoleName("civil-solicitor");
        accessTypeRolesEntity.setLiveFrom(LocalDate.now());
        accessTypeRolesEntity.setHint("User can work with all civil cases without needing to be assigned to each case");
        accessTypeRolesEntity.setDisplayOrder(1);
        accessTypeRolesEntity.setGroupRoleName("[APPLICANTSOLICITORONE]");
        accessTypeRolesEntity.setOrganisationPolicyField("applicant1OrganisationPolicy");
        accessTypeRolesEntity.setCaseAccessGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");

        return accessTypeRolesEntity;
    }
}
