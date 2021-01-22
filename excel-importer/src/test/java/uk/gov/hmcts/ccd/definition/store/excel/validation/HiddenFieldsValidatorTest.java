package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HiddenFieldsValidatorTest {

    private HiddenFieldsValidator validator = new HiddenFieldsValidator();
    private Map<String, DefinitionSheet> definitionSheets;

    @Before
    public void setup() {
        definitionSheets = new LinkedHashMap<>();
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenNoShowConditionInCaseEventToFields() {
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");

        try {
            validator.parseHiddenFields(definitionDataItem);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(), is("'retainHiddenValue' can only be configured for a field that uses a "
                + "showCondition. Field ['fieldId'] on ['CaseEventToFields'] does not use a showCondition"));
            throw ex;
        }
    }

    @Test(expected = Test.None.class)
    public void shouldValidate_ForCaseEventToFields() {
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");

        assertTrue(validator.parseHiddenFields(definitionDataItem));
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenInvalidBoolean() {
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, "blah");
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");

        try {
            validator.parseHiddenFields(definitionDataItem);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(), is(
                "Invalid value 'blah' is found in column 'RetainHiddenValue' in the sheet 'CaseEventToFields'"));
            throw ex;
        }
    }

    @Test(expected = Test.None.class)
    public void shouldValidate_ForComplexType() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.FIELD_TYPE, "Text");
        sheetComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertTrue(validator.parseComplexTypesHiddenFields(definitionDataItem, definitionSheets));

    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenRetainHiddenValueIsFalseInCaseEventToFieldsButTrueForComplexTypes() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.FIELD_TYPE, "Text");
        sheetComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.FALSE);
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        try {
            validator.parseComplexTypesHiddenFields(definitionDataItem, definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(), is("'retainHiddenValue' has been incorrectly configured or is invalid "
                + "for fieldID ['fieldId'] on ['CaseEventToFields']"));
            throw ex;
        }
    }


    @Test(expected = Test.None.class)
    public void shouldValidate_whenMultipleReferencesOfCaseFieldsInCaseEvents() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.FIELD_TYPE, "Text");
        sheetComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem3);

        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "abc=123");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, true);
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertTrue(validator.parseComplexTypesHiddenFields(definitionDataItem, definitionSheets));
    }

    @Test(expected = Test.None.class)
    public void shouldValidate_whenMultipleReferencesOfDifferentCaseFieldsInCaseEvents() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.FIELD_TYPE, "Text");
        sheetComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem4 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem4.addAttribute(ColumnName.CASE_FIELD_ID, "fieldIdTwo");
        definitionDataItem4.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem4.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem4);

        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem3);

        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "abc=123");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, true);
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem5 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem5.addAttribute(ColumnName.ID, "fieldIdTwo");
        definitionDataItem5.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem5);

        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertTrue(validator.parseComplexTypesHiddenFields(definitionDataItem, definitionSheets));
    }

    @Test(expected = Test.None.class)
    public void shouldValidate_whenMultipleReferencesOfDifferentCaseFieldsInCaseEventsForNestedComplex() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldIdNested");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexTypeNested");
        definitionDataItem.addAttribute(ColumnName.FIELD_TYPE, "Text");
        sheetComplexTypes.addDataItem(definitionDataItem);

        DefinitionDataItem definitionDataItem6 = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem6.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem6.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem6.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem6.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem6.addAttribute(ColumnName.FIELD_TYPE, "ComplexTypeNested");
        sheetComplexTypes.addDataItem(definitionDataItem6);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem4 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem4.addAttribute(ColumnName.CASE_FIELD_ID, "fieldIdTwo");
        definitionDataItem4.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem4.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem4);

        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem3);

        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "abc=123");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, true);
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem5 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem5.addAttribute(ColumnName.ID, "fieldIdTwo");
        definitionDataItem5.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem5);

        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertTrue(validator.parseComplexTypesHiddenFields(definitionDataItem, definitionSheets));
    }

    @Test(expected = Test.None.class)
    public void shouldValidate_whenMultipleReferencesOfDifferentCaseFieldsInCaseEventsCollection() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.FIELD_TYPE, "Text");
        sheetComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem4 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem4.addAttribute(ColumnName.CASE_FIELD_ID, "fieldIdTwo");
        definitionDataItem4.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem4.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem4);

        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem3);

        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldIdTwo");
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "abc=123");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, true);
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        DefinitionDataItem definitionDataItem5 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem5.addAttribute(ColumnName.ID, "fieldIdTwo");
        definitionDataItem5.addAttribute(ColumnName.FIELD_TYPE_PARAMETER, "ComplexType");
        definitionDataItem5.addAttribute(ColumnName.FIELD_TYPE, "Collection");
        sheetCF.addDataItem(definitionDataItem5);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertTrue(validator.parseComplexTypesHiddenFields(definitionDataItem, definitionSheets));
    }

    @Test(expected = Test.None.class)
    public void shouldValidate_whenMultipleReferencesOfDifferentCaseFieldsInCaseEvents2() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.FIELD_TYPE, "Text");
        sheetComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem3);

        DefinitionDataItem definitionDataItem4 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem4.addAttribute(ColumnName.CASE_FIELD_ID, "fieldIdTwo");
        definitionDataItem4.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem4.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem4);

        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "abc=123");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, true);
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem5 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem5.addAttribute(ColumnName.ID, "fieldIdTwo");
        definitionDataItem5.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem5);

        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertTrue(validator.parseComplexTypesHiddenFields(definitionDataItem, definitionSheets));
    }

    @Test(expected = Test.None.class)
    public void shouldValidate_whenCaseFieldsInCaseEventHasNoShowConditionOrRetainHiddenValue() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.FALSE);
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "abc=123");
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        sheetComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem3);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertFalse(validator.parseComplexTypesHiddenFields(definitionDataItem, definitionSheets));
    }


    @Test(expected = MapperException.class)
    public void shouldFail_whenCaseFieldsInCaseEventHasRetainHiddenValueOfNullAtTopLevel() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.FIELD_TYPE, "Text");
        sheetComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "abc=123");
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem3);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        try {
            validator.parseComplexTypesHiddenFields(definitionDataItem, definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(), is("'retainHiddenValue' has been incorrectly configured or is invalid "
                + "for fieldID ['fieldId'] on ['CaseEventToFields']"));
            throw ex;
        }
    }

    @Test(expected = Test.None.class)
    public void shouldValidate_whenCaseFieldsInCaseEventHasRetainHiddenValueOfNull() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.FALSE);
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        sheetComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "abc=123");
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        sheetCETF.addDataItem(definitionDataItem3);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertFalse(validator.parseComplexTypesHiddenFields(definitionDataItem, definitionSheets));
    }


    @Test(expected = MapperException.class)
    public void shouldFail_whenRetainHiddenValueIsNotABooleanForSubFieldsOfComplexType() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, "blah");
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        sheetComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.FALSE);
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        try {
            validator.parseComplexTypesHiddenFields(definitionDataItem, definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(),
                is("Invalid value 'blah' is found in column 'RetainHiddenValue' in the sheet 'ComplexTypes'"));
            throw ex;
        }
    }

    @Test(expected = Test.None.class)
    public void shouldValidate_ForCaseEventComplexType() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetEventComplexTypes = addDefinitionSheet(SheetName.CASE_EVENT_TO_COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.FALSE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        sheetEventComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.FALSE);
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem3.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem3.addAttribute(ColumnName.ID, "ComplexType");
        sheetComplexTypes.addDataItem(definitionDataItem3);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem1.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertFalse(validator.parseCaseEventComplexTypesHiddenFields(definitionDataItem, definitionSheets));

    }

    @Test(expected = Test.None.class)
    public void shouldValidateTrue_ForCaseEventComplexType() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetEventComplexTypes = addDefinitionSheet(SheetName.CASE_EVENT_TO_COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        sheetEventComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.FALSE);
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem3.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem3.addAttribute(ColumnName.ID, "ComplexType");
        sheetComplexTypes.addDataItem(definitionDataItem3);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem1.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertTrue(validator.parseCaseEventComplexTypesHiddenFields(definitionDataItem, definitionSheets));

    }

    @Test(expected = Test.None.class)
    public void shouldValidateToNull_ForCaseEventComplexType() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetEventComplexTypes = addDefinitionSheet(SheetName.CASE_EVENT_TO_COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        sheetEventComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.FALSE);
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem3.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem3.addAttribute(ColumnName.ID, "ComplexType");
        sheetComplexTypes.addDataItem(definitionDataItem3);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem1.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertNull(validator.parseCaseEventComplexTypesHiddenFields(definitionDataItem, definitionSheets));

    }

    @Test(expected = Test.None.class)
    public void shouldValidateToFalse_ForCaseEventComplexType() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetEventComplexTypes = addDefinitionSheet(SheetName.CASE_EVENT_TO_COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.FALSE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        sheetEventComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem3.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem3.addAttribute(ColumnName.ID, "ComplexType");
        sheetComplexTypes.addDataItem(definitionDataItem3);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem1.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        assertFalse(validator.parseCaseEventComplexTypesHiddenFields(definitionDataItem, definitionSheets));

    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenRetainHiddenValueIsInvalid() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetEventComplexTypes = addDefinitionSheet(SheetName.CASE_EVENT_TO_COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.CASE_EVENT_TO_COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, "blah");
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        sheetEventComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.FALSE);
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem3.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem3.addAttribute(ColumnName.ID, "ComplexType");
        sheetComplexTypes.addDataItem(definitionDataItem3);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.FALSE);
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "x=yes");
        definitionDataItem1.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "ComplexTypeFieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        try {
            validator.parseCaseEventComplexTypesHiddenFields(definitionDataItem, definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(),
                is("Invalid value 'blah' is found in column "
                    + "'RetainHiddenValue' in the sheet 'EventToComplexTypes'"));
            throw ex;
        }
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenRetainHiddenValueIsNotSetForComplexType() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetEventComplexTypes = addDefinitionSheet(SheetName.CASE_EVENT_TO_COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.CASE_EVENT_TO_COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem.addAttribute(ColumnName.ID, "ComplexType");
        definitionDataItem.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        definitionDataItem.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "abc=123");
        definitionDataItem.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        sheetEventComplexTypes.addDataItem(definitionDataItem);

        final DefinitionSheet sheetComplexTypes = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem3 = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem3.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem3.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, null);
        definitionDataItem3.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem3.addAttribute(ColumnName.LIST_ELEMENT_CODE, "");
        definitionDataItem3.addAttribute(ColumnName.ID, "ComplexType");
        sheetComplexTypes.addDataItem(definitionDataItem3);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.CASE_EVENT_TO_FIELDS);
        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem1.addAttribute(ColumnName.CASE_FIELD_ID, "ComplexTypeFieldId");
        definitionDataItem1.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, Boolean.TRUE);
        definitionDataItem1.addAttribute(ColumnName.FIELD_SHOW_CONDITION, null);
        definitionDataItem1.addAttribute(ColumnName.CASE_EVENT_ID, "eventId");
        sheetCETF.addDataItem(definitionDataItem1);

        final DefinitionSheet sheetCF = addDefinitionSheet(SheetName.CASE_FIELD);
        DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        definitionDataItem2.addAttribute(ColumnName.ID, "fieldId");
        definitionDataItem2.addAttribute(ColumnName.FIELD_TYPE, "ComplexType");
        sheetCF.addDataItem(definitionDataItem2);

        addDefinitionSheet(SheetName.FIXED_LISTS);

        try {
            validator.parseCaseEventComplexTypesHiddenFields(definitionDataItem, definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(),
                is("'retainHiddenValue' on CaseEventToComplexTypes can only be configured for a field that "
                    + "uses a showCondition. Field ['ComplexTypeFieldId'] on ['CaseEventToFields'] does not use "
                    + "a showCondition"));
            throw ex;
        }

    }


    private DefinitionSheet addDefinitionSheet(SheetName sheetName) {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(sheetName.toString());
        definitionSheets.put(sheetName.getName(), sheet);
        return sheet;
    }

    private void addDataItem(final DefinitionSheet sheetCT) {
        sheetCT.addDataItem(new DefinitionDataItem("ngitb"));
    }

}
