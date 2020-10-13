package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComplexFieldEntityShowConditionValidatorImplTest {

    @Mock
    private ShowConditionParser mockShowConditionParser;

    @Mock
    private ComplexFieldValidator.ValidationContext mockValidationContext;

    ComplexFieldEntityShowConditionValidatorImpl testObj;
    ComplexFieldEntity complexField;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObj = new ComplexFieldEntityShowConditionValidatorImpl(mockShowConditionParser);
        complexField = new ComplexFieldEntity();
    }

    @Test
    public void shouldNotExecuteWhenShowConditionIsEmpty() throws InvalidShowConditionException {

        complexField.setShowCondition(null);
        ValidationResult result = testObj.validate(complexField, mockValidationContext);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
        assertThat(result.isValid(), is(true));
    }

    @Test
    public void shouldNotExecuteWhenShowConditionIsBlank() throws InvalidShowConditionException {

        complexField.setShowCondition("");

        ValidationResult result = testObj.validate(complexField, mockValidationContext);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
        assertThat(result.isValid(), is(true));
    }

    @Test
    public void returnsNoValidationErrorsOnSuccess() throws InvalidShowConditionException {

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
    public void returnsComplexFieldInvalidShowConditionErrorWhenUnableToParseShowCondition()
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
    public void returnsComplexFieldInvalidShowConditionFieldWhenShowConditionReferencesInvalidField()
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
    public void returnsNoValidationErrorsWhenShowConditionIncludesMetadataField() throws InvalidShowConditionException {
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


}
