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

    private ComplexFieldACLEntity complexACL0;
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

        complexACL0 = new ComplexFieldACLEntity();
        complexACL0.setListElementCode("Class");
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
        complexACL0.setUserRole(null);
        caseField.addComplexFieldACL(complexACL0);
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
        complexACL0.setUserRole(userRole);
        complexACL0.setCreate(true);
        caseField.addComplexFieldACL(complexACL0);
        fieldACL.setCreate(true);
        fieldACL.setUserRole(userRole);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }

    @Test
    @DisplayName("should fail when parent caseField has no ACL defined")
    public void shouldSucceedWhenComplexChildHasLessOrEqualAccessComparedToCaseFieldParent() {
        complexACL0.setUserRole(userRole);
        complexACL0.setCreate(true);
        caseField.addComplexFieldACL(complexACL0);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), not(empty()));
    }

    @Test
    @DisplayName("should fail when parent caseField has no ACL defined")
    public void shouldFailWhenCaseFieldParentHasNoAcl() {
        complexACL0.setUserRole(userRole);
        complexACL0.setCreate(true);
        caseField.addComplexFieldACL(complexACL0);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        final List<ValidationError> validationErrors = result.getValidationErrors();
        assertAll(
            () -> assertThat(validationErrors, not(empty())),
            () -> assertThat(validationErrors.get(0).getDefaultMessage(), is("Parent case field 'Class' doesn't "
                + "have any ACL defined for List element code 'case_field'"))
        );
    }

    @Test
    @DisplayName("should fail when ACL definition for complex parent node is missing")
    public void shouldFailWhenACLDefinitionForComplexParentNode() {
        complexACL1.setCreate(true);
        complexACL1.setUserRole(userRole);
        fieldACL.setCreate(true);
        fieldACL.setUserRole(userRole);
        caseField.addComplexFieldACL(complexACL1);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        final List<ValidationError> validationErrors = result.getValidationErrors();
        assertAll(
            () -> assertThat(validationErrors, not(empty())),
            () -> assertThat(validationErrors.get(0).getDefaultMessage(), is("Parent list element code 'Class' is missing for list element code 'Class.ClassMembers'"))
        );
    }

    @Test
    @DisplayName("should fail when complex child element has more access than parent")
    public void shouldFailWhenComplexChildHasMoreAccessThanCaseFieldParent() {
        complexACL0.setUserRole(userRole);
        complexACL0.setCreate(true);
        fieldACL.setCreate(false);
        fieldACL.setUserRole(userRole);
        caseField.addComplexFieldACLEntities(asList(complexACL0));
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        final List<ValidationError> validationErrors = result.getValidationErrors();
        assertAll(
            () -> assertThat(validationErrors, not(empty())),
            () -> assertThat(validationErrors.get(0).getDefaultMessage(), is("List element code 'Class' has more"
                + " access than case field 'case_field'"))
        );
    }

    @Test
    @DisplayName("should fail when a complex child has more access than its complex parent")
    public void shouldFailWhenComplexChildHasMoreAccessThanComplexParent() {
        complexACL0.setUserRole(userRole);
        complexACL0.setCreate(false);
        complexACL1.setUserRole(userRole);
        complexACL1.setCreate(false);
        complexACL2.setUserRole(userRole);
        complexACL2.setCreate(true);
        caseField.addComplexFieldACLEntities(asList(complexACL0, complexACL1, complexACL2));
        fieldACL.setUserRole(userRole);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors(), not(empty())),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("List element code "
                + "'Class.ClassMembers.Children' has more access than its parent 'Class'"))
        );
    }

    @Test
    @DisplayName("should fail when a complex child has more access than its second level complex parent")
    public void shouldFailWhenComplexChildHasMoreAccessThanSecondLevelComplexParent() {
        complexACL1.setUserRole(userRole);
        complexACL1.setCreate(false);
        complexACL2.setUserRole(userRole);
        complexACL2.setCreate(true);
        complexACL3.setUserRole(userRole);
        complexACL3.setCreate(true);
        caseField.addComplexFieldACLEntities(asList(complexACL1, complexACL2, complexACL3));
        fieldACL.setUserRole(userRole);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors(), not(empty())),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("List element code "
                + "'Class.ClassMembers.Children' has more access than its parent 'Class.ClassMembers'"))
        );
    }

    @Test
    @DisplayName("should not fail when a complex children has correct access")
    public void shouldNotFailWhenComplexChildrenHasCorrectAccess() {
        complexACL0.setUserRole(userRole);
        complexACL0.setCreate(true);
        complexACL1.setUserRole(userRole);
        complexACL1.setCreate(true);
        complexACL2.setUserRole(userRole);
        complexACL2.setCreate(false);
        complexACL3.setUserRole(userRole);
        complexACL3.setCreate(false);
        caseField.addComplexFieldACLEntities(asList(complexACL0, complexACL1, complexACL2, complexACL3));
        fieldACL.setUserRole(userRole);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }

    @Test
    public void testparseParentCodes() {
        String listElementCodes0 = ".awesome";
        final List<String> parentCodes0 = validator.parseParentCodes(listElementCodes0);
        assertThat(parentCodes0, empty());

        String listElementCodes1 = "some";
        final List<String> parentCodes1 = validator.parseParentCodes(listElementCodes1);
        assertThat(parentCodes1, empty());

        String listElementCodes2 = "some.thing";
        final List<String> parentCodes2 = validator.parseParentCodes(listElementCodes2);
        assertThat(parentCodes2, not(empty()));
        assertThat(parentCodes2.size(), is(1));
        assertThat(parentCodes2.get(0), is("some"));

        String listElementCodes3 = "some.thing.coming";
        final List<String> parentCodes3 = validator.parseParentCodes(listElementCodes3);
        assertThat(parentCodes3, not(empty()));
        assertThat(parentCodes3.size(), is(2));
        assertThat(parentCodes3.get(0), is("some.thing"));
        assertThat(parentCodes3.get(1), is("some"));

        String listElementCodes4 = "some.thing.coming.this.way";
        final List<String> parentCodes4 = validator.parseParentCodes(listElementCodes4);
        assertThat(parentCodes4, not(empty()));
        assertThat(parentCodes4.size(), is(4));
        assertThat(parentCodes4.get(0), is("some.thing.coming.this"));
        assertThat(parentCodes4.get(1), is("some.thing.coming"));
        assertThat(parentCodes4.get(2), is("some.thing"));
        assertThat(parentCodes4.get(3), is("some"));
    }
}
