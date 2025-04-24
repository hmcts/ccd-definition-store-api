package uk.gov.hmcts.ccd.definition.store.domain.validation.state;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class StateEntityACLValidatorImplTest {
    private StateEntity stateEntity;
    private AccessProfileEntity accessProfileEntity;
    private StateACLEntity stateACLEntity;
    private StateEntityACLValidatorImpl validator;
    private StateEntityValidationContext stateEntityValidationContext;

    @BeforeEach
    public void setUp() {
        validator = new StateEntityACLValidatorImpl();
        stateACLEntity = new StateACLEntity();
        accessProfileEntity = new AccessProfileEntity();
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("case type");
        stateEntity = new StateEntity();
        stateEntity.setReference("case state");
        stateACLEntity.setStateEntity(stateEntity);
        stateACLEntity.setAccessProfile(accessProfileEntity);
        stateEntity.addStateACL(stateACLEntity);
        stateEntityValidationContext = new StateEntityValidationContext(caseType);
    }

    @Test
    public void shouldHaveValidationErrorWhenAccessProfileNotFound() {
        stateACLEntity.setAccessProfile(null);
        stateACLEntity.setAccessProfileId("nf_access_profile_id");

        final ValidationResult result = validator.validate(stateEntity, stateEntityValidationContext);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(StateEntityACLValidatorImpl.ValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid AccessProfile nf_access_profile_id for case type 'case type', case state 'case state'"));
    }

    @Test
    public void shouldHaveNoValidationErrorWhenAccessProfileFound() {
        stateACLEntity.setAccessProfile(accessProfileEntity);

        final ValidationResult result = validator.validate(stateEntity, stateEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }
}
