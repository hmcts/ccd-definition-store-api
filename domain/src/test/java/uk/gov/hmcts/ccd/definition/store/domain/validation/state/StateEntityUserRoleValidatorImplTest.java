package uk.gov.hmcts.ccd.definition.store.domain.validation.state;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateUserRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public class StateEntityUserRoleValidatorImplTest {
    private StateEntity stateEntity;
    private UserRoleEntity userRoleEntity;
    private StateUserRoleEntity stateUserRoleEntity;
    private StateEntityUserRoleValidatorImpl validator;
    private StateEntityValidationContext stateEntityValidationContext;

    @Before
    public void setup() {
        validator = new StateEntityUserRoleValidatorImpl();
        stateUserRoleEntity = new StateUserRoleEntity();
        userRoleEntity = new UserRoleEntity();
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("case type");
        stateEntity = new StateEntity();
        stateEntity.setReference("case state");
        stateUserRoleEntity.setStateEntity(stateEntity);
        stateUserRoleEntity.setUserRole(userRoleEntity);
        stateEntity.addStateUserRole(stateUserRoleEntity);
        stateEntityValidationContext = new StateEntityValidationContext(caseType);
    }

    @Test
    public void shouldHaveValidationError_whenUserNotFound() {
        stateUserRoleEntity.setUserRole(null);

        final ValidationResult result = validator.validate(stateEntity, stateEntityValidationContext);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(StateEntityUserRoleValidatorImpl.ValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid UserRole for case type 'case type', case state 'case state'"));
    }

    @Test
    public void shouldHaveNoValidationError_whenUserFound() {
        stateUserRoleEntity.setUserRole(userRoleEntity);

        final ValidationResult result = validator.validate(stateEntity, stateEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }
}
