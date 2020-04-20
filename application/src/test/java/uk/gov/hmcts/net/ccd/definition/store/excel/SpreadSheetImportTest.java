package uk.gov.hmcts.net.ccd.definition.store.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

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

    private static final String RESPONSE_JSON = "GetCaseTypesResponseForCCD_TestDefinition_V45.json";

    private Map<Object, Object> caseTypesId;
    private Map<Object, Object> fieldTypesId;

    /**
     * API test for successful import of a valid Case Definition spreadsheet.
     *
     * @throws Exception On error running test
     */
    @Test
    @Transactional
    public void importValidDefinitionFile() throws Exception {

        try (final InputStream inputStream =
                 new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_URL)
                .file(file)
                .header(AUTHORIZATION, "Bearer testUser")) //
                .andReturn();

            assertResponseCode(mvcResult, HttpStatus.SC_CREATED);
        }

        final String expectedUserProfiles = "[{\"id\":\"user1@hmcts.net\"," +
            "\"work_basket_default_jurisdiction\":\"TEST\"," +
            "\"work_basket_default_case_type\":\"TestAddressBookCase\"," +
            "\"work_basket_default_state\":\"CaseCreated\"}," + //
            "{\"id\":\"UseR2@hmcts.net\"," + "\"work_basket_default_jurisdiction\":\"TEST\"," +
            "\"work_basket_default_case_type\":\"TestAddressBookCase\"," +
            "\"work_basket_default_state\":\"CaseEnteredIntoLegacy\"}]";
        WireMock.verify(1,
                        putRequestedFor(urlEqualTo("/user-profile/users")).withRequestBody(equalTo(expectedUserProfiles)));

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
     * @throws Exception On error running test
     */
    @Test
    @Transactional
    public void importValidDefinitionFileUserProfileHas403Response() throws Exception {
        try (final InputStream inputStream =
                 new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream()) {

            final MockMultipartFile file = new MockMultipartFile("file", inputStream);

            // Given wiremock returns http status 403
            WireMock.stubFor(WireMock.put(urlEqualTo("/user-profile/users"))
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
    }

    /**
     * API test for failure to import a Case Definition spreadsheet, due to invalid data format. (This means one or more
     * workbook sheets did not contain a Definition name in Cell A1, and/or were missing data attribute headers.)
     *
     * @throws Exception On error running test
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
     * @throws Exception On error running test
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

        WireMock.reset();

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
    public static Matcher<Map<String, Object>> hasColumn(Matcher<String> keyMatcher, Matcher valueMatcher) {
        return hasEntry(keyMatcher, valueMatcher);
    }

    public static Matcher<Map<String, Object>> hasColumn(String key, Object value) {
        return hasColumn(is(key), is(value));
    }

    private void assertBody(String contentAsString) throws IOException, URISyntaxException {

        String expected = formatJsonString(readFileToString(new File(getClass().getClassLoader()
                                                                         .getResource(RESPONSE_JSON)
                                                                         .toURI())));
        expected = expected.replaceAll("#date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

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
        assertCaseRoles();
        assertCaseTypeACLs();
        assertSearchAliases();
    }

    private void assertJurisdiction() {
        Map<String, Object> jurisdictionRow = jdbcTemplate.queryForMap("SELECT * FROM jurisdiction");
        assertThat(jurisdictionRow,
                   allOf(hasColumn("reference", "TEST"),
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
                   allOf(hasItem(allOf(hasColumn("jurisdiction_id", getIdForTestJurisdiction()),
                                       hasColumn("reference", "FixedList-marritalStatusEnum"),
                                       hasColumn("base_field_type_id", fieldTypesId.get("FixedList")))),
                         hasItem(allOf(hasColumn("jurisdiction_id", getIdForTestJurisdiction()),
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
                   allOf(hasItem(allOf(hasColumn("jurisdiction_id", getIdForTestJurisdiction()),
                                       hasColumn("reference", "Address"),
                                       hasColumn("base_field_type_id", fieldTypesId.get("Complex")))),
                         hasItem(allOf(hasColumn("jurisdiction_id", getIdForTestJurisdiction()),
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
                   allOf(hasItem(allOf(hasColumn("jurisdiction_id", getIdForTestJurisdiction()),
                                       hasColumn("base_field_type_id", fieldTypesId.get("Collection")),
                                       hasColumn("collection_field_type_id", fieldTypesId.get("Person")),
                                       hasColumn(is("reference"), startsWith("Group-")))),
                         hasItem(allOf(hasColumn("jurisdiction_id", getIdForTestJurisdiction()),
                                       hasColumn("base_field_type_id", fieldTypesId.get("Collection")),
                                       hasColumn("collection_field_type_id", fieldTypesId.get("Text")),
                                       hasColumn(is("reference"), startsWith("Alliases-")))),
                         hasItem(allOf(hasColumn("base_field_type_id", fieldTypesId.get("Text")),
                                       hasColumn(is("collection_field_type_id"), nullValue()),
                                       hasColumn("minimum", "3"),
                                       hasColumn("maximum", "20"),
                                       hasColumn(is("reference"), startsWith("PersonLastNameWithValidation-"))))));
    }

    private void assertCaseTypeACLs() {
        List<Map<String, Object>> allCaseTypeACLs = jdbcTemplate.queryForList("SELECT * FROM case_type_acl");
        assertThat(allCaseTypeACLs, hasSize(8));

        List<Map<String, Object>> acls1 = jdbcTemplate.queryForList("SELECT * FROM case_type_acl where "
            + "case_type_id = ?", caseTypesId.get("TestAddressBookCase"));
        assertThat(acls1, hasSize(4));

        List<Map<String, Object>> acls2 = jdbcTemplate.queryForList("SELECT * FROM case_type_acl where "
            + "case_type_id = ?", caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(acls2, hasSize(4));
    }

    private void assertCaseRoles() {
        List<Map<String, Object>> allCaseRoles = jdbcTemplate.queryForList("SELECT * FROM role WHERE role.dtype = 'CASEROLE'");
        assertThat(allCaseRoles, hasSize(6));

        List<Map<String, Object>> caseTypeCaseRoles = jdbcTemplate.queryForList("SELECT * FROM role where "
            + "case_type_id = ?", caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(caseTypeCaseRoles, allOf(
            hasItem(allOf(
                hasColumn("name", "Claimant"),
                hasColumn("description", "The person created the case"),
                hasColumn("reference", "[Claimant]".toUpperCase()))),
            hasItem(allOf(
                hasColumn("name", "Defendant"),
                hasColumn("description", "The defending person"),
                hasColumn("reference", "[Defendant]".toUpperCase()))),
            hasItem(allOf(
                hasColumn("name", "Claimant solicitor"),
                hasColumn("description", "The claiming solicitor"),
                hasColumn("reference", "[ClaimantSolicitor]".toUpperCase()))),
            hasItem(allOf(
                hasColumn("name", "Defendant solicitor"),
                hasColumn("description", "The defending solicitor"),
                hasColumn("reference", "[DefendantSolicitor]".toUpperCase())))
        ));
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

        Map<Object, Object> userRoleIds = getIdsByReference("SELECT reference, id FROM role WHERE role.dtype = 'USERROLE'");
        List<Map<String, Object>> caseTypeWorkbasket = jdbcTemplate.queryForList(
            "SELECT * FROM workbasket_input_case_field where case_type_id = ?",
            caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(caseTypeWorkbasket,
                   allOf(hasItem(allOf(hasColumn("label", "First Name"),
                                       hasColumn("display_order", 1),
                                       hasColumn("role_id", userRoleIds.get("CaseWorker1")),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonFirstName")))
                         ),
                         hasItem(allOf(hasColumn("label", "Last Name"),
                                       hasColumn("display_order", 2),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonLastName"))))));
    }

    private void assertWorkbasket(Map<Object, Object> caseFieldIds) {

        List<Map<String, Object>> allWorkbasket = jdbcTemplate.queryForList("SELECT * FROM workbasket_case_field");
        assertThat(allWorkbasket, hasSize(6));

        Map<Object, Object> userRoleIds = getIdsByReference("SELECT reference, id FROM role WHERE role.dtype = 'USERROLE'");
        List<Map<String, Object>> caseTypeWorkbasket = jdbcTemplate.queryForList(
            "SELECT * FROM workbasket_case_field where case_type_id = ?",
            caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(caseTypeWorkbasket,
                   allOf(hasItem(allOf(hasColumn("label", "Contect Number"),
                                       hasColumn("display_order", 2),
                                       hasColumn("role_id", userRoleIds.get("CaseWorker1")),
                                       hasColumn("case_field_id", caseFieldIds.get("ContectNumber")))
                         ),
                         hasItem(allOf(hasColumn("label", "Age"),
                                       hasColumn("display_order", 3),
                                       hasColumn("case_field_id", caseFieldIds.get("Age"))))));
    }

    private void assertSearchInput(Map<Object, Object> caseFieldIds) {
        List<Map<String, Object>> allWorkbasket = jdbcTemplate.queryForList("SELECT * FROM search_input_case_field");
        assertThat(allWorkbasket, hasSize(13));

        Map<Object, Object> userRoleIds = getIdsByReference("SELECT reference, id FROM role WHERE role.dtype = 'USERROLE'");
        List<Map<String, Object>> caseTypeWorkbasket = jdbcTemplate.queryForList(
            "SELECT * FROM search_input_case_field where case_type_id = ?",
            caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(caseTypeWorkbasket,
                   allOf(hasItem(allOf(hasColumn("label", "First Name"),
                                       hasColumn("display_order", 1),
                                       hasColumn("role_id", userRoleIds.get("CaseWorker1")),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonFirstName")))
                         ),
                         hasItem(allOf(hasColumn("label", "Last Name"),
                                       hasColumn("display_order", 2),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonLastName"))))));
    }

    private void assertSearchResult(Map<Object, Object> caseFieldIds) {
        List<Map<String, Object>> allWorkbasket = jdbcTemplate.queryForList("SELECT * FROM search_result_case_field");
        assertThat(allWorkbasket, hasSize(7));

        Map<Object, Object> userRoleIds = getIdsByReference("SELECT reference, id FROM role WHERE role.dtype = 'USERROLE'");
        List<Map<String, Object>> caseTypeWorkbasket = jdbcTemplate.queryForList(
            "SELECT * FROM search_result_case_field where case_type_id = ?",
            caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(caseTypeWorkbasket,
                   allOf(hasItem(allOf(hasColumn("label", "Date of Birth"),
                                       hasColumn("display_order", 2),
                                       hasColumn("role_id", userRoleIds.get("CaseWorker1")),
                                       hasColumn("case_field_id", caseFieldIds.get("DateOfBirth")))
                         ),
                         hasItem(allOf(hasColumn("label", "Contact Email"),
                                       hasColumn("display_order", 3),
                                       hasColumn("case_field_id", caseFieldIds.get("ContectEmail"))))));
    }

    private void assertCaseTypeTab() {
        List<Map<String, Object>> allDisplayGroups = jdbcTemplate.queryForList(
            "SELECT * FROM display_group WHERE type = 'TAB'");
        assertThat(allDisplayGroups, hasSize(14));

        Map<Object, Object> userRoleIds = getIdsByReference("SELECT reference, id FROM role WHERE role.dtype = 'USERROLE'");
        List<Map<String, Object>> caseTypeDisplayGroup = jdbcTemplate.queryForList(
            "SELECT * FROM display_group WHERE case_type_id = ? AND type = 'TAB'",
            caseTypesId.get("TestAddressBookCase"));
        assertThat(caseTypeDisplayGroup,
                   allOf(hasItem(allOf(hasColumn("reference", "NameTab"),
                                       hasColumn("label", "Name"),
                                       hasColumn("display_order", 1),
                                       hasColumn("role_id", userRoleIds.get("CaseWorker1")),
                                       hasColumn("show_condition", "PersonLastName=\"Sparrow\""))

                         ),
                         hasItem(allOf(hasColumn("reference", "AddressTab"),
                                       hasColumn("label", "Address"),
                                       hasColumn("display_order", 2)))));


        Map<Object, Object> caseFieldIds = getIdsByReference(
            "SELECT reference, id FROM case_field where case_type_id = ?",
            "TestAddressBookCase");
        Map<Object, Object> displayGroupsId = getIdsByReference(
            "SELECT reference, id FROM display_group where case_type_id = ?",
            "TestAddressBookCase");

        List<Map<String, Object>> displayGroupsFields = jdbcTemplate.queryForList(
            "select dgcf.* from display_group_case_field dgcf, display_group dg where dgcf.display_group_id = dg.id "
                + "AND dg.type = 'TAB' AND case_type_id = ?", caseTypesId.get("TestAddressBookCase"));

        assertThat(displayGroupsFields, hasSize(6));
        assertThat(displayGroupsFields,
                   allOf(hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("NameTab")),
                                       hasColumn("display_order", 2),
                                       hasColumn("show_condition", "PersonFirstName=\"Jack\""),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonLastName")))),
                         hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("NameTab")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonFirstName")))),
                         hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("AddressTab")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonAddress")))),
                         hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("ReferenceCollectionTab")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", caseFieldIds.get("referenceCollection")))),
                         hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("PaymentTab")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id",
                                                 caseFieldIds.get("PersonCasePaymentHistoryViewer")))),
                         hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("HistoryTab")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id",
                                                 caseFieldIds.get("CaseHistoryViewer"))))));

        List<Map<String, Object>> complexCaseTypeDisplayGroup = jdbcTemplate.queryForList(
            "SELECT * FROM display_group WHERE case_type_id = ? AND type = 'TAB'",
            caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(complexCaseTypeDisplayGroup,
                   allOf(hasItem(allOf(hasColumn("reference", "NameTab"),
                                       hasColumn("label", "Name"),
                                       hasColumn("display_order", 1))

                         ),
                         hasItem(allOf(hasColumn("reference", "ContectEmail"),
                                       hasColumn("label", "Details"),
                                       hasColumn("display_order", 3)))));

        Map<Object, Object> complexCaseFieldIds = getIdsByReference(
            "SELECT reference, id FROM case_field where case_type_id = ?",
            "TestComplexAddressBookCase");
        Map<Object, Object> complexDisplayGroupsId = getIdsByReference(
            "SELECT reference, id FROM display_group where case_type_id = ?",
            "TestComplexAddressBookCase");

        List<Map<String, Object>> complexDisplayGroupsFields = jdbcTemplate.queryForList(
            "select dgcf.* from display_group_case_field dgcf, display_group dg where dgcf.display_group_id = dg.id "
                + "AND dg.type = 'TAB'  AND case_type_id = ?", caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(complexDisplayGroupsFields, hasSize(10));
        assertThat(complexDisplayGroupsFields,
                   allOf(hasItem(allOf(hasColumn("display_group_id", complexDisplayGroupsId.get("NameTab")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", complexCaseFieldIds.get("PersonFirstName")))),
                         hasItem(allOf(hasColumn("display_group_id", complexDisplayGroupsId.get("NameTab")),
                                       hasColumn("display_order", 2),
                                       hasColumn("case_field_id", complexCaseFieldIds.get("PersonLastName")))),
                         hasItem(allOf(hasColumn("display_group_id", complexDisplayGroupsId.get("ContectEmail")),
                                       hasColumn("display_order", 4),
                                       hasColumn("case_field_id", complexCaseFieldIds.get("ContectEmail"))))));

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
                                       hasColumn("show_condition", "HasOtherInfo=\"No\""),
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
        assertThat(displayGroupsFields, hasSize(13));
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
                                       hasColumn("case_field_id", caseFieldIds.get("PersonFirstName")))),
                         hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("createCasePage1")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonOrderSummary")))),
                         hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("createCasePage1")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", caseFieldIds.get("PersonCasePaymentHistoryViewer")))),
                         hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("createCasePage1")),
                                       hasColumn("display_order", 1),
                                       hasColumn("case_field_id", caseFieldIds.get("CaseHistoryViewer")))),
                       hasItem(allOf(hasColumn("display_group_id", displayGroupsId.get("createCasePage1")),
                           hasColumn("display_order", 1),
                           hasColumn("case_field_id", caseFieldIds.get("referenceCollection"))))
                        )
                    );
    }

    private void assertSearchAliases() {
        List<Map<String, Object>> searchAliasDefinition = jdbcTemplate.queryForList("SELECT * FROM search_alias_field");
        assertThat(searchAliasDefinition, hasSize(3));

        List<Map<String, Object>> caseTypeSearchAliasDefinition = jdbcTemplate.queryForList(
            "SELECT * FROM search_alias_field where case_type_id = ?",
            caseTypesId.get("TestComplexAddressBookCase"));
        assertThat(caseTypeSearchAliasDefinition,
                   allOf(hasItem(allOf(hasColumn("reference", "nameAlias"),
                                       hasColumn("case_field_path", "PersonLastName"))
                         ),
                         hasItem(allOf(hasColumn("reference", "postcodeAlias"),
                                       hasColumn("case_field_path", "PersonAddress.Postcode")))));
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

    private int getIdForTestJurisdiction() {
        return jdbcTemplate.queryForObject("SELECT id FROM jurisdiction WHERE reference = 'TEST' AND version = 1",
            Integer.class);
    }
}
