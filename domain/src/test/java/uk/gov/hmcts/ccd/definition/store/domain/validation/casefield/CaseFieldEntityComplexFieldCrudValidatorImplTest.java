package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class CaseFieldEntityComplexFieldCrudValidatorImplTest {
    private static final String LIST_ELEMENT_CODE = "CODE_1";
    private CaseFieldEntityComplexFieldCrudValidatorImpl classUnderTest;
    private CaseTypeEntity caseType;
    private CaseFieldEntity caseField;
    private ComplexFieldACLEntity complexFieldACLEntity;
    private CaseFieldEntityValidationContext context;

    @BeforeEach
    void setUp() {
        classUnderTest = new CaseFieldEntityComplexFieldCrudValidatorImpl();
        complexFieldACLEntity = new ComplexFieldACLEntity();
        complexFieldACLEntity.setListElementCode(LIST_ELEMENT_CODE);

        caseType = new CaseTypeEntity();
        caseType.setReference("case type");

        caseField = new CaseFieldEntity();
        caseField.setReference("case field");
        caseField.addComplexFieldACL(complexFieldACLEntity);

        context = new CaseFieldEntityValidationContext(caseType);
    }

    @Test
    public void goodCrud() {
        complexFieldACLEntity.setCrudAsString("Cr Du");
        final ValidationResult result = classUnderTest.validate(caseField, context);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void crudTooLong() {
        complexFieldACLEntity.setCrudAsString(" CRUD   DD ");

        final ValidationResult result = classUnderTest.validate(caseField, context);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0),
                instanceOf(CaseFieldEntityInvalidComplexCrudValidationError.class)),
            () -> assertThat(((CaseFieldEntityInvalidComplexCrudValidationError)
                result.getValidationErrors().get(0))
                .getAuthorisationCaseFieldValidationContext()
                .getCaseFieldReference(), is(caseField.getReference())),
            () -> assertThat(((CaseFieldEntityInvalidComplexCrudValidationError)
                result.getValidationErrors().get(0)).getComplexFieldACLEntity(), is(complexFieldACLEntity)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
                "Invalid CRUD value ' CRUD   DD ' for case type 'case type', case field 'case field', "
                    + "list element code '" + LIST_ELEMENT_CODE + "'"))
        );
    }

    @Test
    public void blankCrud() {
        final ValidationResult result = classUnderTest.validate(caseField, context);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0), instanceOf(
                CaseFieldEntityInvalidComplexCrudValidationError.class)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
                "Invalid CRUD value '' for case type 'case type', case field 'case field', "
                    + "list element code '" + LIST_ELEMENT_CODE + "'"))
        );
    }

    @Test
    public void invalidCrud() {
        complexFieldACLEntity.setCrudAsString("X");

        final ValidationResult result = classUnderTest.validate(caseField, context);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0), instanceOf(
                CaseFieldEntityInvalidComplexCrudValidationError.class)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
                "Invalid CRUD value 'X' for case type 'case type', case field 'case field', "
                    + "list element code '" + LIST_ELEMENT_CODE + "'"))
        );
    }
}
