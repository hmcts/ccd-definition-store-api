package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_ORDER_SUMMARY;

public class OrderSummaryFieldTypeToDisplayContextValidatorTest {

    private final OrderSummaryFieldTypeToDisplayContextValidator classUnderTest = new OrderSummaryFieldTypeToDisplayContextValidator();

    @Test
    public void shouldReturnValidationErrorIfFieldTypeOptional() throws Exception {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setDisplayContext(DisplayContext.OPTIONAL);
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(BASE_ORDER_SUMMARY);
        caseFieldEntity.setFieldType(fieldType);
        eventCaseFieldEntity.setCaseField(caseFieldEntity);

        Optional<ValidationError> validationError = classUnderTest.validate(eventCaseFieldEntity);

        assertAll(
            () -> assertThat(validationError.isPresent(), is(true)),
            () -> assertThat(validationError.get(),
                             hasProperty("defaultMessage", equalTo("OrderSummary field type can only be configured with 'READONLY' DisplayContext")))
        );
    }

    @Test
    public void shouldReturnValidationErrorIfFieldTypeMandatory() throws Exception {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setDisplayContext(DisplayContext.MANDATORY);
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(BASE_ORDER_SUMMARY);
        caseFieldEntity.setFieldType(fieldType);
        eventCaseFieldEntity.setCaseField(caseFieldEntity);

        Optional<ValidationError> validationError = classUnderTest.validate(eventCaseFieldEntity);

        assertAll(
            () -> assertThat(validationError.isPresent(), is(true)),
            () -> assertThat(validationError.get(),
                             hasProperty("defaultMessage", equalTo("OrderSummary field type can only be configured with 'READONLY' DisplayContext")))
        );
    }

    @Test
    public void shouldReturnValidationErrorIfFieldTypeEmpty() throws Exception {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setDisplayContext(DisplayContext.MANDATORY);
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(BASE_ORDER_SUMMARY);
        caseFieldEntity.setFieldType(fieldType);
        eventCaseFieldEntity.setCaseField(caseFieldEntity);

        Optional<ValidationError> validationError = classUnderTest.validate(eventCaseFieldEntity);

        assertAll(
            () -> assertThat(validationError.isPresent(), is(true)),
            () -> assertThat(validationError.get(),
                             hasProperty("defaultMessage", equalTo("OrderSummary field type can only be configured with 'READONLY' DisplayContext")))
        );
    }

    @Test
    public void shouldReturnEmptyIfFieldTypeReadonly() throws Exception {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setDisplayContext(DisplayContext.READONLY);
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(BASE_ORDER_SUMMARY);
        caseFieldEntity.setFieldType(fieldType);
        eventCaseFieldEntity.setCaseField(caseFieldEntity);

        Optional<ValidationError> validationError = classUnderTest.validate(eventCaseFieldEntity);

        assertThat(validationError.isPresent(), is(false));
    }
}
