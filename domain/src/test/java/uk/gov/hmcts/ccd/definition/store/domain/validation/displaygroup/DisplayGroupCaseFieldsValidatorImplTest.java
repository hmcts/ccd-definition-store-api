package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

public class DisplayGroupCaseFieldsValidatorImplTest {

    private static final List<DisplayGroupEntity> UNUSED_DISPLAY_GROUPS = Lists.newArrayList();
    DisplayGroupEntity displayGroup;
    DisplayGroupCaseFieldsValidatorImpl testObj;

    @Mock
    DisplayGroupCaseFieldValidator validator1;
    @Mock
    DisplayGroupCaseFieldValidator validator2;
    @Mock
    DisplayGroupCaseFieldEntity e1;
    @Mock
    DisplayGroupCaseFieldEntity e2;
    @Mock
    ValidationError ve1;
    @Mock
    ValidationError ve2;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObj = new DisplayGroupCaseFieldsValidatorImpl(Lists.newArrayList(validator1, validator2));
        displayGroup = new DisplayGroupEntity();
    }

    @Test
    public void shouldApplyValidatorsToAllFieldsOfADisplayGroup() throws InvalidShowConditionException {

        displayGroup.addDisplayGroupCaseField(e1);
        displayGroup.addDisplayGroupCaseField(e2);
        displayGroup.setType(DisplayGroupType.TAB);
        when(validator1.validate(anyObject())).thenReturn(new ValidationResult(ve1));
        when(validator2.validate(anyObject())).thenReturn(new ValidationResult(ve2));

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(validator1).validate(e1);
        verify(validator1).validate(e2);
        verify(validator2).validate(e1);
        verify(validator2).validate(e2);
        assertThat(result.getValidationErrors(), hasSize(2));
        assertThat(result.getValidationErrors(), hasItems(ve1, ve2));
    }

    @Test
    public void shouldReturnEmptyValidationResultWhenNoErrors() throws InvalidShowConditionException {

        displayGroup.addDisplayGroupCaseField(e1);
        displayGroup.addDisplayGroupCaseField(e2);
        when(validator1.validate(anyObject())).thenReturn(ValidationResult.SUCCESS);
        when(validator2.validate(anyObject())).thenReturn(ValidationResult.SUCCESS);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(true));
    }
}
