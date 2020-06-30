package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;

public class DisplayGroupColumnNumberValidatorTest {

    DisplayGroupColumnNumberValidator testObj;
    DisplayGroupCaseFieldEntity entity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObj = new DisplayGroupColumnNumberValidator();
        entity = new DisplayGroupCaseFieldEntity();
    }


    @Test
    public void shouldPassValidationWhenColumnNrIs1() {
        entity.setColumnNumber(1);

        ValidationResult result = testObj.validate(entity);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void shouldPassValidationWhenColumnNrIs2() {
        entity.setColumnNumber(2);

        ValidationResult result = testObj.validate(entity);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void shouldPassValidationWhenColumnNrNull() {
        entity.setColumnNumber(null);

        ValidationResult result = testObj.validate(entity);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void shouldFailValidationWhenWhenColumnNrIsNotValid() {
        entity.setColumnNumber(3);
        CaseFieldEntity cf = new CaseFieldEntity();
        cf.setReference("cf");
        entity.setCaseField(cf);

        ValidationResult result = testObj.validate(entity);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors(), hasItem(instanceOf(DisplayGroupColumnNumberValidator.ValidationError.class)));
        DisplayGroupColumnNumberValidator.ValidationError validationError =
            (DisplayGroupColumnNumberValidator.ValidationError) result.getValidationErrors().get(0);
        assertThat(validationError.getEntity(), is(entity));
        assertThat(validationError.getDefaultMessage(), is("Invalid page column number '3' for case field 'cf'"));
    }


}
