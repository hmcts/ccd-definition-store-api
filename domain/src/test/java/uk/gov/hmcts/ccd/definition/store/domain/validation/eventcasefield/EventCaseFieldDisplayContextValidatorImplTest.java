package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

public class EventCaseFieldDisplayContextValidatorImplTest {

    @Mock
    private FieldTypeToDisplayContextValidator entityToDefinitionDataItemRegistry;

    @InjectMocks
    private EventCaseFieldDisplayContextValidatorImpl classUnderTest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new EventCaseFieldDisplayContextValidatorImpl(Lists.newArrayList(entityToDefinitionDataItemRegistry));
        when(entityToDefinitionDataItemRegistry.validate(anyObject())).thenReturn(Optional.empty());
    }

    @Test
    public void CaseFieldEntityDisplayContextMustHaveValueValidationErrorFiresWhenDisplayContextDoesNotExist() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntity();

        ValidationResult validationResult
            = classUnderTest.validate(eventCaseFieldEntity, null);

        assertAll(
            () -> assertFalse(validationResult.isValid()),
            () -> assertEquals(1, validationResult.getValidationErrors().size()),
            () -> assertTrue(validationResult.getValidationErrors().get(0) instanceof
                                 EventCaseFieldDisplayContextValidatorImpl.ValidationError),
            () -> assertEquals(
                eventCaseFieldEntity,
                ((EventCaseFieldDisplayContextValidatorImpl.ValidationError) validationResult.getValidationErrors().get(0))
                    .getEntity()
            ),
            () -> assertThat(validationResult.getValidationErrors().get(0),
                             hasProperty("defaultMessage", equalTo("Couldn't find the column DisplayContext or " +
                                                                       "incorrect value specified for DisplayContext. Allowed values are 'READONLY','MANDATORY' or 'OPTIONAL'")))
        );
    }

    @Test
    public void CaseFieldEntityDisplayContextMustHaveValueValidationErrorDoesNotFireWhenDisplayContextExists() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntity();
        eventCaseFieldEntity.setDisplayContext(DisplayContext.OPTIONAL);

        ValidationResult validationResult
            = classUnderTest.validate(eventCaseFieldEntity, null);

        assertAll(
            () -> assertTrue(validationResult.isValid()),
            () -> assertEquals(0, validationResult.getValidationErrors().size())
        );
    }

    @Test
    public void CaseFieldEntityFieldTypeToDisplayContextDoesNotFireWhenFieldTypeToDisplayContextValidationPasses() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntity();
        eventCaseFieldEntity.setDisplayContext(aDisplayContext());

        ValidationResult validationResult
            = classUnderTest.validate(eventCaseFieldEntity, null);

        assertAll(
            () -> assertTrue(validationResult.isValid()),
            () -> assertEquals(0, validationResult.getValidationErrors().size())
        );
    }

    @Test
    public void CaseFieldEntityFieldTypeToDisplayContextFiresWhenFieldTypeToDisplayContextValidationFails() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntity();
        eventCaseFieldEntity.setDisplayContext(aDisplayContext());
        when(entityToDefinitionDataItemRegistry.validate(anyObject())).thenReturn(getErrorOpt(eventCaseFieldEntity));

        ValidationResult validationResult
            = classUnderTest.validate(eventCaseFieldEntity, null);

        assertAll(
            () -> assertFalse(validationResult.isValid()),
            () -> assertEquals(1, validationResult.getValidationErrors().size()),
            () -> assertTrue(validationResult.getValidationErrors().get(0) instanceof
                                 EventCaseFieldDisplayContextValidatorImpl.ValidationError),
            () -> assertEquals(
                eventCaseFieldEntity,
                ((EventCaseFieldDisplayContextValidatorImpl.ValidationError) validationResult.getValidationErrors().get(0))
                    .getEntity()),
            () -> assertThat(validationResult.getValidationErrors().get(0),
                             hasProperty("defaultMessage", equalTo("Error")))
        );
    }

    private DisplayContext aDisplayContext() {
        return DisplayContext.READONLY;
    }

    private Optional<ValidationError> getErrorOpt(EventCaseFieldEntity eventCaseFieldEntity) {
        return Optional.of(new EventCaseFieldDisplayContextValidatorImpl.ValidationError(
            "Error",
            eventCaseFieldEntity));
    }

    private EventCaseFieldEntity eventCaseFieldEntity() {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference("Event1");
        eventCaseFieldEntity.setEvent(eventEntity);
        return eventCaseFieldEntity;
    }
}
