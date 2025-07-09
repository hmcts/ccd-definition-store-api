package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DisplayGroupCaseFieldsValidatorImplTest {

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


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testObj = new DisplayGroupCaseFieldsValidatorImpl(Lists.newArrayList(validator1, validator2));
        displayGroup = new DisplayGroupEntity();
    }

    @Test
    void shouldApplyValidatorsToAllFieldsOfADisplayGroup() throws InvalidShowConditionException {

        displayGroup.addDisplayGroupCaseField(e1);
        displayGroup.addDisplayGroupCaseField(e2);
        displayGroup.setType(DisplayGroupType.TAB);
        when(validator1.validate(any())).thenReturn(new ValidationResult(ve1));
        when(validator2.validate(any())).thenReturn(new ValidationResult(ve2));

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);
        assertThat(result.getValidationErrors(), hasSize(2));
        assertThat(result.getValidationErrors(), hasItems(ve1, ve2));
        verify(validator1).validate(e1);
        verify(validator1).validate(e2);
        verify(validator2).validate(e1);
        verify(validator2).validate(e2);
    }

    @Test
    void shouldReturnEmptyValidationResultWhenNoErrors() throws InvalidShowConditionException {

        displayGroup.addDisplayGroupCaseField(e1);
        displayGroup.addDisplayGroupCaseField(e2);
        when(validator1.validate(any())).thenReturn(ValidationResult.SUCCESS);
        when(validator2.validate(any())).thenReturn(ValidationResult.SUCCESS);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(true));
    }
}
