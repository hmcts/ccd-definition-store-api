package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

class CaseFieldEntityCrudValidatorImplTest {

    private CaseFieldEntityCrudValidatorImpl validator;

    private CaseTypeEntity caseType;

    private CaseFieldEntity caseField;

    private CaseFieldACLEntity caseFieldUserRole;

    private CaseFieldEntityValidationContext context;

    @BeforeEach
    void setup() {

        validator = new CaseFieldEntityCrudValidatorImpl();

        caseFieldUserRole = new CaseFieldACLEntity();

        caseType = new CaseTypeEntity();
        caseType.setReference("case type");

        caseField = new CaseFieldEntity();
        caseField.setReference("case field");
        caseField.addCaseFieldACL(caseFieldUserRole);

        context = new CaseFieldEntityValidationContext(caseType);
    }

    @Test
    void goodCrud() {
        caseFieldUserRole.setCrudAsString("Cr Du");
        final ValidationResult result = validator.validate(caseField, context);

        assertThat(result.isValid(), is(true));
    }

    @Test
    void crudTooLong() {

        caseFieldUserRole.setCrudAsString(" CRUD   DD ");

        final ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseFieldEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value ' CRUD   DD ' for case type 'case type', case field 'case field'"));
    }

    @Test
    void blankCrud() {

        final ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseFieldEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value '' for case type 'case type', case field 'case field'"));
    }

    @Test
    void invalidCrud() {

        caseFieldUserRole.setCrudAsString("X");

        final ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseFieldEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value 'X' for case type 'case type', case field 'case field'"));
    }
}
