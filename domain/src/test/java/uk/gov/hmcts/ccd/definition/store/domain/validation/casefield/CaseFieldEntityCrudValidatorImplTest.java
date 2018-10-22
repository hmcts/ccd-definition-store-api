package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public class CaseFieldEntityCrudValidatorImplTest {

    private CaseFieldEntityCrudValidatorImpl validator;

    private CaseTypeEntity caseType;

    private CaseFieldEntity caseField;

    private CaseFieldACLEntity caseFieldUserRole;

    private CaseFieldEntityValidationContext context;

    @Before
    public void setup() {

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
    public void goodCrud() {
        caseFieldUserRole.setCrudAsString("Cr Du");
        final ValidationResult result = validator.validate(caseField, context);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void crudTooLong() {

        caseFieldUserRole.setCrudAsString(" CRUD   DD ");

        final ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseFieldEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value ' CRUD   DD ' for case type 'case type', case field 'case field'"));
    }

    @Test
    public void blankCrud() {

        final ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseFieldEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value '' for case type 'case type', case field 'case field'"));
    }

    @Test
    public void invalidCrud() {

        caseFieldUserRole.setCrudAsString("X");

        final ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(CaseFieldEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value 'X' for case type 'case type', case field 'case field'"));
    }
}
