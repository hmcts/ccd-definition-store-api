package uk.gov.hmcts.net.ccd.definition.store.rest;

import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageCollection;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkbasketInputDefinition;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class DisplayApiControllerEndpointIT extends BaseTest {
    private static final String WIZARD_PAGE_STRUCTURE_URL_1 = "/api/display/wizard-page-structure/case-types/%s/event-triggers/%s";
    private static final String WORKBASKET_INPUT_DEFINITION_URL = "/api/display/work-basket-input-definition/%s";

    @Test
    public void shouldReturnThreeWorkbasketInputFieldsForTestAddressBookCase() throws Exception {
        givenUserProfileReturnsSuccess();
        InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_URL)
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

        final WorkbasketInputDefinition workbasketInputDefinition = mapper.readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructType(WorkbasketInputDefinition.class));
        assertAll(
            () -> assertThat(workbasketInputDefinition.getFields(), hasSize(3)),
            () -> assertThat(workbasketInputDefinition.getFields(), hasItem(Matchers.hasProperty("label", containsString("First Name")))),
            () -> assertThat(workbasketInputDefinition.getFields(), hasItem(Matchers.hasProperty("label", containsString("Last Name")))),
            () -> assertThat(workbasketInputDefinition.getFields(), hasItem(Matchers.hasProperty("label", containsString("Address"))))
        );
    }

    @Test
    public void shouldReturnThreeWizardPagesForTestAddressBookCase() throws Exception {
        givenUserProfileReturnsSuccess();
        InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_URL)
            .file(file)
            .header(AUTHORIZATION, "Bearer testUser"))
            .andReturn();
        assertResponseCode(mvcResult, HttpStatus.SC_CREATED);
        final String CASE_TYPE = "TestAddressBookCase";
        final String EVENT_TYPE = "enterCaseIntoLegacy";
        final String URL = String.format(WIZARD_PAGE_STRUCTURE_URL_1, CASE_TYPE, EVENT_TYPE);
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.case_type_id").value(CASE_TYPE))
            .andExpect(jsonPath("$.event_id").value(EVENT_TYPE))
            .andExpect(jsonPath("$.wizard_pages").isArray())
            .andReturn();

        WizardPageCollection wizardPageCollection = mapper.readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructType(WizardPageCollection.class));
        assertAll(
            () -> assertThat(wizardPageCollection.getWizardPages(), hasSize(3)),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(Matchers.hasProperty("label", containsString("Personal Information")))),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(Matchers.hasProperty("label", containsString("Address Information")))),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(Matchers.hasProperty("label", containsString("A Label"))))
        );
    }

    @Test
    public void shouldReturnSingleWizardPageForTestComplexAddressBookCase() throws Exception {
        givenUserProfileReturnsSuccess();
        InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_URL)
            .file(file)
            .header(AUTHORIZATION, "Bearer testUser"))
            .andReturn();
        assertResponseCode(mvcResult, HttpStatus.SC_CREATED);
        final String CASE_TYPE = "TestComplexAddressBookCase";
        final String EVENT_TYPE = "createCase";
        final String URL = String.format(WIZARD_PAGE_STRUCTURE_URL_1, CASE_TYPE, EVENT_TYPE);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.case_type_id").value(CASE_TYPE))
            .andExpect(jsonPath("$.event_id").value(EVENT_TYPE))
            .andExpect(jsonPath("$.wizard_pages").isArray())
            .andReturn();
        final WizardPageCollection wizardPageCollection = mapper.readValue(result.getResponse().getContentAsString(), TypeFactory.defaultInstance().constructType(WizardPageCollection.class));
        assertAll(
            () -> assertThat(wizardPageCollection.getWizardPages(), hasSize(1)),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(Matchers.hasProperty("label", containsString("Contact Information")))),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(Matchers.hasProperty("id", containsString("createCaseContactPage")))),
            () -> assertThat(wizardPageCollection.getWizardPages(), hasItem(Matchers.hasProperty("wizardPageFields",
                hasItem(Matchers.hasProperty("caseFieldId", containsString("ContectEmail"))))))
        );
    }
}
