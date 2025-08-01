package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_UK;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;

class CaseFieldEntityComplexFieldACLValidatorImplTest {

    private static final String ACCESS_PROFILE_1 = "AccessProfile1";
    private static final String CLASS = "Class";
    private static final String CLASS_MEMBERS = "ClassMembers";
    private static final String CHILDREN = "Children";
    private static final String CHILD_FULL_NAME = "ChildFullName";
    private CaseFieldEntityComplexFieldACLValidatorImpl validator;

    private ComplexFieldACLEntity complexACL0;
    private ComplexFieldACLEntity complexACL1;
    private ComplexFieldACLEntity complexACL2;
    private ComplexFieldACLEntity complexACL3;
    private ComplexFieldACLEntity complexACL4;
    private ComplexFieldACLEntity complexACL5;
    private ComplexFieldACLEntity complexACL6;
    private ComplexFieldACLEntity complexACL7;
    private ComplexFieldACLEntity complexACL8;

    private CaseFieldACLEntity fieldACL;

    private CaseFieldEntity caseField;

    @Mock
    private AccessProfileEntity accessProfile;

    @Mock
    private CaseFieldEntityValidationContext caseFieldEntityValidationContext;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        accessProfile = new AccessProfileEntity();
        accessProfile.setReference(ACCESS_PROFILE_1);

        complexACL0 = new ComplexFieldACLEntity();
        complexACL0.setListElementCode(CLASS);
        complexACL1 = new ComplexFieldACLEntity();
        complexACL1.setListElementCode(CLASS + "." + CLASS_MEMBERS);
        complexACL2 = new ComplexFieldACLEntity();
        complexACL2.setListElementCode(CLASS + "." + CLASS_MEMBERS + "." + CHILDREN);
        complexACL3 = new ComplexFieldACLEntity();
        complexACL3.setListElementCode(CLASS + "." + CLASS_MEMBERS + "." + CHILDREN + "." + CHILD_FULL_NAME);
        complexACL4 = new ComplexFieldACLEntity();
        complexACL4.setListElementCode(CLASS_MEMBERS);
        complexACL5 = new ComplexFieldACLEntity();
        complexACL5.setListElementCode(PREDEFINED_COMPLEX_ADDRESS_GLOBAL);
        complexACL6 = new ComplexFieldACLEntity();
        complexACL6.setListElementCode(PREDEFINED_COMPLEX_ADDRESS_GLOBAL + ".AddressLine1");
        complexACL7 = new ComplexFieldACLEntity();
        complexACL7.setListElementCode(
            CLASS + "." + CLASS_MEMBERS + "." + CHILDREN + "." + PREDEFINED_COMPLEX_ADDRESS_GLOBAL);
        complexACL8 = new ComplexFieldACLEntity();
        complexACL8.setListElementCode(
            CLASS + "." + CLASS_MEMBERS + "." + CHILDREN + "." + PREDEFINED_COMPLEX_ADDRESS_GLOBAL + ".AddressLine1");

        given(caseFieldEntityValidationContext.getCaseReference()).willReturn("case_type");

        caseField = new CaseFieldEntity();
        caseField.setReference("case_field");

        caseField.setFieldType(
            newType("New")
                .addFieldToComplex(CLASS, newType(CLASS)
                    .addFieldToComplex(CLASS_MEMBERS, newType(CLASS_MEMBERS)
                        .addFieldToComplex(CHILDREN, newType(CHILDREN)
                            .addFieldToComplex(CHILD_FULL_NAME, newType(CHILD_FULL_NAME).buildComplex())
                            .addFieldToComplex(
                                PREDEFINED_COMPLEX_ADDRESS_GLOBAL, newType(PREDEFINED_COMPLEX_ADDRESS_GLOBAL)
                                    .buildComplex())
                            .buildComplex())
                        .buildComplex())
                    .buildComplex())
                .addFieldToComplex(CLASS_MEMBERS, newType("Text").build())
                .addFieldToComplex(PREDEFINED_COMPLEX_ADDRESS_GLOBAL, newType(PREDEFINED_COMPLEX_ADDRESS_GLOBAL)
                    .buildComplex())
                .buildComplex()
        );

        fieldACL = new CaseFieldACLEntity();

        validator = new CaseFieldEntityComplexFieldACLValidatorImpl();
    }

    @Test
    @DisplayName("should fail when access profile not found")
    public void shouldHaveValidationErrorWhenAccessProfileNotFound() {
        complexACL0.setAccessProfile(null);
        complexACL0.setAccessProfileId("nf_access_profile_id");
        caseField.addComplexFieldACL(complexACL0);
        fieldACL.setAccessProfile(accessProfile);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(2)),
            () -> assertThat(result.getValidationErrors().get(0), instanceOf(
                CaseFieldEntityInvalidAccessProfileValidationError.class)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
                "Invalid AccessProfile nf_access_profile_id for case type 'case_type', case field 'case_field'"))
        );
    }

    @Test
    @DisplayName("should not fail when access profile is found")
    public void shouldHaveNoValidationErrorWhenAccessProfileFound() {
        complexACL0.setAccessProfile(accessProfile);
        complexACL0.setCreate(true);
        caseField.addComplexFieldACL(complexACL0);
        fieldACL.setCreate(true);
        fieldACL.setAccessProfile(accessProfile);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }

    @Test
    @DisplayName("should fail when parent caseField has no ACL defined")
    public void shouldFailWhenCaseFieldParentHasNoAcl() {
        complexACL0.setAccessProfile(accessProfile);
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
    public void shouldFailWhenACLDefinitionForComplexParentNodeIsMissing() {
        complexACL1.setCreate(true);
        complexACL1.setAccessProfile(accessProfile);
        fieldACL.setCreate(true);
        fieldACL.setAccessProfile(accessProfile);
        caseField.addComplexFieldACL(complexACL1);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        final List<ValidationError> validationErrors = result.getValidationErrors();
        assertAll(
            () -> assertThat(validationErrors, not(empty())),
            () -> assertThat(validationErrors.get(0).getDefaultMessage(),
                is("Parent list element code 'Class' is missing for list element code 'Class.ClassMembers'"))
        );
    }

    @Test
    @DisplayName("should fail when complex child element has higher access than parent")
    public void shouldFailWhenComplexChildHasHigherAccessThanCaseFieldParent() {
        complexACL0.setAccessProfile(accessProfile);
        complexACL0.setCreate(true);
        fieldACL.setCreate(false);
        fieldACL.setAccessProfile(accessProfile);
        caseField.addComplexFieldACLEntities(asList(complexACL0));
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        final List<ValidationError> validationErrors = result.getValidationErrors();
        assertAll(
            () -> assertThat(validationErrors, not(empty())),
            () -> assertThat(validationErrors.get(0).getDefaultMessage(),
                is("List element code 'Class' has higher"
                + " access than case field 'case_field'"))
        );
    }

    @Test
    @DisplayName("should fail when a complex child has higher access than its complex parent")
    public void shouldFailWhenComplexChildHasHigherAccessThanComplexParent() {
        complexACL0.setAccessProfile(accessProfile);
        complexACL0.setCreate(false);
        complexACL1.setAccessProfile(accessProfile);
        complexACL1.setCreate(false);
        complexACL2.setAccessProfile(accessProfile);
        complexACL2.setCreate(true);
        caseField.addComplexFieldACLEntities(asList(complexACL0, complexACL1, complexACL2));
        fieldACL.setAccessProfile(accessProfile);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors(), not(empty())),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("List element code "
                + "'Class.ClassMembers.Children' has higher access than its parent 'Class'"))
        );
    }

    @Test
    @DisplayName("should not fail for partial list element code matching on parent level")
    public void shouldNotFailForPartialListElementCodeMatchingOnParentLevel() {
        complexACL0.setAccessProfile(accessProfile);
        complexACL0.setCreate(true);
        complexACL1.setAccessProfile(accessProfile);
        complexACL1.setCreate(true);
        complexACL2.setAccessProfile(accessProfile);
        complexACL2.setCreate(true);
        complexACL3.setAccessProfile(accessProfile);
        complexACL3.setCreate(true);
        complexACL4.setAccessProfile(accessProfile);
        complexACL4.setCreate(false);
        caseField.addComplexFieldACLEntities(asList(complexACL0, complexACL1, complexACL2, complexACL3, complexACL4));
        fieldACL.setAccessProfile(accessProfile);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }

    @Test
    @DisplayName("should fail when a complex child has higher access than its second level complex parent")
    public void shouldFailWhenComplexChildHasHigherAccessThanSecondLevelComplexParent() {
        complexACL0.setAccessProfile(accessProfile);
        complexACL0.setCreate(true);
        complexACL1.setAccessProfile(accessProfile);
        complexACL1.setCreate(false);
        complexACL2.setAccessProfile(accessProfile);
        complexACL2.setCreate(true);
        complexACL3.setAccessProfile(accessProfile);
        complexACL3.setCreate(true);
        caseField.addComplexFieldACLEntities(asList(complexACL0, complexACL1, complexACL2, complexACL3));
        fieldACL.setAccessProfile(accessProfile);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors(), not(empty())),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("List element code "
                + "'Class.ClassMembers.Children' has higher access than its parent 'Class.ClassMembers'"))
        );
    }

    @Test
    @DisplayName("should succeed when a complex children has correct access")
    public void shouldSucceedWhenComplexChildrenHasCorrectAccess() {
        complexACL0.setAccessProfile(accessProfile);
        complexACL0.setCreate(true);
        complexACL1.setAccessProfile(accessProfile);
        complexACL1.setCreate(true);
        complexACL2.setAccessProfile(accessProfile);
        complexACL2.setCreate(false);
        complexACL3.setAccessProfile(accessProfile);
        complexACL3.setCreate(false);
        caseField.addComplexFieldACLEntities(asList(complexACL0, complexACL1, complexACL2, complexACL3));
        fieldACL.setAccessProfile(accessProfile);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }

    @Test
    @DisplayName("should fail when a predefined complex child has been defined")
    public void shouldFailWhenPredefinedComplexChildHasBeenDefined() {
        complexACL0.setAccessProfile(accessProfile);
        complexACL0.setCreate(true);
        complexACL1.setAccessProfile(accessProfile);
        complexACL1.setCreate(true);
        complexACL2.setAccessProfile(accessProfile);
        complexACL2.setCreate(false);
        complexACL5.setAccessProfile(accessProfile);
        complexACL5.setCreate(false);
        complexACL6.setAccessProfile(accessProfile);
        complexACL6.setCreate(false);
        complexACL7.setAccessProfile(accessProfile);
        complexACL7.setCreate(false);
        complexACL8.setAccessProfile(accessProfile);
        complexACL8.setCreate(false);
        caseField.addComplexFieldACLEntities(asList(complexACL0, complexACL1, complexACL2, complexACL5, complexACL6,
            complexACL7, complexACL8));
        fieldACL.setAccessProfile(accessProfile);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors(), not(empty())),
            () -> assertThat(result.getValidationErrors().size(), is(2)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("List element code "
                + "'AddressGlobal.AddressLine1' refers to a predefined complex type parent 'AddressGlobal' "
                + "and is not allowed")),
            () -> assertThat(result.getValidationErrors().get(1).getDefaultMessage(), is("List element code "
                + "'Class.ClassMembers.Children.AddressGlobal.AddressLine1' refers to "
                + "a predefined complex type parent 'AddressGlobal' and is not allowed"))
        );
    }

    @Test
    @DisplayName("should fail when a predefined complex child has been defined for root level complex field")
    public void shouldFailWhenPredefinedComplexChildHasBeenDefinedForRootLevelComplexField() {

        ComplexFieldACLEntity complexACL10 = new ComplexFieldACLEntity();
        complexACL10.setListElementCode("parent.non-existent");
        complexACL10.setAccessProfile(accessProfile);
        complexACL10.setCreate(true);
        ComplexFieldACLEntity complexACL9 = new ComplexFieldACLEntity();
        complexACL9.setListElementCode("parent");
        complexACL9.setAccessProfile(accessProfile);
        complexACL9.setCreate(true);
        caseField.addComplexFieldACLEntities(asList(complexACL10, complexACL9));
        fieldACL.setAccessProfile(accessProfile);
        fieldACL.setCreate(true);
        caseField.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors(), not(empty())),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("List element code "
                + "'parent.non-existent' is not a valid reference..."))
        );
    }

    @Test
    @DisplayName("should fail when a non existent child has been defined")
    public void shouldFailWhenANonExistentChildHasBeenDefined() {

        CaseFieldEntity preDefined = new CaseFieldEntity();
        preDefined.setReference("personAddress");
        preDefined.setFieldType(
            newType(PREDEFINED_COMPLEX_ADDRESS_UK)
                .addFieldToComplex("AddressLine1", newType("Text").build())
                .addFieldToComplex("AddressLine2", newType("Text").build())
                .addFieldToComplex("Postcode", newType("Text").build())
                .buildComplex()
        );
        ComplexFieldACLEntity complexACL11 = new ComplexFieldACLEntity();
        complexACL11.setListElementCode("AddressLine1");
        complexACL11.setAccessProfile(accessProfile);
        complexACL11.setCreate(true);
        preDefined.addComplexFieldACLEntities(asList(complexACL11));
        fieldACL.setAccessProfile(accessProfile);
        fieldACL.setCreate(true);
        preDefined.addCaseFieldACL(fieldACL);

        final ValidationResult result = validator.validate(preDefined, caseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors(), not(empty())),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("CaseField "
                + "'personAddress' is a predefined complex type and list element code 'AddressLine1' is not allowed"))
        );
    }
}
