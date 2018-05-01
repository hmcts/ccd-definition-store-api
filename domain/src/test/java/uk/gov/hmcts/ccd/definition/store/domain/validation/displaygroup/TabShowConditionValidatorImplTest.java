package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class TabShowConditionValidatorImplTest {
    private static final List<DisplayGroupEntity> UNUSED_DISPLAY_GROUPS = com.google.common.collect.Lists.newArrayList();

    @Mock
    private ShowConditionParser mockShowConditionParser;
    TabShowConditionValidatorImpl testObj;
    DisplayGroupEntity displayGroup;
    List<DisplayGroupEntity> allTabDisplayGroups;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObj = new TabShowConditionValidatorImpl(mockShowConditionParser);
        displayGroup = new DisplayGroupEntity();
        allTabDisplayGroups = Lists.newArrayList();
    }

    @Nested
    class TabFieldShowCondition {

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
        @DisplayName("should not execute when tab field show condition is blank")
        public void shouldNotExecuteWhenShowConditionIsBlank() throws InvalidShowConditionException {

            DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity = new DisplayGroupCaseFieldEntity();
            displayGroupCaseFieldEntity.setShowCondition("");
            displayGroup.setType(DisplayGroupType.TAB);
            displayGroup.addDisplayGroupCaseField(displayGroupCaseFieldEntity);

            testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

            verify(mockShowConditionParser, never()).parseShowCondition(anyString());
        }

        @Test
        @DisplayName("should not execute when tab show type is not tab")
        public void shouldNotExecuteWhenShowTypeIsNotTab() throws InvalidShowConditionException {

            DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity = new DisplayGroupCaseFieldEntity();
            displayGroupCaseFieldEntity.setShowCondition("someShowCondition");
            displayGroup.addDisplayGroupCaseField(displayGroupCaseFieldEntity);
            displayGroup.setType(DisplayGroupType.PAGE);

            testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

            verify(mockShowConditionParser, never()).parseShowCondition(anyString());
        }

        @Test
        @DisplayName("should return no errors when tab field referenced field in other tab")
        public void returnsNoValidationErrorsOnSuccessWhenReferencedFieldInOtherTab() throws InvalidShowConditionException {

            displayGroup.setType(DisplayGroupType.TAB);
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setReference("SimpleType");
            displayGroup.setCaseType(caseTypeEntity);
            DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
            displayGroupCaseField.setShowCondition("someShowCondition");
            CaseFieldEntity caseField = new CaseFieldEntity();
            caseField.setReference("thisField");
            displayGroupCaseField.setCaseField(caseField);
            displayGroup.addDisplayGroupCaseField(displayGroupCaseField);
            allTabDisplayGroups.add(displayGroup);

            DisplayGroupCaseFieldEntity displayGroupCaseFieldOther = new DisplayGroupCaseFieldEntity();
            CaseFieldEntity caseFieldOther = new CaseFieldEntity();
            caseFieldOther.setReference("otherField");
            displayGroupCaseFieldOther.setCaseField(caseFieldOther);
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
        @DisplayName("should return no errors when tab field referenced field in this tab")
        public void returnsNoValidationErrorsOnSuccessWhenReferencedFieldInThisTab() throws InvalidShowConditionException {

            displayGroup.setType(DisplayGroupType.TAB);
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setReference("SimpleType");
            displayGroup.setCaseType(caseTypeEntity);

            DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
            CaseFieldEntity caseField = new CaseFieldEntity();
            caseField.setReference("field");
            displayGroupCaseField.setCaseField(caseField);
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
        @DisplayName("should fail when unable to parse show condition for tab field")
        public void returnsDisplayGroupInvalidShowConditionErrorWhenUnableToParseShowCondition() throws InvalidShowConditionException {

            displayGroup.setType(DisplayGroupType.TAB);
            DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
            displayGroupCaseField.setShowCondition("someShowCondition");
            CaseFieldEntity caseField = new CaseFieldEntity();
            caseField.setReference("field");
            displayGroupCaseField.setCaseField(caseField);
            displayGroup.addDisplayGroupCaseField(displayGroupCaseField);

            when(mockShowConditionParser.parseShowCondition("someShowCondition"))
                .thenThrow(new InvalidShowConditionException("someShowCondition"));

            ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

            assertThat(result.isValid(), is(false));
            assertThat(result.getValidationErrors(), hasSize(1));
            assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidTabFieldShowCondition.class));
        }

        @Test
        @DisplayName("should fail when tab field show condition references invalid field from same tab")
        public void returnsDisplayGroupInvalidShowConditionFieldWhenShowConditionReferencesInvalidFieldFromSameTab() throws InvalidShowConditionException {

            displayGroup.setType(DisplayGroupType.TAB);
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setReference("SimpleType");
            displayGroup.setCaseType(caseTypeEntity);

            DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
            CaseFieldEntity caseField = new CaseFieldEntity();
            caseField.setReference("otherField");
            displayGroupCaseField.setCaseField(caseField);
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
        @DisplayName("should fail when tab field show condition references invalid field from other tab")
        public void returnsDisplayGroupInvalidShowConditionFieldWhenShowConditionReferencesInvalidFieldFromOtherTab() throws InvalidShowConditionException {

            displayGroup.setType(DisplayGroupType.TAB);
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setReference("SimpleType");
            displayGroup.setCaseType(caseTypeEntity);
            DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
            displayGroupCaseField.setShowCondition("someShowCondition");
            CaseFieldEntity caseField = new CaseFieldEntity();
            caseField.setReference("thisField");
            displayGroupCaseField.setCaseField(caseField);
            displayGroup.addDisplayGroupCaseField(displayGroupCaseField);

            DisplayGroupCaseFieldEntity displayGroupCaseFieldOther = new DisplayGroupCaseFieldEntity();
            CaseFieldEntity caseFieldOther = new CaseFieldEntity();
            caseFieldOther.setReference("otherField");
            displayGroupCaseFieldOther.setCaseField(caseFieldOther);
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
    }

    @Nested
    class TabShowCondition {

        @Test
        @DisplayName("should not execute when tab show condition is empty")
        public void shouldNotExecuteWhenShowConditionIsEmpty() throws InvalidShowConditionException {

            displayGroup.setShowCondition(null);
            displayGroup.setType(DisplayGroupType.TAB);

            testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

            verify(mockShowConditionParser, never()).parseShowCondition(anyString());
        }

        @Test
        @DisplayName("should not execute when tab show condition is blank")
        public void shouldNotExecuteWhenShowConditionIsBlank() throws InvalidShowConditionException {

            displayGroup.setShowCondition("");
            displayGroup.setType(DisplayGroupType.TAB);

            testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

            verify(mockShowConditionParser, never()).parseShowCondition(anyString());
        }

        @Test
        @DisplayName("should not execute when tab show type is not tab")
        public void shouldNotExecuteWhenShowTypeIsNotTab() throws InvalidShowConditionException {

            displayGroup.setShowCondition("someShowCondition");
            displayGroup.setType(DisplayGroupType.PAGE);

            testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

            verify(mockShowConditionParser, never()).parseShowCondition(anyString());
        }

        @Test
        @DisplayName("should return no errors when tab referenced field in other tab")
        public void returnsNoValidationErrorsOnSuccessWhenReferencedFieldInOtherTab() throws InvalidShowConditionException {

            displayGroup.setShowCondition("someShowCondition");
            displayGroup.setType(DisplayGroupType.TAB);
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setReference("SimpleType");
            displayGroup.setCaseType(caseTypeEntity);

            DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
            CaseFieldEntity caseField = new CaseFieldEntity();
            caseField.setReference("field");
            displayGroupCaseField.setCaseField(caseField);
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
        @DisplayName("should return no errors when tab referenced field in this tab")
        public void returnsNoValidationErrorsOnSuccessWhenReferencedFieldInThisTab() throws InvalidShowConditionException {

            displayGroup.setShowCondition("someShowCondition");
            displayGroup.setType(DisplayGroupType.TAB);
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setReference("SimpleType");
            displayGroup.setCaseType(caseTypeEntity);

            DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
            CaseFieldEntity caseField = new CaseFieldEntity();
            caseField.setReference("field");
            displayGroupCaseField.setCaseField(caseField);
            displayGroup.addDisplayGroupCaseField(displayGroupCaseField);
            allTabDisplayGroups.add(displayGroup);

            ShowCondition validParsedShowCondition = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
            when(mockShowConditionParser.parseShowCondition("someShowCondition"))
                .thenReturn(validParsedShowCondition);

            ValidationResult result = testObj.validate(displayGroup, allTabDisplayGroups);

            assertThat(result.isValid(), is(true));
        }

        @Test
        @DisplayName("should fail when unable to parse show condition for tab")
        public void returnsDisplayGroupInvalidShowConditionErrorWhenUnableToParseShowCondition() throws InvalidShowConditionException {

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
        @DisplayName("should fail when tab show condition references invalid field from same tab")
        public void returnsDisplayGroupInvalidShowConditionFieldWhenShowConditionReferencesInvalidFieldFromSameTab() throws InvalidShowConditionException {

            displayGroup.setShowCondition("someShowCondition");
            displayGroup.setType(DisplayGroupType.TAB);

            DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
            CaseFieldEntity caseField = new CaseFieldEntity();
            caseField.setReference("otherField");
            displayGroupCaseField.setCaseField(caseField);
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
        @DisplayName("should fail when tab show condition references invalid field from other tab")
        public void returnsDisplayGroupInvalidShowConditionFieldWhenShowConditionReferencesInvalidFieldFromOtherTab() throws InvalidShowConditionException {

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
    }

}
