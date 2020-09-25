package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;

class EventCaseFieldMetadataValidatorImplTest {

    private final EventCaseFieldMetadataValidatorImpl validator = new EventCaseFieldMetadataValidatorImpl();

    @Test
    @DisplayName(
        "Should return validation result with exception when metadata case field id is defined in case event fields")
    void shouldReturnValidationResultWithError_whenNonMetadataCaseFieldIdContainsSquareBrackets() {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference("event");
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setEvent(eventEntity);
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("[FIELD]");
        caseField.setDataFieldType(DataFieldType.METADATA);
        eventCaseFieldEntity.setCaseField(caseField);
        EventCaseFieldEntityValidationContext context = mock(EventCaseFieldEntityValidationContext.class);
        eventCaseFieldEntity.setDisplayContext(DisplayContext.MANDATORY);

        ValidationResult result = validator.validate(eventCaseFieldEntity, context);

        assertThat(result.getValidationErrors(), hasSize(1));
        Assert.assertThat(result.getValidationErrors().get(0),
            instanceOf(EventCaseFieldMetadataValidatorImpl.ValidationError.class));
        Assert.assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "'[FIELD]' is a metadata field and cannot be editable for event with reference 'event'"));
    }

    @Test
    @DisplayName("Should return validation result with no errors when metadata case field has readonly display context")
    void shouldReturnValidationResultWithNoError_whenMetadataCaseFieldIdDisplayContextIsReadOnly() {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("FIELD");
        caseField.setDataFieldType(DataFieldType.METADATA);
        eventCaseFieldEntity.setCaseField(caseField);
        EventCaseFieldEntityValidationContext context = mock(EventCaseFieldEntityValidationContext.class);
        eventCaseFieldEntity.setDisplayContext(DisplayContext.READONLY);

        ValidationResult result = validator.validate(eventCaseFieldEntity, context);

        assertThat(result.getValidationErrors(), hasSize(0));
    }

}
