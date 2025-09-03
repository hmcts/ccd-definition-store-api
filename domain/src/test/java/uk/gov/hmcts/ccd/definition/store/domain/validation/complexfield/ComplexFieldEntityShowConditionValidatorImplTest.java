package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;


import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

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

class ComplexFieldEntityShowConditionValidatorImplTest {

    @Mock
    private ShowConditionParser mockShowConditionParser;

    @Mock
    private ComplexFieldValidator.ValidationContext mockValidationContext;

    ComplexFieldEntityShowConditionValidatorImpl testObj;
    ComplexFieldEntity complexField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testObj = new ComplexFieldEntityShowConditionValidatorImpl(mockShowConditionParser);
        complexField = new ComplexFieldEntity();
    }

    @Test
    void shouldNotExecuteWhenShowConditionIsEmpty() throws InvalidShowConditionException {

        complexField.setShowCondition(null);
        ValidationResult result = testObj.validate(complexField, mockValidationContext);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
        assertThat(result.isValid(), is(true));
    }

    @Test
    void shouldNotExecuteWhenShowConditionIsBlank() throws InvalidShowConditionException {

        complexField.setShowCondition("");

        ValidationResult result = testObj.validate(complexField, mockValidationContext);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
        assertThat(result.isValid(), is(true));
    }

    @Test
    void returnsNoValidationErrorsOnSuccess() throws InvalidShowConditionException {

        complexField.setShowCondition("someShowCondition");
        FieldTypeEntity complexFieldType = mock(FieldTypeEntity.class);
        when(complexFieldType.hasComplexField("field")).thenReturn(true);
        complexField.setComplexFieldType(complexFieldType);
        ShowCondition validParsedShowCondition = new ShowCondition.Builder()
            .showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(validParsedShowCondition);

        ValidationResult result = testObj.validate(complexField, mockValidationContext);

        assertThat(result.isValid(), is(true));
    }

    @Test
    void returnsComplexFieldInvalidShowConditionErrorWhenUnableToParseShowCondition()
        throws InvalidShowConditionException {

        complexField.setShowCondition("someShowCondition");
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenThrow(new InvalidShowConditionException("someShowCondition"));

        ValidationResult result = testObj.validate(complexField, mockValidationContext);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(ComplexFieldInvalidShowConditionError.class));
    }

    @Test
    void returnsComplexFieldInvalidShowConditionFieldWhenShowConditionReferencesInvalidField()
        throws InvalidShowConditionException {

        complexField.setShowCondition("someShowCondition");
        FieldTypeEntity complexFieldType = mock(FieldTypeEntity.class);
        when(complexFieldType.hasComplexField("field")).thenReturn(false);
        complexField.setComplexFieldType(complexFieldType);
        ShowCondition sc = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(sc);

        ValidationResult result = testObj.validate(complexField, mockValidationContext);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0),
            instanceOf(ComplexFieldShowConditionReferencesInvalidFieldError.class));
    }

    @Test
    void returnsNoValidationErrorsWhenShowConditionIncludesMetadataField() throws InvalidShowConditionException {
        String field = MetadataField.STATE.getReference();
        complexField.setShowCondition("someShowCondition");
        FieldTypeEntity complexFieldType = mock(FieldTypeEntity.class);
        when(complexFieldType.hasComplexField(field)).thenReturn(false);
        complexField.setComplexFieldType(complexFieldType);
        ShowCondition sc = new ShowCondition.Builder().showConditionExpression("parsedSC").field(field).build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(sc);

        ValidationResult result = testObj.validate(complexField, mockValidationContext);

        assertThat(result.isValid(), is(true));
    }


    @Test
    void returnsNoValidationErrorsWhenShowConditionIncludesInjectedField() throws InvalidShowConditionException {
        String field = "[INJECTED_DATA.test]";
        complexField.setShowCondition("someShowCondition");
        FieldTypeEntity complexFieldType = mock(FieldTypeEntity.class);
        when(complexFieldType.hasComplexField(field)).thenReturn(false);
        complexField.setComplexFieldType(complexFieldType);
        ShowCondition sc = new ShowCondition.Builder().showConditionExpression("parsedSC").field(field).build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .thenReturn(sc);

        ValidationResult result = testObj.validate(complexField, mockValidationContext);

        assertThat(result.isValid(), is(true));
    }

}
