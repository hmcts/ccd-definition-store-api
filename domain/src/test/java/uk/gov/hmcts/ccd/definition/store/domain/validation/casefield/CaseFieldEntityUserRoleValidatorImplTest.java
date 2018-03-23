package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldUserRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

public class CaseFieldEntityUserRoleValidatorImplTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private CaseFieldEntityUserRoleValidatorImpl validator;

    private CaseFieldUserRoleEntity caseFieldUserRole;

    private CaseFieldEntity caseField;

    @Mock
    private UserRoleEntity userRole;

    @Mock
    private CaseFieldEntityValidationContext caseFieldEntityValidationContext;

    @Before
    public void setup() {

        caseFieldUserRole = new CaseFieldUserRoleEntity();

        given(caseFieldEntityValidationContext.getCaseReference()).willReturn("case_type");

        caseField = new CaseFieldEntity();
        caseField.addCaseFieldUserRole(caseFieldUserRole);
        caseField.setReference("case_field");

        validator = new CaseFieldEntityUserRoleValidatorImpl();
    }

    @Test
    public void shouldHaveValidationError_whenUserNotFound() {

        caseFieldUserRole.setUserRole(null);
        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseFieldEntityInvalidUserRoleValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid UserRole for case type 'case_type', case field 'case_field'"));
    }

    @Test
    public void shouldHaveNoValidationError_whenUserFound() {

        caseFieldUserRole.setUserRole(userRole);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }
}
