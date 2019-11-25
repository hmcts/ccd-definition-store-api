package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class CaseFieldEntityACLValidatorImplTest {
    private static final String IDAM_ROLE = "caseworker-test-solicitor";
    private static final String ROLE_CREATOR = "[CREATOR]";
    private static final String ROLE_DEFENDANT = "[DEFENDANT]";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private CaseFieldEntityACLValidatorImpl validator;

    private CaseFieldEntity caseField;

    @Mock
    private UserRoleEntity userRole;

    private CaseFieldACLEntity caseFieldUserRole;

    private UserRoleEntity idamRole;

    private CaseRoleEntity caseRoleCreator;

    private CaseRoleEntity caseRoleDefendant;

    @Mock
    private CaseFieldEntityValidationContext caseFieldEntityValidationContext;

    @Before
    public void setup() {

        idamRole = new UserRoleEntity();
        idamRole.setReference(IDAM_ROLE);

        caseFieldUserRole = new CaseFieldACLEntity();
        caseFieldUserRole.setUserRole(idamRole);

        caseRoleCreator = new CaseRoleEntity();
        caseRoleCreator.setReference(ROLE_CREATOR);

        caseRoleDefendant = new CaseRoleEntity();
        caseRoleDefendant.setReference(ROLE_DEFENDANT);

        given(caseFieldEntityValidationContext.getCaseReference()).willReturn("case_type");
        given(caseFieldEntityValidationContext.getCaseRoles()).willReturn(emptyList());

        caseField = new CaseFieldEntity();
        caseField.addCaseFieldACL(caseFieldUserRole);
        caseField.setReference("case_field");

        validator = new CaseFieldEntityACLValidatorImpl();
    }

    @Test
    public void shouldHaveValidationErrorWhenUserNotFound() {

        caseFieldUserRole.setUserRole(null);
        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseFieldEntityInvalidUserRoleValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid UserRole for case type 'case_type', case field 'case_field'"));
    }

    @Test
    public void shouldHaveNoValidationErrorWhenUserFound() {

        caseFieldUserRole.setUserRole(userRole);
        given(userRole.getReference()).willReturn("some-user");

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }

    @Test
    public void shouldHaveNoValidationErrorWhenCaseRoleIsFoundOnCaseType() {
        given(caseFieldEntityValidationContext.getCaseRoles()).willReturn(asList(caseRoleCreator, caseRoleDefendant));

        caseFieldUserRole.setUserRole(caseRoleCreator);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }

    @Test
    public void shouldHaveValidationErrorWhenCaseRoleNotFoundOnCaseType() {
        given(caseFieldEntityValidationContext.getCaseRoles()).willReturn(asList(caseRoleCreator));

        caseFieldUserRole.setUserRole(caseRoleDefendant);
        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseFieldEntityInvalidUserRoleValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid UserRole for case type 'case_type', case field 'case_field'"));
    }
}
