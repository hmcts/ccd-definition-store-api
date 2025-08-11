package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.showcondition.BaseShowConditionTest.caseFieldEntity;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.showcondition.BaseShowConditionTest.exampleFieldTypeEntityWithComplexFields;

class PageShowConditionValidatorImplTest {
    private static final List<DisplayGroupEntity> UNUSED_DISPLAY_GROUPS = Lists.newArrayList();

    @Mock
    private ShowConditionParser mockShowConditionParser;

    private PageShowConditionValidatorImpl testObj;
    private DisplayGroupEntity displayGroup;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testObj = new PageShowConditionValidatorImpl(mockShowConditionParser, new CaseFieldEntityUtil());
        displayGroup = new DisplayGroupEntity();
    }

    @Test
    void shouldNotExecuteWhenShowConditionIsEmpty() throws InvalidShowConditionException {

        displayGroup.setShowCondition(null);
        displayGroup.setType(DisplayGroupType.PAGE);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
    void shouldNotExecuteWhenShowConditionIsBlank() throws InvalidShowConditionException {

        displayGroup.setShowCondition("");
        displayGroup.setType(DisplayGroupType.PAGE);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
    void shouldNotExecuteWhenShowTypeIsNotPage() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.TAB);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
    void returnsNoValidationErrorsOnSuccess() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.PAGE);
        EventEntity event = mock(EventEntity.class);
        when(event.hasField("field")).thenReturn(true);
        displayGroup.setEvent(event);
        ShowCondition validParsedShowCondition = new ShowCondition.Builder()
            .showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(validParsedShowCondition);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(true));
    }

    @Test
    void successfullyValidatesShowConditionForCustomComplexField() throws InvalidShowConditionException {
        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId
            + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode.AddressUKCode.Country";
        String showCondition = matchingCaseFieldKey + "=\"UK\"";

        ShowCondition validParsedShowCondition = new ShowCondition.Builder()
            .showConditionExpression(showCondition).field(matchingCaseFieldKey).build();
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
    void shouldAddErrorForInvalidShowConditionForCustomComplexField() throws InvalidShowConditionException {
        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode";
        String invalidShowCondition = matchingCaseFieldKey + "=\"UK\"";

        ShowCondition validParsedShowCondition = new ShowCondition.Builder()
            .showConditionExpression(invalidShowCondition).field(matchingCaseFieldKey).build();
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
    void returnsDisplayGroupInvalidShowConditionErrorWhenUnableToParseShowCondition()
        throws InvalidShowConditionException {

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
    void successfullyValidatesShowConditionWithMetadataField() throws InvalidShowConditionException {
        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.PAGE);
        String field = MetadataField.STATE.getReference();
        EventEntity event = mock(EventEntity.class);
        when(event.hasField(field)).thenReturn(false);
        displayGroup.setEvent(event);
        ShowCondition validParsedShowCondition = new ShowCondition.Builder()
            .showConditionExpression("parsedSC").field(field).build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(validParsedShowCondition);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(true));
    }


    @Test
    void successfullyValidatesShowConditionWithInjected() throws InvalidShowConditionException {
        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.PAGE);
        String field = "[INJECTED_DATA.test]";
        EventEntity event = mock(EventEntity.class);
        when(event.hasField(field)).thenReturn(false);
        displayGroup.setEvent(event);
        ShowCondition validParsedShowCondition = new ShowCondition.Builder()
            .showConditionExpression("parsedSC").field(field).build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(validParsedShowCondition);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        displayGroup.setCaseType(caseTypeEntity);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(true));
    }

    @Test
    void returnsDisplayGroupInvalidShowConditionFieldWhenShowConditionReferencesInvalidField()
        throws InvalidShowConditionException {

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
}
