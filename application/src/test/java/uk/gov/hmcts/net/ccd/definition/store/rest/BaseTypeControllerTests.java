package uk.gov.hmcts.net.ccd.definition.store.rest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BaseTypeControllerTests extends BaseTest {

    private static final String GET_BASE_TYPES = "/api/base-types";

    private static final String POST_CODE_REGEX =
        "^([A-PR-UWYZ0-9][A-HK-Y0-9][AEHMNPRTVXY0-9]?[ABEHMNPRVWXY0-9]? {1,2}[0-9][ABD-HJLN-UW-Z]{2}|GIR 0AA)$";

    private static final String PHONE_NUMBER_REGEX = "^(((\\+44\\s?\\d{4}|\\(?0\\d{4}\\)?)\\s?\\d{3}\\s?\\d{3})"
        + "|((\\+44\\s?\\d{3}|\\(?0\\d{3}\\)?)\\s?\\d{3}\\s?\\d{4})"
        + "|((\\+44\\s?\\d{2}|\\(?0\\d{2}\\)?)\\s?\\d{4}\\s?\\d{4}))(\\s?\\#(\\d{4}|\\d{3}))?$";

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getBaseTypesTest() throws Exception {

        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(GET_BASE_TYPES))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        FieldType[] baseTypes = BaseTest.mapper.readValue(result.getResponse().getContentAsString(), FieldType[].class);

        assertEquals("Unexpected number of Base Types", 20, baseTypes.length);

        assertEquals("Unexpected Base Type", "Text", baseTypes[0].getType());
        assertEquals("Unexpected Base Type Id", "Text", baseTypes[0].getId());
        assertNull("Unexpected Reg Ex", baseTypes[0].getRegularExpression());

        assertEquals("Unexpected Base Type", "Number", baseTypes[1].getType());
        assertEquals("Unexpected Base Type Id", "Number", baseTypes[1].getId());
        assertNull("Unexpected Reg Ex", baseTypes[1].getRegularExpression());

        assertEquals("Unexpected Base Type", "Email", baseTypes[2].getType());
        assertEquals("Unexpected Base Type Id", "Email", baseTypes[2].getId());
        assertNull("Unexpected Reg Ex", baseTypes[2].getRegularExpression());

        assertEquals("Unexpected Base Type", "YesOrNo", baseTypes[3].getType());
        assertEquals("Unexpected Base Type Id", "YesOrNo", baseTypes[3].getId());
        assertNull("Unexpected Reg Ex", baseTypes[3].getRegularExpression());

        assertEquals("Unexpected Base Type", "Date", baseTypes[4].getType());
        assertEquals("Unexpected Base Type Id", "Date", baseTypes[4].getId());
        assertNull("Unexpected Reg Ex", baseTypes[4].getRegularExpression());

        assertEquals("Unexpected Base Type", "FixedList", baseTypes[5].getType());
        assertEquals("Unexpected Base Type Id", "FixedList", baseTypes[5].getId());
        assertNull("Unexpected Reg Ex", baseTypes[5].getRegularExpression());

        assertEquals("Unexpected Base Type", "Postcode", baseTypes[6].getType());
        assertEquals("Unexpected Base Type Id", "Postcode", baseTypes[6].getId());
        assertEquals("Unexpected Reg Ex", POST_CODE_REGEX, baseTypes[6].getRegularExpression());

        assertEquals("Unexpected Base Type", "MoneyGBP", baseTypes[7].getType());
        assertEquals("Unexpected Base Type Id", "MoneyGBP", baseTypes[7].getId());
        assertNull("Unexpected Reg Ex", baseTypes[7].getRegularExpression());

        assertEquals("Unexpected Base Type", "PhoneUK", baseTypes[8].getType());
        assertEquals("Unexpected Base Type Id", "PhoneUK", baseTypes[8].getId());
        assertEquals("Unexpected Reg Ex", PHONE_NUMBER_REGEX, baseTypes[8].getRegularExpression());

        assertEquals("Unexpected Base Type", "TextArea", baseTypes[9].getType());
        assertEquals("Unexpected Base Type Id", "TextArea", baseTypes[9].getId());
        assertNull("Unexpected Reg Ex", baseTypes[9].getRegularExpression());

        assertEquals("Unexpected Base Type", "Complex", baseTypes[10].getType());
        assertEquals("Unexpected Base Type Id", "Complex", baseTypes[10].getId());
        assertNull("Unexpected Reg Ex", baseTypes[10].getRegularExpression());

        assertEquals("Unexpected Base Type", "Collection", baseTypes[11].getType());
        assertEquals("Unexpected Base Type Id", "Collection", baseTypes[11].getId());
        assertNull("Unexpected Reg Ex", baseTypes[11].getRegularExpression());

        assertEquals("Unexpected Base Type", "MultiSelectList", baseTypes[12].getType());
        assertEquals("Unexpected Base Type Id", "MultiSelectList", baseTypes[12].getId());
        assertNull("Unexpected Reg Ex", baseTypes[12].getRegularExpression());

        assertEquals("Unexpected Base Type", "Document", baseTypes[13].getType());
        assertEquals("Unexpected Base Type Id", "Document", baseTypes[13].getId());
        assertNull("Unexpected Reg Ex", baseTypes[13].getRegularExpression());

        assertEquals("Unexpected Base Type", "Label", baseTypes[14].getType());
        assertEquals("Unexpected Base Type Id", "Label", baseTypes[14].getId());
        assertNull("Unexpected Reg Ex", baseTypes[14].getRegularExpression());

        assertEquals("Unexpected Base Type", "DateTime", baseTypes[15].getType());
        assertEquals("Unexpected Base Type Id", "DateTime", baseTypes[15].getId());
        assertNull("Unexpected Reg Ex", baseTypes[15].getRegularExpression());

        assertEquals("Unexpected Base Type", "CasePaymentHistoryViewer", baseTypes[16].getType());
        assertEquals("Unexpected Base Type Id", "CasePaymentHistoryViewer", baseTypes[16].getId());
        assertNull("Unexpected Reg Ex", baseTypes[16].getRegularExpression());

        assertEquals("Unexpected Base Type", "FixedRadioList", baseTypes[17].getType());
        assertEquals("Unexpected Base Type Id", "FixedRadioList", baseTypes[17].getId());
        assertNull("Unexpected Reg Ex", baseTypes[17].getRegularExpression());

        assertEquals("Unexpected Base Type", "CaseHistoryViewer", baseTypes[18].getType());
        assertEquals("Unexpected Base Type Id", "CaseHistoryViewer", baseTypes[18].getId());
        assertNull("Unexpected Reg Ex", baseTypes[18].getRegularExpression());

        assertEquals("Unexpected Base Type", "DynamicList", baseTypes[19].getType());
        assertEquals("Unexpected Base Type Id", "DynamicList", baseTypes[19].getId());
        assertNull("Unexpected Reg Ex", baseTypes[19].getRegularExpression());


    }
}
