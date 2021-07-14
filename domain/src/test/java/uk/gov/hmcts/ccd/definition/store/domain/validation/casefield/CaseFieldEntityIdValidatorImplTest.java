package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

public class CaseFieldEntityIdValidatorImplTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private CaseFieldEntityIdValidatorImpl validator;

    private CaseFieldEntity caseField;

    @Mock
    private CaseFieldEntityValidationContext caseFieldEntityValidationContext;

    @Before
    public void setup() {

        given(caseFieldEntityValidationContext.getCaseReference()).willReturn("case_type");

        caseField = new CaseFieldEntity();
        caseField.setReference("case_field");

        validator = new CaseFieldEntityIdValidatorImpl();
    }

    @Test
    public void shouldHaveValidationErrorForInvalidCaseFieldId() {
        caseField.setReference("select * from tab1");
        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0),
            instanceOf(CaseFieldEntityInvalidIdValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "case field '" + caseField.getReference() + "' for case type 'case_type' "
            + "does not match pattern '^['a-zA-Z0-9\\[\\]\\#%\\&()\\.?_\\£\\s\\xA0-]+$' "));
    }

    @Test
    public void shouldHaveNoValidationErrorForValidCaseFieldId() {

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }
}
