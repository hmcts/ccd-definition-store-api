package uk.gov.hmcts.net.ccd.definition.store.rest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;

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

        assertThat(baseTypes)
            .withFailMessage("Unexpected Base Type and Id")
            .extracting(FieldType::getId, FieldType::getType, FieldType::getRegularExpression)
            .containsExactlyInAnyOrder(
                tuple("Text", "Text", null),
                tuple("Number", "Number", null),
                tuple("Email", "Email", null),
                tuple("YesOrNo", "YesOrNo", null),
                tuple("Date", "Date", null),
                tuple("FixedList", "FixedList", null),
                tuple("Postcode", "Postcode", POST_CODE_REGEX),
                tuple("MoneyGBP", "MoneyGBP", null),
                tuple("PhoneUK", "PhoneUK", PHONE_NUMBER_REGEX),
                tuple("TextArea", "TextArea", null),
                tuple("Complex", "Complex", null),
                tuple("Collection", "Collection", null),
                tuple("MultiSelectList", "MultiSelectList", null),
                tuple("Document", "Document", null),
                tuple("Label", "Label", null),
                tuple("DateTime", "DateTime", null),
                tuple("CasePaymentHistoryViewer", "CasePaymentHistoryViewer", null),
                tuple("FixedRadioList", "FixedRadioList", null),
                tuple("CaseHistoryViewer", "CaseHistoryViewer", null),
                tuple("DynamicList", "DynamicList", null)
            );

    }
}