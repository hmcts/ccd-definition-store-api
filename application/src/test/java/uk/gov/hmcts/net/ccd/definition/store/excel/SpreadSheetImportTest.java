package uk.gov.hmcts.net.ccd.definition.store.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Component-level tests for the Core Case Definition Importer API.
 *
 * @author Daniel Lam (A533913)
 */
@TestPropertySource(properties = {"ccd.authorised.services=ccd_data"})
public class SpreadSheetImportTest extends BaseTest {
    private static final String TEST_CASE_TYPE = "TestAddressBookCase";
    private static final String CASE_TYPE_DEF_URL = "/api/data/caseworkers/cid/jurisdictions/jid/case-types/" +
        TEST_CASE_TYPE;
    private static final String GET_CASE_TYPES_COUNT_QUERY = "SELECT COUNT(*) FROM case_type";

    private static final int JURISDICTION_ID = 1;
    private static final String RESPONSE_JSON = "GetCaseTypesResponseForCCD_TestDefinition_V18.json";

    private Map<Object, Object> caseTypesId;
    private Map<Object, Object> fieldTypesId;

    /**
     * API test for successful import of a valid Case Definition spreadsheet.
     *
     * @throws Exception
     *         On error running test
     */
    @Test
    @Transactional
    public void importValidDefinitionFile() throws Exception {

        givenUserProfileReturnsSuccess();

        InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_URL)
                                                  .file(file)
                                                  .header(AUTHORIZATION, "Bearer testUser")) //
            .andReturn();

        assertResponseCode(mvcResult, HttpStatus.SC_CREATED);

        final String expectedUserProfiles = "[{\"id\":\"user1@hmcts.net\"," +
            "\"work_basket_default_jurisdiction\":\"TEST\"," +
            "\"work_basket_default_case_type\":\"TestAddressBookCase\"," +
            "\"work_basket_default_state\":\"CaseCreated\"}," + //
            "{\"id\":\"UseR2@hmcts.net\"," + "\"work_basket_default_jurisdiction\":\"TEST\"," +
            "\"work_basket_default_case_type\":\"TestAddressBookCase\"," +
            "\"work_basket_default_state\":\"CaseEnteredIntoLegacy\"}]";
        WireMock.verify(1,
                        putRequestedFor(urlEqualTo("/user-profile/users")).withRequestBody(equalTo
                                                                                               (expectedUserProfiles)));

        // Check the HTTP GET request for the imported Case Type returns the correct response.
        MvcResult getCaseTypesMvcResult = mockMvc.perform(MockMvcRequestBuilders.get(CASE_TYPE_DEF_URL)
                                                              .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
        assertBody(getCaseTypesMvcResult.getResponse().getContentAsString());

        assertDatabaseIsCorrect();
    }

    /**
     * API test when user profile app fails to respond.
     *
     * @throws Exception
     *         On error running test
     */
    @Test
    @Transactional
    public void importValidDefinitionFileUserProfileNotResponding() throws Exception {

        final InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        final MockMultipartFile file = new MockMultipartFile("file", inputStream);

        // Given WorkBasketUserDefaultService is not getting a response

        // when I import a definition file
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_URL)
                                               .file(file)
                                               .header(AUTHORIZATION, "Bearer testUser")).andReturn();

        assertResponseCode(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        // Check the error response message.
        assertThat("Incorrect HTTP response",
                   result.getResponse().getContentAsString(),
                   allOf(containsString("Problem updating user profile"), containsString("404 Not Found")));
    }

    /**
     * API test when user profile app fails to respond.
     *
     * @throws Exception
     *         On error running test
     */
    @Test
    @Transactional
    public void importValidDefinitionFileUserProfileHas403Response() throws Exception {

        final InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        final MockMultipartFile file = new MockMultipartFile("file", inputStream);

        // Given wiremock returns http status 403
        WireMock.givenThat(WireMock.put(WireMock.urlEqualTo("/user-profile/users"))
                               .willReturn(WireMock.aResponse().withStatus(403)));

        // when I import a definition file
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_URL)
                                               .file(file)
                                               .header(AUTHORIZATION, "Bearer testUser")).andReturn();

        assertResponseCode(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        // Check the error response message.
        assertThat("Incorrect HTTP response",
                   result.getResponse().getContentAsString(),
                   allOf(containsString("Problem updating user profile"), containsString("403 Forbidden")));
    }

    /**
     * API test for failure to import a Case Definition spreadsheet, due to invalid data format. (This means one or more
     * workbook sheets did not contain a Definition name in Cell A1, and/or were missing data attribute headers.)
     *
     * @throws Exception
     *         On error running test
     */
    @Test
    @Transactional
    public void importInvalidDefinitionFile() throws Exception {
        InputStream inputStream = new ClassPathResource("/CCD_TestDefinition_Invalid_Data.xlsx",
                                                        getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_URL)
                                                     .file(file)
                                                     .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();

        // Check the error response message.
        assertThat("Incorrect HTTP status message for bad request",
                   result.getResponse().getContentAsString(),
                   containsString("Invalid Case Definition sheet - no Definition data attribute headers found"));

        // Check that no Definition data has been persisted.
        assertEquals("Unexpected number of rows returned from case_type_items table",
                     0,
                     jdbcTemplate.queryForObject(GET_CASE_TYPES_COUNT_QUERY, Integer.class).intValue());
    }

    /**
     * API test for transactional rollback of import of Case Definition spreadsheet that fails due to missing a required
     * workbook sheet (in this case, WorkBasketResultFields).
     *
     * @throws Exception
     *         On error running test
     */
    @Test
    public void rollbackFailedDefinitionFileImport() throws Exception {
        InputStream inputStream = new ClassPathResource("/ccd_testdefinition-missing-WorkBasketResultFields.xlsx",
                                                        getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_URL)
                                                     .file(file)
                                                     .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();

        // Check the error response message.
        assertThat("Incorrect HTTP status message for bad request",
                   result.getResponse().getContentAsString(),
                   containsString("A definition must contain a WorkBasketResultFields sheet"));

        // Check that no Definition data has been persisted.
        assertEquals("Unexpected number of rows returned from case_type_items table",
                     0,
                     jdbcTemplate.queryForObject(GET_CASE_TYPES_COUNT_QUERY, Integer.class).intValue());
    }

    @Test
    public void userProfileIsNotStoredWhenImportFails() throws Exception {

        givenUserProfileReturnsSuccess();

        InputStream inputStream = new ClassPathResource("/ccd-definition-wrong-complex-type.xlsx",
                                                        getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_URL)
                                                     .file(file)
                                                     .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();

        WireMock.verify(0, putRequestedFor(urlEqualTo("/user-profile/users")));

        // Check that no Definition data has been persisted.
        assertEquals("Unexpected number of rows returned from case_type_items table",
                     0,
                     jdbcTemplate.queryForObject(GET_CASE_TYPES_COUNT_QUERY, Integer.class).intValue());
        assertEquals("data stored during a failed import",
                     0,
                     jdbcTemplate.queryForObject(GET_CASE_TYPES_COUNT_QUERY, Integer.class).intValue());
    }

    /**
     * returns a version of the 'hasEntry' matcher that is unchecked. This allows mixing of different types of
     * matchers for a of Map<String, Object>, which would otherwise be not possible due to compilation issues
     */
    @SuppressWarnings("unchecked")
    public static Matcher<Map<String, Object>> hasColumn(Matcher<String> keyMatcher, Matcher valueMatcher) {
        Matcher mapMatcher = hasEntry(keyMatcher, valueMatcher);
        return mapMatcher;
    }

    @SuppressWarnings("unchecked")
    public static Matcher<Map<String, Object>> hasColumn(String key, Object value) {
        Matcher mapMatcher = hasColumn(is(key), is(value));
        return mapMatcher;
    }

    private void assertBody(String contentAsString) throws IOException, URISyntaxException {

        final String expected = formatJsonString(readFileToString(new File(getClass().getClassLoader()
                                                                               .getResource(RESPONSE_JSON)
                                                                               .toURI())));

        assertEquals(removeGuids(expected), formatJsonString(removeGuids(contentAsString)));

    }

    private String formatJsonString(String string) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(objectMapper.readValue(string, Object.class));
    }

    private String removeGuids(String response) throws IOException {
        String guidRegex = "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";
        Pattern idFieldWithGuid = Pattern.compile(String.format("\"id\"\\s*:\\s*\"[A-Za-z0-9]*-(%s)\"", guidRegex));
        java.util.regex.Matcher matcher = idFieldWithGuid.matcher(response);
        while (matcher.find()) {
            response = response.replace(matcher.toMatchResult().group(1), "<GUID-REMOVED>");
        }
        return response;
    }

    private void assertDatabaseIsCorrect() {
        assertJurisdiction();
        caseTypesId = getIdsByReference("SELECT reference, id FROM case_type");
        fieldTypesId = getIdsByReference("SELECT reference, id FROM field_type");

        assertFieldTypes();
        assertLayout();
    }

    private void assertJurisdiction() {
        Map<String, Object> jurisdictionRow = jdbcTemplate.queryForMap("SELECT * FROM jurisdiction");
        assertThat(jurisdictionRow,
                   allOf(hasColumn("id", JURISDICTION_ID),
                         hasColumn("reference", "TEST"),
                         hasColumn("version", 1),
                         hasColumn("name", "Test"),
                         hasColumn("description", "Content for the Test Jurisdiction.")));
    }

    private void assertFieldTypes() {
        List<Map<String, Object>> fieldTypes = jdbcTemplate.queryForList("SELECT * FROM field_type");

        assertListFieldTypes(fieldTypes);
        assertComplexFieldTypes(fieldTypes);
        assertFieldTypes(fieldTypes);
    }

    private void assertListFieldTypes(List<Map<String, Object>> fieldTypes) {

        assertThat(fieldTypes,
                   allOf(hasItem(allOf(hasColumn("jurisdiction_id", JURISDICTION_ID),
                                       hasColumn("reference", "FixedList-marritalStatusEnum"),
                                       hasColumn("base_field_type_id", fieldTypesId.get("FixedList")))),
                         hasItem(allOf(hasColumn("jurisdiction_id", JURISDICTION_ID),
                                       hasColumn("reference", "MultiSelectList-marritalStatusEnum"),
                                       hasColumn("base_field_type_id", fieldTypesId.get("MultiSelectList")))),
                         hasItem(allOf(hasColumn("reference", "FixedList-regionalCentreEnum"))),
                         hasItem(allOf(hasColumn("reference", "MultiSelectList-regionalCentreEnum")))));

        List<Map<String, Object>> fieldTypeListItems = jdbcTemplate.queryForList("SELECT * FROM field_type_list_item");

        assertThat(fieldTypeListItems,
                   allOf(hasItem(allOf(hasColumn("value", "MARRIAGE"),
                                       hasColumn("field_type_id", fieldTypesId.get("FixedList-marritalStatusEnum")))),
                         hasItem(allOf(hasColumn("value", "MARRIAGE"),
                                       hasColumn("field_type_id",
                                                 fieldTypesId.get("MultiSelectList-marritalStatusEnum")))),
                         hasItem(allOf(hasColumn("value", "OXFORD"),
                                       hasColumn("field_type_id", fieldTypesId.get("FixedList-regionalCentreEnum")))),
                         hasItem(allOf(hasColumn("value", "OXFORD"),
                                       hasColumn("field_type_id",
                                                 fieldTypesId.get("MultiSelectList-regionalCentreEnum"))))));
    }

    private void assertComplexFieldTypes(List<Map<String, Object>> fieldTypes) {

        assertThat(fieldTypes,
                   allOf(hasItem(allOf(hasColumn("jurisdiction_id", JURISDICTION_ID),
                                       hasColumn("reference", "Address"),
                                       hasColumn("base_field_type_id", fieldTypesId.get("Complex")))),
                         hasItem(allOf(hasColumn("jurisdiction_id", JURISDICTION_ID),
                                       hasColumn("reference", "Person"),
                                       hasColumn("base_field_type_id", fieldTypesId.get("Complex"))))));

        List<Map<String, Object>> complexFieldsItems = jdbcTemplate.queryForList("SELECT * FROM complex_field");

        assertThat(complexFieldsItems,
                   allOf(hasItem(allOf(hasColumn("reference", "AddressLine3"),
                                       hasColumn("field_type_id", fieldTypesId.get("Text")),
                                       hasColumn("complex_field_type_id", fieldTypesId.get("Address")),
                                       hasColumn("security_classification",
                                                 SecurityClassification.PRIVATE.toString()))),
                         hasItem(allOf(hasColumn("reference", "Postcode"),
                                       hasColumn("field_type_id", fieldTypesId.get("Postcode")),
                                       hasEntry("complex_field_type_id", fieldTypesId.get("Address")),
                                       hasColumn("show_condition", "Country=\"Italy\""),
                                       hasColumn("security_classification",
                                                 SecurityClassification.PRIVATE.toString()))),
                         hasItem(allOf(hasColumn("reference", "FirstName"),
                                       hasColumn("field_type_id", fieldTypesId.get("Text")),
                                       hasColumn("complex_field_type_id", fieldTypesId.get("Person")),
                                       hasColumn("security_classification",
                                                 SecurityClassification.RESTRICTED.toString()),
                                       hasColumn("show_condition", null))),
                         hasItem(allOf(hasColumn("reference", "PostalAddress"),
                                       hasColumn("field_type_id", fieldTypesId.get("Address")),
                                       hasColumn("complex_field_type_id", fieldTypesId.get("Company")),
                                       hasColumn("security_classification",
                                                 SecurityClassification.PRIVATE.toString()))),
                         hasItem(allOf(hasColumn("reference", "Name"),
                                       hasColumn("field_type_id", fieldTypesId.get("Text")),
                                       hasColumn("complex_field_type_id", fieldTypesId.get("Party")),
                                       hasColumn("security_classification", SecurityClassification.PUBLIC.toString()))),
                         hasItem(allOf(hasColumn("reference", "BusinessAddress"),
                                       hasColumn("field_type_id", fieldTypesId.get("Address")),
                                       hasColumn("complex_field_type_id", fieldTypesId.get("Party")),
                                       hasColumn("security_classification",
                                                 SecurityClassification.RESTRICTED.toString()))),
                         hasItem(allOf(hasColumn("reference", "PostalAddress"),
                                       hasColumn("field_type_id", fieldTypesId.get("Address")),
                                       hasColumn("complex_field_type_id", fieldTypesId.get("Party")),
                                       hasColumn("security_classification",
                                                 SecurityClassification.PRIVATE.toString())))));
    }

    private void assertFieldTypes(List<Map<String, Object>> fieldTypes) {

        assertThat(fieldTypes,
                   allOf(hasItem(allOf(hasColumn("jurisdiction_id", JURISDICTION_ID),
                                       hasColumn("base_field_type_id", fieldTypesId.get("Collection")),
                                       hasColumn("collection_field_type_id", fieldTypesId.get("Person")),
                                       hasColumn(is("reference"), startsWith("Group-")))),
                         hasItem(allOf(hasColumn("jurisdiction_id", JURISDICTION_ID),
                                       hasColumn("base_field_type_id", fieldTypesId.get("Collection")),
                                       hasColumn("collection_field_type_id", fieldTypesId.get("Text")),
                                       hasColumn(is("reference"), startsWith("Alliases-")))),
                         hasItem(allOf(hasColumn("base_field_type_id", fieldTypesId.get("Text")),
                                       hasColumn(is("collection_field_type_id"), nullValue()),
                                       hasColumn("minimum", "3.0"),
                                       hasColumn("maximum", "20.0"),
                                       hasColumn(is("reference"), startsWith("PersonLastNameWithValidation-"))))));
    }

    private void assertLayout() {
        Map<Object, Object> caseFieldIds = getIdsByReference(
            "SELECT reference, id FROM case_field where case_type_id = ?",
            "TestComplexAddressBookCase");

        assertWorkBasketInput(caseFieldIds);
        assertWorkbasket(caseFieldIds);
        assertSearchInput(caseFieldIds);
        assertSearchResult(caseFieldIds);
        assertCaseTypeTab();
        assertWizardPage();
    }

    private void assertWorkBasketInput(Map<Object, Object> caseFieldIds) {
        List<Map<String, Object>> allWorkbasket = jdbcTemplate.queryForList("SELECT * FROM " +
                                                                                "workbasket_input_case_field");
        assertThat(allWorkbasket, hasSize(13));

        List<Map<String, Object>> caseTypeWorkbasket = jdbcTemplate.queryForList(
            "SELECT * FROM workbasket_input_case_field where case_type_id = ?",
            caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(caseTypeWorkbasket,
                   allOf(hasItem(allOf(hasColumn("label", "First Name"),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonFirstName")))

                                ),
                         hasItem(allOf(hasColumn("label", "Last Name"),
                                       hasColumn("display_order", 2),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonLastName"))))));
    }

    private void assertWorkbasket(Map<Object, Object> caseFieldIds) {

        List<Map<String, Object>> allWorkbasket = jdbcTemplate.queryForList("SELECT * FROM workbasket_case_field");
        assertThat(allWorkbasket, hasSize(6));

        List<Map<String, Object>> caseTypeWorkbasket = jdbcTemplate.queryForList(
            "SELECT * FROM workbasket_case_field where case_type_id = ?",
            caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(caseTypeWorkbasket,
                   allOf(hasItem(allOf(hasColumn("label", "Contect Number"),
                                       hasColumn("display_order", 2),
                                       hasColumn("case_field_id", caseFieldIds.get("ContectNumber")))

                                ),
                         hasItem(allOf(hasColumn("label", "Age"),
                                       hasColumn("display_order", 3),
                                       hasColumn("case_field_id", caseFieldIds.get("Age"))))));
    }

    private void assertSearchInput(Map<Object, Object> caseFieldIds) {
        List<Map<String, Object>> allWorkbasket = jdbcTemplate.queryForList("SELECT * FROM search_input_case_field");
        assertThat(allWorkbasket, hasSize(13));

        List<Map<String, Object>> caseTypeWorkbasket = jdbcTemplate.queryForList(
            "SELECT * FROM search_input_case_field where case_type_id = ?",
            caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(caseTypeWorkbasket,
                   allOf(hasItem(allOf(hasColumn("label", "First Name"),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonFirstName")))

                                ),
                         hasItem(allOf(hasColumn("label", "Last Name"),
                                       hasColumn("display_order", 2),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonLastName"))))));
    }

    private void assertSearchResult(Map<Object, Object> caseFieldIds) {
        List<Map<String, Object>> allWorkbasket = jdbcTemplate.queryForList("SELECT * FROM search_result_case_field");
        assertThat(allWorkbasket, hasSize(6));

        List<Map<String, Object>> caseTypeWorkbasket = jdbcTemplate.queryForList(
            "SELECT * FROM search_result_case_field where case_type_id = ?",
            caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(caseTypeWorkbasket,
                   allOf(hasItem(allOf(hasColumn("label", "Date of Birth"),
                                       hasColumn("display_order", 2),
                                       hasColumn("case_field_id", caseFieldIds.get("DateOfBirth")))

                                ),
                         hasItem(allOf(hasColumn("label", "Contact Email"),
                                       hasColumn("display_order", 3),
                                       hasColumn("case_field_id", caseFieldIds.get("ContectEmail"))))));
    }

    private void assertCaseTypeTab() {
        List<Map<String, Object>> allDisplayGroups = jdbcTemplate.queryForList(
            "SELECT * FROM display_group WHERE type = 'TAB'");
        assertThat(allDisplayGroups, hasSize(11));

        List<Map<String, Object>> caseTypeDisplayGroup = jdbcTemplate.queryForList(
            "SELECT * FROM display_group WHERE case_type_id = ? AND type = 'TAB'",
            caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(caseTypeDisplayGroup,
                   allOf(hasItem(allOf(hasColumn("reference", "NameTab"),
                                       hasColumn("label", "Name"),
                                       hasColumn("display_order", 1))

                                ),
                         hasItem(allOf(hasColumn("reference", "ContectEmail"),
                                       hasColumn("label", "Details"),
                                       hasColumn("display_order", 3)))));

        Map<Object, Object> caseFieldIds = getIdsByReference(
            "SELECT reference, id FROM case_field where case_type_id = ?",
            "TestComplexAddressBookCase");
        Map<Object, Object> displayGroupsId = getIdsByReference(
            "SELECT reference, id FROM display_group where case_type_id = ?",
            "TestComplexAddressBookCase");

        List<Map<String, Object>> displayGroupsFields = jdbcTemplate.queryForList(
            "select dgcf.* from display_group_case_field dgcf, display_group dg where dgcf.display_group_id = dg.id "
                + "AND dg.type = 'TAB';");
        assertThat(displayGroupsFields, hasSize(13));
        assertThat(displayGroupsFields,
                   allOf(hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("NameTab")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonFirstName")))),
                         hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("NameTab")),
                                       hasColumn("display_order", 2),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonLastName")))),
                         hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("ContectEmail")),
                                       hasColumn("display_order", 4),
                                       hasColumn("case_field_id", caseFieldIds.get("ContectEmail"))))));

    }

    private void assertWizardPage() {
        List<Map<String, Object>> allDisplayGroups = jdbcTemplate.queryForList(
            "SELECT * FROM display_group WHERE type = 'PAGE'");
        assertThat(allDisplayGroups, hasSize(6));

        List<Map<String, Object>> caseTypeDisplayGroup = jdbcTemplate.queryForList(
            "SELECT * FROM display_group where case_type_id = ? AND type = 'PAGE'",
            caseTypesId.get("TestAddressBookCase"));
        assertThat(caseTypeDisplayGroup,
                   allOf(hasItem(allOf(hasColumn("reference", "createCaseInfoPage"),
                                       hasColumn("label", "Required Information1"),
                                       hasColumn("show_condition", "HasOtherInfo=\"Yes\""),
                                       hasColumn("display_order", 2))),
                         hasItem(allOf(hasColumn("reference", "enterCaseIntoLegacyPage1"),
                                       hasColumn("label", "A Label"),
                                       hasColumn("show_condition", null),
                                       hasColumn("display_order", null)))));

        Map<Object, Object> caseFieldIds = getIdsByReference(
            "SELECT reference, id FROM case_field where case_type_id = ?",
            "TestAddressBookCase");
        Map<Object, Object> displayGroupsId = getIdsByReference(
            "SELECT reference, id FROM display_group where case_type_id = ?",
            "TestAddressBookCase");

        List<Map<String, Object>> displayGroupsFields = jdbcTemplate.queryForList(
            "select dgcf.* from display_group_case_field dgcf, display_group dg where dgcf.display_group_id = dg.id "
                + "AND dg.type = 'PAGE';");
        assertThat(displayGroupsFields, hasSize(9));
        assertThat(displayGroupsFields,
                   allOf(hasItem(allOf(hasColumn("display_group_id",
                                                 displayGroupsId.get("enterCaseIntoLegacyPersonPage")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonFirstName")))),
                         hasItem(allOf(hasColumn("display_group_id",
                                                 displayGroupsId.get("enterCaseIntoLegacyPersonPage")),
                                       hasColumn("display_order", 2),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonLastName")))),
                         hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("createCaseInfoPage")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonFirstName"))))));
    }

    private Map<Object, Object> getIdsByReference(String query) {
        return jdbcTemplate.queryForList(query)
            .stream()
            .collect(toMap(row -> row.get("reference"), row -> row.get("id")));
    }

    private Map<Object, Object> getIdsByReference(String query, String caseTypeReference) {
        return jdbcTemplate.queryForList(query, caseTypesId.get(caseTypeReference))
            .stream()
            .collect(toMap(row -> row.get("reference"), row -> row.get("id")));
    }

}
