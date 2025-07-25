package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

class CaseTypeEntityCrudValidatorImplTest {

    private CaseTypeEntityCrudValidatorImpl validator;

    private CaseTypeEntity caseType;

    private CaseTypeACLEntity caseTypeUserRole;

    @BeforeEach
    void setup() {

        validator = new CaseTypeEntityCrudValidatorImpl();

        caseTypeUserRole = new CaseTypeACLEntity();

        caseType = new CaseTypeEntity();
        caseType.setReference("case type");
        caseType.addCaseTypeACL(caseTypeUserRole);
    }

    @Test
    void goodCrud() {
        caseTypeUserRole.setCrudAsString("Cr Du");
        final ValidationResult result = validator.validate(caseType);

        assertThat(result.isValid(), is(true));
    }

    @Test
    void crudTooLong() {

        caseTypeUserRole.setCrudAsString(" CRUD   DD ");

        final ValidationResult result = validator.validate(caseType);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseTypeEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value ' CRUD   DD ' for case type 'case type'"));
    }

    @Test
    void blankCrud() {

        final ValidationResult result = validator.validate(caseType);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseTypeEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value '' for case type 'case type'"));
    }

    @Test
    void invalidCrud() {

        caseTypeUserRole.setCrudAsString("X");

        final ValidationResult result = validator.validate(caseType);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseTypeEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value 'X' for case type 'case type'"));
    }
}
