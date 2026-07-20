package uk.gov.hmcts.net.ccd.definition.store.rest;


import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseTypeControllerTests extends BaseTest {

    private static final String GET_BASE_TYPES = "/api/base-types";

    private static final String POST_CODE_REGEX =
        "^([A-PR-UWYZ0-9][A-HK-Y0-9][AEHMNPRTVXY0-9]?[ABEHMNPRVWXY0-9]? {1,2}[0-9][ABD-HJLN-UW-Z]{2}|GIR 0AA)$";

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
