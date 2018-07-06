package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

class EventEntityCanSaveDraftValidatorImplTest {

    private EventEntityCanSaveDraftValidatorImpl classUnderTest;
    private EventEntity eventEntity;
    private EventEntityValidationContext context;

    @BeforeEach
    void setUp() {
        CaseTypeEntity caseTypeEntity;
        classUnderTest = new EventEntityCanSaveDraftValidatorImpl();
        caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("case type");

        eventEntity = new EventEntity();
        eventEntity.setName("Some Event");
        eventEntity.setCanSaveDraft(true);
        eventEntity.setReference("case event");
        context = new EventEntityValidationContext(caseTypeEntity);
    }

    @Test
    @DisplayName("should allow enable save draft when event has no pre-state i.e. Create type event")
    void shouldAllowEnabling() {
        final ValidationResult result = classUnderTest.validate(eventEntity, context);

        assertThat(result.isValid(), is(true));
    }

    @Test
    @DisplayName("should not allow enable save draft when event has a pre-state i.e. Not a Create type event")
    void shouldNotAllowEnabling() {
        StateEntity stateEntity = new StateEntity();
        stateEntity.setName("SomeState");
        eventEntity.getPreStates().add(stateEntity);
        final ValidationResult result = classUnderTest.validate(eventEntity, context);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("Enable saving draft is only "
            + "available for Create events. Event Some Event is not eligible."));
    }
}
