package uk.gov.hmcts.ccd.definition.store.domain.validation.state;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public class StateEntityCrudValidatorImplTest {
    private StateEntity stateEntity;
    private StateACLEntity userRoleEntity;
    private StateEntityValidationContext context;
    private StateEntityCrudValidatorImpl validator;

    @Before
    public void setup() {

        validator = new StateEntityCrudValidatorImpl();

        userRoleEntity = new StateACLEntity();
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("case type");

        stateEntity = new StateEntity();
        stateEntity.setReference("case state");
        stateEntity.addStateACL(userRoleEntity);

        context = new StateEntityValidationContext(caseType);
    }

    @Test
    public void goodCrud() {
        userRoleEntity.setCrudAsString("Cr Du");
        final ValidationResult result = validator.validate(stateEntity, context);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void crudTooLong() {

        userRoleEntity.setCrudAsString(" CRUD   DD ");

        final ValidationResult result = validator.validate(stateEntity, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(StateEntityCrudValidatorImpl.ValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value ' CRUD   DD ' for case type 'case type', state 'case state'"));
    }

    @Test
    public void blankCrud() {

        final ValidationResult result = validator.validate(stateEntity, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(StateEntityCrudValidatorImpl.ValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value '' for case type 'case type', state 'case state'"));
    }

    @Test
    public void invalidCrud() {

        userRoleEntity.setCrudAsString("X");

        final ValidationResult result = validator.validate(stateEntity, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(StateEntityCrudValidatorImpl.ValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value 'X' for case type 'case type', state 'case state'"));
    }
}
