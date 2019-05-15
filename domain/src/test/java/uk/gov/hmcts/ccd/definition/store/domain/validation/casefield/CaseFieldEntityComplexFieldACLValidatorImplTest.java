package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

public class CaseFieldEntityComplexFieldACLValidatorImplTest {

    private static String ROLE1 = "Role1";
    private CaseFieldEntityComplexFieldACLValidatorImpl validator;

    private ComplexFieldACLEntity complexACL1;
    private ComplexFieldACLEntity complexACL2;
    private ComplexFieldACLEntity complexACL3;

    private CaseFieldACLEntity fieldACL;

    private CaseFieldEntity caseField;

    @Mock
    private UserRoleEntity userRole;

    @Mock
    private CaseFieldEntityValidationContext caseFieldEntityValidationContext;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        userRole = new UserRoleEntity();
        userRole.setReference(ROLE1);

        complexACL1 = new ComplexFieldACLEntity();
        complexACL1.setListElementCode("Class.ClassMembers");
        complexACL2 = new ComplexFieldACLEntity();
        complexACL2.setListElementCode("Class.ClassMembers.Children");
        complexACL3 = new ComplexFieldACLEntity();
        complexACL3.setListElementCode("Class.ClassMembers.Children.ChildFullName");

        given(caseFieldEntityValidationContext.getCaseReference()).willReturn("case_type");

        caseField = new CaseFieldEntity();
        caseField.setReference("case_field");

        fieldACL = new CaseFieldACLEntity();

        validator = new CaseFieldEntityComplexFieldACLValidatorImpl();
    }

    @Test
    @DisplayName("should fail when user not found")
    public void shouldHaveValidationErrorWhenUserNotFound() {
        complexACL1.setUserRole(null);
        caseField.addComplexFieldACL(complexACL1);
        fieldACL.setUserRole(userRole);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(2)),
            () -> assertThat(result.getValidationErrors().get(0), instanceOf(CaseFieldEntityInvalidUserRoleValidationError.class)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid UserRole for case type 'case_type', case field 'case_field'"))
        );
    }

    @Test
    @DisplayName("should not fail when user is found")
    public void shouldHaveNoValidationErrorWhenUserFound() {
        complexACL1.setUserRole(userRole);
        complexACL1.setCreate(true);
        caseField.addComplexFieldACL(complexACL1);
        fieldACL.setCreate(true);
        fieldACL.setUserRole(userRole);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }

    @Test
    @DisplayName("should fail when parent caseField has no ACL defined")
    public void shouldSucceedWhenComplexChildHasLessOrEqualAccessComparedToCaseFieldParent() {
        complexACL1.setUserRole(userRole);
        complexACL1.setCreate(true);
        caseField.addComplexFieldACL(complexACL1);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), not(empty()));
    }

    @Test
    @DisplayName("should fail when parent caseField has no ACL defined")
    public void shouldFailWhenCaseFieldParentHasNoAcl() {
        complexACL1.setUserRole(userRole);
        complexACL1.setCreate(true);
        caseField.addComplexFieldACL(complexACL1);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        final List<ValidationError> validationErrors = result.getValidationErrors();
        assertAll(
            () -> assertThat(validationErrors, not(empty())),
            () -> assertThat(validationErrors.get(0).getDefaultMessage(), is("The access for case type 'case_type', "
                + "case field 'case_field', list element code 'Class.ClassMembers' is more than its parent"))
        );
    }

    @Test
    @DisplayName("should fail when complex child element has more access than parent")
    public void shouldFailWhenComplexChildHasMoreAccessThanCaseFieldParent() {
        complexACL1.setCreate(true);
        complexACL1.setUserRole(userRole);
        fieldACL.setCreate(false);
        fieldACL.setUserRole(userRole);
        caseField.addComplexFieldACL(complexACL1);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        final List<ValidationError> validationErrors = result.getValidationErrors();
        assertAll(
            () -> assertThat(validationErrors, not(empty())),
            () -> assertThat(validationErrors.get(0).getDefaultMessage(), is("The access for case type 'case_type', "
            + "case field 'case_field', list element code 'Class.ClassMembers' is more than its parent"))
        );
    }

    @Test
    @DisplayName("should fail when a complex child has more access than its complex parent")
    public void shouldFailWhenComplexChildHasMoreAccessThanComplexParent() {
        complexACL1.setUserRole(userRole);
        complexACL1.setCreate(false);
        complexACL2.setUserRole(userRole);
        complexACL2.setCreate(true);
        caseField.addComplexFieldACLEntities(asList(complexACL1, complexACL2));
        fieldACL.setUserRole(userRole);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors(), not(empty())),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("The access for case type 'case_type', "
            + "case field 'case_field', list element code 'Class.ClassMembers.Children' is more than its parent"))
        );
    }

    @Test
    @DisplayName("should fail when a complex child has more access than its second level complex parent")
    public void shouldFailWhenComplexChildHasMoreAccessThanSecondLevelComplexParent() {
        complexACL1.setUserRole(userRole);
        complexACL1.setCreate(false);
        complexACL2.setUserRole(userRole);
        complexACL2.setCreate(false);
        complexACL3.setUserRole(userRole);
        complexACL3.setCreate(true);
        caseField.addComplexFieldACLEntities(asList(complexACL1, complexACL2, complexACL3));
        fieldACL.setUserRole(userRole);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors(), not(empty())),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("The access for case type 'case_type', "
            + "case field 'case_field', list element code 'Class.ClassMembers.Children.ChildFullName' is more than its parent"))
        );
    }


    @Test
    @DisplayName("should not fail when a complex children has correct access")
    public void shouldNotFailWhenComplexChildrenHasCorrectAccess() {
        complexACL1.setUserRole(userRole);
        complexACL1.setCreate(true);
        complexACL2.setUserRole(userRole);
        complexACL2.setCreate(false);
        complexACL3.setUserRole(userRole);
        complexACL3.setCreate(false);
        caseField.addComplexFieldACLEntities(asList(complexACL1, complexACL2, complexACL3));
        fieldACL.setUserRole(userRole);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }
}
