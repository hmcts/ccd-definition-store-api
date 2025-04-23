package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

public class EventCaseFieldArgumentDisplayContextParameterValidatorImplTest {

    private EventCaseFieldEntityValidator validator;

    @Mock
    private DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory;

    @Mock
    private DisplayContextParameterValidator displayContextParameterValidator;

    @Mock
    private EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new EventCaseFieldArgumentDisplayContextParameterValidatorImpl(
            displayContextParameterValidatorFactory);
        when(displayContextParameterValidatorFactory.getValidator(Mockito.any()))
            .thenReturn(displayContextParameterValidator);
    }

    @Test
    void shouldValidateEntityWithNoDisplayContextParameter() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithArgumentDisplayContextParameter() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setDisplayContextParameter("#ARGUMENT(TestArgument)");
        entity.setCaseField(caseFieldEntity());
        entity.setDisplayContext(DisplayContext.OPTIONAL);

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithDateTimeDisplayDisplayContextParameter() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss)");
        entity.setCaseField(caseFieldEntity());
        entity.setDisplayContext(DisplayContext.READONLY);

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithSheetName() {
        EventCaseFieldArgumentDisplayContextParameterValidatorImpl eventCase
            = new EventCaseFieldArgumentDisplayContextParameterValidatorImpl(displayContextParameterValidatorFactory);

        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        assertAll(
            () -> assertThat(eventCase.getSheetName(entity), is("CaseEventToFields"))
        );
    }

    @Test
    void shouldValidateEntityWithCaseReference() {
        EventCaseFieldArgumentDisplayContextParameterValidatorImpl eventCase
            = new EventCaseFieldArgumentDisplayContextParameterValidatorImpl(displayContextParameterValidatorFactory);
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        assertAll(
            () -> assertThat(eventCase.getCaseFieldReference(entity), is("CASE_FIELD"))
        );
    }

    private static CaseFieldEntity caseFieldEntity() {
        return caseFieldEntity(fieldTypeEntity(FieldTypeUtils.BASE_TEXT));
    }

    private static CaseFieldEntity caseFieldEntity(FieldTypeEntity fieldTypeEntity) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference("CASE_FIELD");
        caseFieldEntity.setFieldType(fieldTypeEntity);
        return caseFieldEntity;
    }

    private static FieldTypeEntity fieldTypeEntity(String fieldType) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(fieldType);
        return fieldTypeEntity;
    }
}
