package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORDER_SUMMARY;

public class TabShowConditionValidatorImplTest {
    private static final List<DisplayGroupEntity> UNUSED_DISPLAY_GROUPS = com.google.common.collect.Lists.newArrayList();

    @Mock
    private ShowConditionParser mockShowConditionParser;

    TabShowConditionValidatorImpl testObj;
    DisplayGroupEntity displayGroup;
    List<DisplayGroupEntity> allTabDisplayGroups;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObj = new TabShowConditionValidatorImpl(mockShowConditionParser, new CaseFieldEntityUtil());
        displayGroup = new DisplayGroupEntity();
        allTabDisplayGroups = Lists.newArrayList();
    }

//    @Nested
//    class TabFieldShowCondition {

    @Test
    @DisplayName("should not execute when tab field show condition is empty")
    public void shouldNotExecuteWhenShowConditionIsEmpty() throws InvalidShowConditionException {

        displayGroup.setType(DisplayGroupType.TAB);
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity.setShowCondition(null);
        displayGroup.addDisplayGroupCaseField(displayGroupCaseFieldEntity);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
//    @DisplayName("should not execute when tab field show condition is blank")
    public void TabFieldShowCondition_shouldNotExecuteWhenShowConditionIsBlank() throws InvalidShowConditionException {

        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity.setShowCondition("");
        displayGroup.setType(DisplayGroupType.TAB);
        displayGroup.addDisplayGroupCaseField(displayGroupCaseFieldEntity);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
//    @DisplayName("should not execute when tab show type is not tab")
    public void TabFieldShowCondition_shouldNotExecuteWhenShowTypeIsNotTab() throws InvalidShowConditionException {

        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity.setShowCondition("someShowCondition");
        displayGroup.addDisplayGroupCaseField(displayGroupCaseFieldEntity);
        displayGroup.setType(DisplayGroupType.PAGE);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
//    @DisplayName("should return no errors when tab field referenced field in other tab")
    public void TabFieldShowCondition_returnsNoValidationErrorsOnSuccessWhenReferencedFieldInOtherTab() throws InvalidShowConditionException {

        displayGroup.setType(DisplayGroupType.TAB);
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);
        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setShowCondition("someShowCondition");
        displayGroupCaseField.setCaseField(caseFieldEntity("thisField"));
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);
        allTabDisplayGroups.add(displayGroup);

        DisplayGroupCaseFieldEntity displayGroupCaseFieldOther = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldOther.setCaseField(caseFieldEntity("otherField"));
        DisplayGroupEntity otherTabDisplayGroup = new DisplayGroupEntity();
        otherTabDisplayGroup.setType(DisplayGroupType.TAB);
        otherTabDisplayGroup.addDisplayGroupCaseField(displayGroupCaseFieldOther);
        otherTabDisplayGroup.setCaseType(caseTypeEntity);
        allTabDisplayGroups.add(otherTabDisplayGroup);

        ShowCondition validParsedShowCondition = new ShowCondition.Builder().showConditionExpression("parsedSC").field("otherField").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(validParsedShowCondition);

        ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

        assertThat(result.isValid(), is(true));
    }

    @Test
//    @DisplayName("should return no errors when tab field referenced field in this tab")
    public void TabFieldShowCondition_returnsNoValidationErrorsOnSuccessWhenReferencedFieldInThisTab() throws InvalidShowConditionException {

        displayGroup.setType(DisplayGroupType.TAB);
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);

        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setCaseField(caseFieldEntity("field"));
        displayGroupCaseField.setShowCondition("someShowCondition");
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);
        allTabDisplayGroups.add(displayGroup);

        ShowCondition validParsedShowCondition = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(validParsedShowCondition);

        ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void TabFieldShowCondition_shouldValidateShowConditionForCustomComplexField() throws InvalidShowConditionException {
        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode.AddressUKCode.Country";
        String showCondition = matchingCaseFieldKey + "=\"UK\"";

        ShowCondition sc = new ShowCondition.Builder().showConditionExpression(showCondition).field(matchingCaseFieldKey).build();
        when(mockShowConditionParser.parseShowCondition(showCondition))
            .thenReturn(sc);

        displayGroup.setType(DisplayGroupType.TAB);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId1"));
        caseTypeEntity.addCaseField(caseFieldEntity(matchingCaseFieldId, exampleFieldTypeEntityWithComplexFields()));
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId2"));
        displayGroup.setCaseType(caseTypeEntity);

        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setCaseField(caseFieldEntity(matchingCaseFieldId));
        displayGroupCaseField.setShowCondition(showCondition);
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);
        allTabDisplayGroups.add(displayGroup);

        ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

        assertTrue(result.isValid());
    }

    @Test
    public void TabFieldShowCondition_shouldAddErrorForInvalidShowConditionForCustomComplexField() throws InvalidShowConditionException {
        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode";
        String invalidShowCondition = matchingCaseFieldKey + "=\"UK\"";

        ShowCondition sc = new ShowCondition.Builder().showConditionExpression(invalidShowCondition).field(matchingCaseFieldKey).build();
        when(mockShowConditionParser.parseShowCondition(invalidShowCondition))
            .thenReturn(sc);

        displayGroup.setType(DisplayGroupType.TAB);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId1"));
        caseTypeEntity.addCaseField(caseFieldEntity(matchingCaseFieldId, exampleFieldTypeEntityWithComplexFields()));
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId2"));
        displayGroup.setCaseType(caseTypeEntity);

        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setCaseField(caseFieldEntity(matchingCaseFieldId));
        displayGroupCaseField.setShowCondition(invalidShowCondition);
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);
        allTabDisplayGroups.add(displayGroup);

        ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidTabFieldShowCondition.class));
    }

    @Test
//    @DisplayName("should fail when unable to parse show condition for tab field")
    public void TabFieldShowCondition_returnsDisplayGroupInvalidShowConditionErrorWhenUnableToParseShowCondition() throws InvalidShowConditionException {

        displayGroup.setType(DisplayGroupType.TAB);
        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setShowCondition("someShowCondition");
        displayGroupCaseField.setCaseField(caseFieldEntity("field"));
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);

        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenThrow(new InvalidShowConditionException("someShowCondition"));

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidTabFieldShowCondition.class));
    }

    @Test
//    @DisplayName("should fail when tab field show condition references invalid field from same tab")
    public void TabFieldShowCondition_returnsDisplayGroupInvalidShowConditionFieldWhenShowConditionReferencesInvalidFieldFromSameTab() throws InvalidShowConditionException {

        displayGroup.setType(DisplayGroupType.TAB);
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);

        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setCaseField(caseFieldEntity("otherField"));
        displayGroupCaseField.setShowCondition("someShowCondition");
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);
        allTabDisplayGroups.add(displayGroup);

        ShowCondition sc = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(sc);

        ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidTabFieldShowCondition.class));
    }


    @Test
//    @DisplayName("should fail when tab field show condition references invalid field from other tab")
    public void TabFieldShowCondition_returnsDisplayGroupInvalidShowConditionFieldWhenShowConditionReferencesInvalidFieldFromOtherTab() throws InvalidShowConditionException {

        displayGroup.setType(DisplayGroupType.TAB);
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);
        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setShowCondition("someShowCondition");
        displayGroupCaseField.setCaseField(caseFieldEntity("thisField"));
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);

        DisplayGroupCaseFieldEntity displayGroupCaseFieldOther = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldOther.setCaseField(caseFieldEntity("otherField"));
        DisplayGroupEntity otherTabDisplayGroup = new DisplayGroupEntity();
        otherTabDisplayGroup.setType(DisplayGroupType.TAB);
        otherTabDisplayGroup.addDisplayGroupCaseField(displayGroupCaseFieldOther);
        otherTabDisplayGroup.setCaseType(caseTypeEntity);
        allTabDisplayGroups.add(otherTabDisplayGroup);

        ShowCondition sc = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(sc);

        ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidTabFieldShowCondition.class));
    }

//}
//    @Nested
//    class TabShowCondition {

    @Test
//    @DisplayName("should not execute when tab show condition is empty")
    public void TabShowCondition_shouldNotExecuteWhenShowConditionIsEmpty() throws InvalidShowConditionException {

        displayGroup.setShowCondition(null);
        displayGroup.setType(DisplayGroupType.TAB);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
//    @DisplayName("should not execute when tab show condition is blank")
    public void TabShowCondition_shouldNotExecuteWhenShowConditionIsBlank() throws InvalidShowConditionException {

        displayGroup.setShowCondition("");
        displayGroup.setType(DisplayGroupType.TAB);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
//    @DisplayName("should not execute when tab show type is not tab")
    public void TabShowCondition_shouldNotExecuteWhenShowTypeIsNotTab() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.PAGE);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
//    @DisplayName("should return no errors when tab referenced field in other tab")
    public void TabShowCondition_returnsNoValidationErrorsOnSuccessWhenReferencedFieldInOtherTab() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.TAB);
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);

        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setCaseField(caseFieldEntity("field"));
        DisplayGroupEntity otherTabDisplayGroup = new DisplayGroupEntity();
        otherTabDisplayGroup.setType(DisplayGroupType.TAB);
        otherTabDisplayGroup.addDisplayGroupCaseField(displayGroupCaseField);
        otherTabDisplayGroup.setCaseType(caseTypeEntity);
        allTabDisplayGroups.add(otherTabDisplayGroup);

        ShowCondition validParsedShowCondition = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(validParsedShowCondition);

        ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

        assertThat(result.isValid(), is(true));
    }

    @Test
//    @DisplayName("should return no errors when tab referenced field in this tab")
    public void TabShowCondition_returnsNoValidationErrorsOnSuccessWhenReferencedFieldInThisTab() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.TAB);
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);

        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setCaseField(caseFieldEntity("field"));
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);
        allTabDisplayGroups.add(displayGroup);

        ShowCondition validParsedShowCondition = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(validParsedShowCondition);

        ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void TabShowCondition_shouldValidateShowConditionForCustomComplexField() throws InvalidShowConditionException {
        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode.AddressUKCode.Country";
        String showCondition = matchingCaseFieldKey + "=\"UK\"";

        ShowCondition sc = new ShowCondition.Builder().showConditionExpression(showCondition).field(matchingCaseFieldKey).build();
        when(mockShowConditionParser.parseShowCondition(showCondition))
            .thenReturn(sc);

        displayGroup.setShowCondition(showCondition);
        displayGroup.setType(DisplayGroupType.TAB);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId1"));
        caseTypeEntity.addCaseField(caseFieldEntity(matchingCaseFieldId, exampleFieldTypeEntityWithComplexFields()));
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId2"));
        displayGroup.setCaseType(caseTypeEntity);

        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setCaseField(caseFieldEntity(matchingCaseFieldId));
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);
        allTabDisplayGroups.add(displayGroup);

        ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void TabShowCondition_shouldAddErrorForInvalidShowConditionForCustomComplexField() throws InvalidShowConditionException {
        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode";
        String invalidShowCondition = matchingCaseFieldKey + "=\"UK\"";

        ShowCondition sc = new ShowCondition.Builder().showConditionExpression(invalidShowCondition).field(matchingCaseFieldKey).build();
        when(mockShowConditionParser.parseShowCondition(invalidShowCondition))
            .thenReturn(sc);

        displayGroup.setShowCondition(invalidShowCondition);
        displayGroup.setType(DisplayGroupType.TAB);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId1"));
        caseTypeEntity.addCaseField(caseFieldEntity(matchingCaseFieldId, exampleFieldTypeEntityWithComplexFields()));
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId2"));
        displayGroup.setCaseType(caseTypeEntity);

        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setCaseField(caseFieldEntity(matchingCaseFieldId));
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);
        allTabDisplayGroups.add(displayGroup);

        ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidTabShowCondition.class));
    }

    @Test
//    @DisplayName("should fail when unable to parse show condition for tab")
    public void TabShowCondition_returnsDisplayGroupInvalidShowConditionErrorWhenUnableToParseShowCondition() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.TAB);
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenThrow(new InvalidShowConditionException("someShowCondition"));

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidTabShowCondition.class));
    }

    @Test
//    @DisplayName("should fail when tab show condition references invalid field from same tab")
    public void TabShowCondition_returnsDisplayGroupInvalidShowConditionFieldWhenShowConditionReferencesInvalidFieldFromSameTab() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.TAB);
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);

        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setCaseField(caseFieldEntity("otherField"));
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);

        ShowCondition sc = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(sc);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidTabShowCondition.class));
    }

    @Test
//    @DisplayName("should fail when tab show condition references invalid field from other tab")
    public void TabShowCondition_returnsDisplayGroupInvalidShowConditionFieldWhenShowConditionReferencesInvalidFieldFromOtherTab() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.TAB);
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);


        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("otherField");
        displayGroupCaseField.setCaseField(caseField);
        DisplayGroupEntity otherTabDisplayGroup = new DisplayGroupEntity();
        otherTabDisplayGroup.setType(DisplayGroupType.TAB);
        otherTabDisplayGroup.addDisplayGroupCaseField(displayGroupCaseField);
        otherTabDisplayGroup.setCaseType(caseTypeEntity);
        allTabDisplayGroups.add(otherTabDisplayGroup);

        ShowCondition sc = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(sc);

        ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidTabShowCondition.class));
    }

//    }
    private static FieldTypeEntity exampleFieldTypeEntityWithComplexFields() {
        return fieldTypeEntity("FullName",
            asList(
                complexFieldEntity(
                    "NamePrefix",
                    fixedListFieldTypeEntity(
                        "FixedList-PreFix",
                        asList(
                            fieldTypeListItemEntity("Mr.", "Mr."),
                            fieldTypeListItemEntity("Mrs.", "Mrs.")))),
                complexFieldEntity("FirstName", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("MiddleName", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("LastNameWithSomeCplxFields", fieldTypeEntity("FullName1",
                    asList(
                        complexFieldEntity("LastName", fieldTypeEntity("Text", emptyList())),
                        complexFieldEntity("SomeComplexFieldsCode",
                            fieldTypeEntity("SomeComplexFields",
                                asList(
                                    complexFieldEntity("AddressUKCode", addressUKFieldTypeEntity()),
                                    complexFieldEntity("AddressGlobalCode", addressGlobalFieldTypeEntity()),
                                    complexFieldEntity("AddressGlobalUKCode", addressGlobalUKFieldTypeEntity()),
                                    complexFieldEntity("OrderSummaryCode", orderSummaryFieldTypeEntity()),
                                    complexFieldEntity("SecondSurname", fieldTypeEntity("Text", emptyList()))
                                      )))
                          ))))
                              );
    }

    private static FieldTypeEntity orderSummaryFieldTypeEntity() {
        return fieldTypeEntity(PREDEFINED_COMPLEX_ORDER_SUMMARY,
            asList(
                complexFieldEntity("PaymentReference", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("PaymentTotal", fieldTypeEntity("MoneyGBP", emptyList())),
                complexFieldEntity("Fees", fieldTypeEntity("FeesList", emptyList()))
                  ));
    }

    private static FieldTypeEntity addressUKFieldTypeEntity() {
        return address(PREDEFINED_COMPLEX_ADDRESS_UK);
    }

    private static FieldTypeEntity addressGlobalFieldTypeEntity() {
        return address(PREDEFINED_COMPLEX_ADDRESS_GLOBAL);
    }

    private static FieldTypeEntity addressGlobalUKFieldTypeEntity() {
        return address(PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK);
    }

    private static FieldTypeEntity address(String reference) {
        return fieldTypeEntity(reference,
            asList(
                complexFieldEntity("AddressLine1", fieldTypeEntity("TextMax150", emptyList())),
                complexFieldEntity("AddressLine2", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("AddressLine3", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("PostTown", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("County", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("PostCode", fieldTypeEntity("TextMax14", emptyList())),
                complexFieldEntity("Country", fieldTypeEntity("TextMax50", emptyList()))
                  ));
    }

    private static CaseFieldEntity caseFieldEntity(String reference, FieldTypeEntity fieldTypeEntity) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        caseFieldEntity.setFieldType(fieldTypeEntity);
        return caseFieldEntity;
    }

    private static CaseFieldEntity caseFieldEntity(String caseFieldReference) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(caseFieldReference);
        caseFieldEntity.setFieldType(fieldTypeEntity("TEXT", emptyList()));
        return caseFieldEntity;
    }

    private static FieldTypeEntity fieldTypeEntity(String reference,
                                                   List<ComplexFieldEntity> complexFieldEntityList) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.addComplexFields(complexFieldEntityList);
        return fieldTypeEntity;
    }

    private static ComplexFieldEntity complexFieldEntity(String reerence, FieldTypeEntity fieldTypeEntity) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reerence);
        complexFieldEntity.setFieldType(fieldTypeEntity);
        return complexFieldEntity;
    }

    private static FieldTypeEntity fixedListFieldTypeEntity(String reference,
                                                            List<FieldTypeListItemEntity> listItemEntities) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.addListItems(listItemEntities);
        return fieldTypeEntity;
    }

    private static FieldTypeListItemEntity fieldTypeListItemEntity(String label, String value) {
        FieldTypeListItemEntity fieldTypeListItemEntity = new FieldTypeListItemEntity();
        fieldTypeListItemEntity.setLabel(label);
        fieldTypeListItemEntity.setValue(value);
        return fieldTypeListItemEntity;
    }
}
