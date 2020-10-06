package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EventCaseFieldDisplayContextValidatorImplTest {

    @Test
    public void shouldFireValidationErrorWhenDisplayContextDoesNotExist() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntity();
        ValidationResult validationResult
            = new EventCaseFieldDisplayContextValidatorImpl().validate(eventCaseFieldEntity, null);

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());

        assertTrue(validationResult.getValidationErrors().get(0)
            instanceof EventCaseFieldDisplayContextValidatorImpl.ValidationError);

        assertEquals(
            eventCaseFieldEntity,
            ((EventCaseFieldDisplayContextValidatorImpl.ValidationError) validationResult.getValidationErrors().get(0))
                .getEntity()
        );
    }

    @Test
    public void shouldNotFireValidationErrorWhenDisplayContextExists() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntity();
        eventCaseFieldEntity.setDisplayContext(DisplayContext.OPTIONAL);
        ValidationResult validationResult
            = new EventCaseFieldDisplayContextValidatorImpl().validate(eventCaseFieldEntity, null);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    private EventCaseFieldEntity eventCaseFieldEntity() {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference("Case1");
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference("Event1");
        eventCaseFieldEntity.setEvent(eventEntity);
        return eventCaseFieldEntity;
    }
}
