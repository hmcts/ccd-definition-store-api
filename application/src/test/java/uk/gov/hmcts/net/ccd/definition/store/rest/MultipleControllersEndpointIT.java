package uk.gov.hmcts.net.ccd.definition.store.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTabCollection;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageCollection;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkbasketInputDefinition;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PRIVATE;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.RESTRICTED;

@Transactional
class MultipleControllersEndpointIT extends BaseTest {
    private static final String WIZARD_PAGE_STRUCTURE_URL_1 =
        "/api/display/wizard-page-structure/case-types/%s/event-triggers/%s";
    private static final String TABS_STRUCTURE_URL =
        "/api/display/tab-structure/%s";
    private static final String ROLES_URL = "/api/user-roles/%s";
    private static final String WORKBASKET_INPUT_DEFINITION_URL = "/api/display/work-basket-input-definition/%s";
    private static final String JURISDICTIONS_URL = "/api/data/jurisdictions";
    private static final String CASE_TYPE_URL = "/api/data/case-type/%s";

    @Test
    void shouldReturnCaseType() throws Exception {
        try (final InputStream inputStream =
                 new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                .file(file)
                .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();
            assertResponseCode(mvcResult, HttpStatus.SC_CREATED);
            final String CASE_TYPE = "TestAddressBookCase";
            final String URL = String.format(CASE_TYPE_URL, CASE_TYPE);
            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(CASE_TYPE))
                .andReturn();

            final CaseType caseType = mapper.readValue(result.getResponse()
                .getContentAsString(), TypeFactory.defaultInstance().constructType(CaseType.class));
            assertAll(
                () -> assertThat(caseType.getEvents().stream().filter(
                    e -> e.getId().equals("enterCaseIntoLegacy")).findFirst().get(),
                    hasProperty("caseFields", hasItem(allOf(
                        hasProperty("showSummaryContentOption", equalTo(2)),
                        hasProperty("caseFieldId", containsString("PersonFirstName")))))),
                () -> assertThat(caseType.getEvents().stream().filter(
                    e -> e.getId().equals("enterCaseIntoLegacy")).findFirst().get(),
                    hasProperty("caseFields", hasItem(allOf(
                        hasProperty("showSummaryContentOption", equalTo(1)),
                        hasProperty("caseFieldId", containsString("PersonLastName")))))),
                () -> assertThat(caseType.getEvents().stream().filter(
                    e -> e.getId().equals("createCase")).findFirst().get(),
                    hasProperty("showEventNotes", equalTo(true))),
                () -> assertThat(caseType.getEvents().stream().filter(
                    e -> e.getId().equals("enterCaseIntoLegacy")).findFirst().get(),
                    hasProperty("showEventNotes", equalTo(false))),
                () -> assertThat(caseType.getEvents().stream().filter(
                    e -> e.getId().equals("stopCase")).findFirst().get(),
                    hasProperty("showEventNotes", nullValue()))
            );
        }
    }

    // To be @Nested - DisplayAPI Controller
    @Test
    void shouldReturnThreeWorkbasketInputFieldsForTestAddressBookCase() throws Exception {
        InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
            .file(file)
            .header(AUTHORIZATION, "Bearer testUser"))
            .andReturn();
        assertResponseCode(mvcResult, HttpStatus.SC_CREATED);
        final String CASE_TYPE = "TestAddressBookCase";
        final String URL = String.format(WORKBASKET_INPUT_DEFINITION_URL, CASE_TYPE);
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.case_type_id").value(CASE_TYPE))
            .andReturn();

        final WorkbasketInputDefinition workbasketInputDefinition = mapper.readValue(
            result.getResponse().getContentAsString(),
            TypeFactory.defaultInstance().constructType(WorkbasketInputDefinition.class));
        assertAll(
            () -> assertThat(workbasketInputDefinition.getFields(), hasSize(3)),
            () -> assertThat(workbasketInputDefinition.getFields(), hasItem(hasProperty("label",
                containsString("First Name")))),
            () -> assertThat(workbasketInputDefinition.getFields(), hasItem(hasProperty("label",
                containsString("Last Name")))),
            () -> assertThat(workbasketInputDefinition.getFields(), hasItem(hasProperty("label",
                containsString("Address"))))
        );
    }

    @Test
    void shouldReturnTabsForTestAddressBookCase() throws Exception {
        InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
            .file(file)
            .header(AUTHORIZATION, "Bearer testUser"))
            .andReturn();
        assertResponseCode(mvcResult, HttpStatus.SC_CREATED);
        final String CASE_TYPE = "TestAddressBookCase";
        final String URL = String.format(TABS_STRUCTURE_URL, CASE_TYPE);
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.case_type_id").value(CASE_TYPE))
            .andExpect(jsonPath("$.tabs").isArray())
            .andReturn();

        CaseTabCollection caseTabCollection = mapper.readValue(result.getResponse().getContentAsString(),
            TypeFactory.defaultInstance().constructType(CaseTabCollection.class));
        assertAll(
            () -> assertThat(caseTabCollection.getTabs(), hasSize(5)),
            () -> assertThat(caseTabCollection.getTabs(), hasItem(allOf(
                hasProperty("showCondition", containsString("PersonLastName=\"Sparrow\"")),
                hasProperty("id", containsString("NameTab")),
                hasProperty("label", containsString("Name"))))),
            () -> {
                assertThat(caseTabCollection.getTabs().stream()
                        .filter(t -> t.getId().equals("NameTab")).findFirst().get(),
                    hasProperty("tabFields", hasItem(allOf(
                        hasProperty("showCondition", containsString("PersonFirstName=\"Jack\"")),
                        hasProperty("caseField",
                            hasProperty("id", containsString("PersonLastName")))))));
            }
        );
    }

    @Test
    void shouldReturnThreeWizardPagesForTestAddressBookCase() throws Exception {
        InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
            .file(file)
            .header(AUTHORIZATION, "Bearer testUser"))
            .andReturn();
        assertResponseCode(mvcResult, HttpStatus.SC_CREATED);
        final String CASE_TYPE = "TestAddressBookCase";
        final String eventType = "enterCaseIntoLegacy";
        final String URL = String.format(WIZARD_PAGE_STRUCTURE_URL_1, CASE_TYPE, eventType);
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.case_type_id").value(CASE_TYPE))
            .andExpect(jsonPath("$.event_id").value(eventType))
            .andExpect(jsonPath("$.wizard_pages").isArray())
            .andReturn();

        WizardPageCollection wizardPageCollection = mapper.readValue(result.getResponse().getContentAsString(),
            TypeFactory.defaultInstance().constructType(WizardPageCollection.class));
        assertAll(
            () -> assertThat(wizardPageCollection.getWizardPages(), hasSize(3)),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(hasProperty("label",
                containsString("Personal Information")))),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(hasProperty("label",
                containsString("Address Information")))),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(hasProperty("label",
                containsString("A Label"))))
        );
    }

    @Test
    void shouldReturnSingleWizardPageForTestComplexAddressBookCase() throws Exception {
        InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
            .file(file)
            .header(AUTHORIZATION, "Bearer testUser"))
            .andReturn();
        assertResponseCode(mvcResult, HttpStatus.SC_CREATED);
        final String CASE_TYPE = "TestComplexAddressBookCase";
        final String eventType = "createCase";
        final String URL = String.format(WIZARD_PAGE_STRUCTURE_URL_1, CASE_TYPE, eventType);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.case_type_id").value(CASE_TYPE))
            .andExpect(jsonPath("$.event_id").value(eventType))
            .andExpect(jsonPath("$.wizard_pages").isArray())
            .andReturn();
        final WizardPageCollection wizardPageCollection = mapper.readValue(result.getResponse().getContentAsString(),
            TypeFactory.defaultInstance().constructType(WizardPageCollection.class));
        assertAll(
            () -> assertThat(wizardPageCollection.getWizardPages(), hasSize(1)),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(hasProperty("label",
                containsString("Contact Information")))),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(
                hasProperty("id", containsString("createCaseContactPage")))),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(hasProperty("wizardPageFields",
                hasItem(hasProperty("caseFieldId", containsString("ContectEmail"))))))
        );
    }

    // To be @Nested - UserRoleController
    @Test
    void shouldReturnUserRolesForDefinedRoles() throws Exception {
        final String URL = String.format(ROLES_URL, "CaseWorker1,CaseWorker2,CaseWorker3,Nayab,Fatih,Andrzej,Mario");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
        final List<UserRole> userRoles = mapper.readValue(result.getResponse().getContentAsString(),
            TypeFactory.defaultInstance().constructType(new TypeReference<List<UserRole>>() {
            }));
        assertAll(
            () -> assertThat(userRoles, hasSize(3)),
            () -> assertThat(userRoles.get(0).getSecurityClassification(), is(PUBLIC)),
            () -> assertThat(userRoles.get(1).getSecurityClassification(), is(PRIVATE)),
            () -> assertThat(userRoles.get(2).getSecurityClassification(), is(RESTRICTED))
        );
    }

    @Test
    void shouldSaveUserRole() throws Exception {
        final String URL = ROLES_URL.substring(0, ROLES_URL.length() - 4);
        MvcResult result = mockMvc
            .perform(MockMvcRequestBuilders
                .put(URL)
                .contentType("application/json")
                .accept("application/json")
                .content("{\"role\":\"to-delete-1\",\"security_classification\":\"PUBLIC\"}"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andReturn();
        final UserRole userRole = mapper.readValue(result.getResponse().getContentAsString(),
            TypeFactory.defaultInstance().constructType(new TypeReference<UserRole>() {
            }));
        assertAll(
            () -> assertThat(userRole.getSecurityClassification(), is(PUBLIC)),
            () -> assertThat(userRole.getRole(), is("to-delete-1")),
            () -> assertThat(userRole.getId(), is(notNullValue()))
        );
    }

    @Test
    void shouldReturnNoUserRolesWhenUndefinedRolesQueried() throws Exception {
        final String URL = String.format(ROLES_URL, "Nayab,Fatih,Andrzej,Mario");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
        final List<UserRole> userRoles = mapper.readValue(result.getResponse().getContentAsString(),
            TypeFactory.defaultInstance().constructType(new TypeReference<List<UserRole>>() {
            }));
        assertAll(
            () -> assertThat(userRoles, hasSize(0))
        );
    }

    // To be @Nested - CaseDefinition Controller
    @Test
    void shouldReturnJurisdictionsUpperCase() throws Exception {
        shouldReturnJurisdictions("TEST");
    }

    @Test
    void shouldReturnJurisdictionsLowerCase() throws Exception {
        shouldReturnJurisdictions("test");
    }

    private void shouldReturnJurisdictions(String id) throws Exception {

        InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
            .file(file)
            .header(AUTHORIZATION, "Bearer testUser"))
            .andReturn();
        assertResponseCode(mvcResult, HttpStatus.SC_CREATED);
        final String URL = JURISDICTIONS_URL + "?ids=" + id;
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        List<Jurisdiction> jurisdictions = mapper.readValue(result.getResponse().getContentAsString(),
            TypeFactory.defaultInstance().constructType(new TypeReference<List<Jurisdiction>>() {
            }));

        assertAll(
            () -> assertThat(jurisdictions, hasSize(1)),
            () -> assertThat(jurisdictions, hasItem(
                allOf(
                    hasProperty("id", is("TEST")),
                    hasProperty("name", is("Test"))
                )))
        );
    }
}
