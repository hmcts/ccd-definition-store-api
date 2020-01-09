package uk.gov.hmcts.ccd.definition.store.domain.validation.state;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

public class StateEntityACLValidatorImplTest {
    private StateEntity stateEntity;
    private UserRoleEntity userRoleEntity;
    private StateACLEntity stateACLEntity;
    private StateEntityACLValidatorImpl validator;
    private StateEntityValidationContext stateEntityValidationContext;

    @Before
    public void setUp() {
        validator = new StateEntityACLValidatorImpl();
        stateACLEntity = new StateACLEntity();
        userRoleEntity = new UserRoleEntity();
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("case type");
        stateEntity = new StateEntity();
        stateEntity.setReference("case state");
        stateACLEntity.setStateEntity(stateEntity);
        stateACLEntity.setUserRole(userRoleEntity);
        stateEntity.addStateACL(stateACLEntity);
        stateEntityValidationContext = new StateEntityValidationContext(caseType);
    }

    @Test
    public void shouldHaveValidationErrorWhenUserNotFound() {
        stateACLEntity.setUserRole(null);
        stateACLEntity.setUserRoleId("nf_user_role_id");

        final ValidationResult result = validator.validate(stateEntity, stateEntityValidationContext);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(StateEntityACLValidatorImpl.ValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid UserRole nf_user_role_id for case type 'case type', case state 'case state'"));
    }

    @Test
    public void shouldHaveNoValidationErrorWhenUserFound() {
        stateACLEntity.setUserRole(userRoleEntity);

        final ValidationResult result = validator.validate(stateEntity, stateEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }
}
