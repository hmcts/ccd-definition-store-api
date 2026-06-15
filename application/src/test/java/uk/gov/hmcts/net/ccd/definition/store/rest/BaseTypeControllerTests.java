package uk.gov.hmcts.net.ccd.definition.store.rest;


import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseTypeControllerTests extends BaseTest {

    private static final String GET_BASE_TYPES = "/api/base-types";

    private static final String POST_CODE_REGEX =
        "^([A-PR-UWYZ][A-HK-Y0-9][AC-HJKMNPR-VXY0-9]?[ABEHMNPRVWXY0-9]? {1,2}[0-9][ABD-HJLN-UW-Z]{2}|GIR 0AA)$";

    private static final String PHONE_NUMBER_REGEX = "^(((\\+44\\s?\\d{4}|\\(?0\\d{4}\\)?)\\s?\\d{3}\\s?\\d{3})"
        + "|((\\+44\\s?\\d{3}|\\(?0\\d{3}\\)?)\\s?\\d{3}\\s?\\d{4})"
        + "|((\\+44\\s?\\d{2}|\\(?0\\d{2}\\)?)\\s?\\d{4}\\s?\\d{4}))(\\s?\\#(\\d{4}|\\d{3}))?$";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void getBaseTypesTest() throws Exception {

        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(GET_BASE_TYPES))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        FieldType[] baseTypes = BaseTest.mapper.readValue(result.getResponse().getContentAsString(), FieldType[].class);

        assertEquals(27, baseTypes.length, "Unexpected number of Base Types");
        assertContainsFieldType(baseTypes, "Text", "Text");
        assertContainsFieldType(baseTypes, "Number", "Number");
        assertContainsFieldType(baseTypes, "Email", "Email");
        assertContainsFieldType(baseTypes, "YesOrNo", "YesOrNo");
        assertContainsFieldType(baseTypes, "Date", "Date");
        assertContainsFieldType(baseTypes, "FixedList", "FixedList");
        assertContainsFieldType(baseTypes, "Postcode", "Postcode", POST_CODE_REGEX);
        assertContainsFieldType(baseTypes, "MoneyGBP", "MoneyGBP");
        assertContainsFieldType(baseTypes, "PhoneUK", "PhoneUK", PHONE_NUMBER_REGEX);
        assertContainsFieldType(baseTypes, "TextArea", "TextArea");
        assertContainsFieldType(baseTypes, "Complex", "Complex");
        assertContainsFieldType(baseTypes, "Collection", "Collection");
        assertContainsFieldType(baseTypes, "MultiSelectList", "MultiSelectList");
        assertContainsFieldType(baseTypes, "Document", "Document");
        assertContainsFieldType(baseTypes, "Label", "Label");
        assertContainsFieldType(baseTypes, "DateTime", "DateTime");
        assertContainsFieldType(baseTypes, "CasePaymentHistoryViewer", "CasePaymentHistoryViewer");
        assertContainsFieldType(baseTypes, "FixedRadioList", "FixedRadioList");
        assertContainsFieldType(baseTypes, "CaseHistoryViewer", "CaseHistoryViewer");
        assertContainsFieldType(baseTypes, "DynamicList", "DynamicList");
        assertContainsFieldType(baseTypes, "Region", "Region");
        assertContainsFieldType(baseTypes, "BaseLocation", "BaseLocation");
        assertContainsFieldType(baseTypes, "DynamicRadioList", "DynamicRadioList");
        assertContainsFieldType(baseTypes, "DynamicMultiSelectList", "DynamicMultiSelectList");
        assertContainsFieldType(baseTypes, "WaysToPay", "WaysToPay");
        assertContainsFieldType(baseTypes, "FlagLauncher", "FlagLauncher");
        assertContainsFieldType(baseTypes, "ComponentLauncher", "ComponentLauncher");
    }

    // CCD-2008: behavioural regression guard for the Postcode base type.
    // getBaseTypesTest above asserts the endpoint serves the expected regex *string*; this test
    // proves that the served regex actually accepts valid UK postcodes (e.g. the reported W1U 3BW)
    // and still rejects malformed ones - the user-facing behaviour the regex fix was about.
    // The valid samples deliberately include third-position letters (C/D/U) that the pre-fix regex
    // wrongly rejected, plus the GIR 0AA special case; the invalid samples include a digit-first
    // outward code which the pre-fix regex wrongly accepted.
    private static final String[] VALID_UK_POSTCODES = {
        "W1U 3BW", "W1D 4FA", "N1C 4AG", "EC1A 1BB", "SW1A 1AA",
        "M1 1AE", "B33 8TH", "CR2 6XH", "DN55 1PT", "WC1B 3DG", "GIR 0AA"
    };

    private static final String[] INVALID_UK_POSTCODES = {
        "0X1 1AA",      // outward code must not start with a digit
        "W1U3BW",       // missing the separating space
        "LONDON",       // not a postcode
        "12345",        // numeric only
        "W1"            // incomplete
    };

    @Test
    void postcodeRegexAcceptsValidUkPostcodesAndRejectsInvalidOnes() throws Exception {
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(GET_BASE_TYPES))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        FieldType[] baseTypes = BaseTest.mapper.readValue(result.getResponse().getContentAsString(), FieldType[].class);

        final String postcodeRegex = Stream.of(baseTypes)
            .filter(baseType -> "Postcode".equals(baseType.getId()))
            .map(FieldType::getRegularExpression)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Postcode base type not found in /api/base-types response"));

        final Pattern pattern = Pattern.compile(postcodeRegex);

        for (String postcode : VALID_UK_POSTCODES) {
            assertTrue(pattern.matcher(postcode).matches(),
                "Expected valid UK postcode to be accepted by the Postcode regex but it was rejected: " + postcode);
        }
        for (String postcode : INVALID_UK_POSTCODES) {
            assertFalse(pattern.matcher(postcode).matches(),
                "Expected invalid postcode to be rejected by the Postcode regex but it was accepted: " + postcode);
        }
    }

    private void assertContainsFieldType(FieldType[] baseTypes, String id, String type) {
        assertTrue(Stream.of(baseTypes)
            .anyMatch(baseType -> baseType.getType().equals(type) && baseType.getId().equals(id)),
            "Base Type not found: " + id + " with Type: " + type);
    }

    private void assertContainsFieldType(FieldType[] baseTypes, String id, String type, String regex) {
        assertTrue(Stream.of(baseTypes)
            .anyMatch(baseType -> baseType.getType().equals(type)
                && baseType.getId().equals(id)
                && baseType.getRegularExpression().equals(regex)),
            "Base Type not found: " + id + " with Type: " + type + " and Regex: " + regex);
    }
}
