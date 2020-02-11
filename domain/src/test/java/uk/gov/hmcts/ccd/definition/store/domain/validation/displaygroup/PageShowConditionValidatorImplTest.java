package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORDER_SUMMARY;

public class PageShowConditionValidatorImplTest {
    private static final List<DisplayGroupEntity> UNUSED_DISPLAY_GROUPS = Lists.newArrayList();

    @Mock
    private ShowConditionParser mockShowConditionParser;

    private PageShowConditionValidatorImpl testObj;
    private DisplayGroupEntity displayGroup;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObj = new PageShowConditionValidatorImpl(mockShowConditionParser, new CaseFieldEntityUtil());
        displayGroup = new DisplayGroupEntity();
    }

    @Test
    public void shouldNotExecuteWhenShowConditionIsEmpty() throws InvalidShowConditionException {

        displayGroup.setShowCondition(null);
        displayGroup.setType(DisplayGroupType.PAGE);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
    public void shouldNotExecuteWhenShowConditionIsBlank() throws InvalidShowConditionException {

        displayGroup.setShowCondition("");
        displayGroup.setType(DisplayGroupType.PAGE);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
    public void shouldNotExecuteWhenShowTypeIsNotPage() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.TAB);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
    public void returnsNoValidationErrorsOnSuccess() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.PAGE);
        EventEntity event = mock(EventEntity.class);
        when(event.hasField("field")).thenReturn(true);
        displayGroup.setEvent(event);
        ShowCondition validParsedShowCondition = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
                .thenReturn(validParsedShowCondition);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void successfullyValidatesShowConditionForCustomComplexField() throws InvalidShowConditionException {
        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode.AddressUKCode.Country";
        String showCondition = matchingCaseFieldKey + "=\"UK\"";

        ShowCondition validParsedShowCondition = new ShowCondition.Builder().showConditionExpression(showCondition).field(matchingCaseFieldKey).build();
        when(mockShowConditionParser.parseShowCondition(showCondition))
            .thenReturn(validParsedShowCondition);

        displayGroup.setShowCondition(showCondition);
        displayGroup.setType(DisplayGroupType.PAGE);
        EventEntity event = mock(EventEntity.class);
        when(event.hasField(matchingCaseFieldId)).thenReturn(true);
        displayGroup.setEvent(event);
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId1"));
        caseTypeEntity.addCaseField(caseFieldEntity(matchingCaseFieldId, exampleFieldTypeEntityWithComplexFields()));
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId2"));
        displayGroup.setCaseType(caseTypeEntity);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void shouldAddErrorForInvalidShowConditionForCustomComplexField() throws InvalidShowConditionException {
        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode";
        String invalidShowCondition = matchingCaseFieldKey + "=\"UK\"";

        ShowCondition validParsedShowCondition = new ShowCondition.Builder().showConditionExpression(invalidShowCondition).field(matchingCaseFieldKey).build();
        when(mockShowConditionParser.parseShowCondition(invalidShowCondition))
            .thenReturn(validParsedShowCondition);

        displayGroup.setShowCondition(invalidShowCondition);
        displayGroup.setType(DisplayGroupType.PAGE);
        EventEntity event = mock(EventEntity.class);
        when(event.hasField(matchingCaseFieldId)).thenReturn(true);
        displayGroup.setEvent(event);
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId1"));
        caseTypeEntity.addCaseField(caseFieldEntity(matchingCaseFieldId, exampleFieldTypeEntityWithComplexFields()));
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId2"));
        displayGroup.setCaseType(caseTypeEntity);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidEventFieldShowCondition.class));
    }

    @Test
    public void returnsDisplayGroupInvalidShowConditionErrorWhenUnableToParseShowCondition() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.PAGE);
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
                .thenThrow(new InvalidShowConditionException("someShowCondition"));

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidShowConditionError.class));
    }

    @Test
    public void successfullyValidatesShowConditionWithMetadataField() throws InvalidShowConditionException {
        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.PAGE);
        String field = MetadataField.STATE.getReference();
        EventEntity event = mock(EventEntity.class);
        when(event.hasField(field)).thenReturn(false);
        displayGroup.setEvent(event);
        ShowCondition validParsedShowCondition = new ShowCondition.Builder().showConditionExpression("parsedSC").field(field).build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(validParsedShowCondition);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void returnsDisplayGroupInvalidShowConditionFieldWhenShowConditionReferencesInvalidField() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.PAGE);
        EventEntity event = mock(EventEntity.class);
        when(event.hasField("field")).thenReturn(false);
        displayGroup.setEvent(event);
        ShowCondition sc = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
                .thenReturn(sc);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidEventFieldShowCondition.class));
    }

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
