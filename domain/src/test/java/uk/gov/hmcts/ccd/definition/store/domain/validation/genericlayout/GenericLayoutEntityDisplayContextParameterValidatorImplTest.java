package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.InvalidDateTimeFormatException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@DisplayName("Generic Layout Entity Display Context Parameter Validator Implementation Tests")
public class GenericLayoutEntityDisplayContextParameterValidatorImplTest {

    private GenericLayoutValidator validator;

    @Mock
    private DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory;

    @Mock
    private DisplayContextParameterValidator displayContextParameterValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new GenericLayoutEntityDisplayContextParameterValidatorImpl(displayContextParameterValidatorFactory);
        when(displayContextParameterValidatorFactory.getValidator(Mockito.any())).thenReturn(displayContextParameterValidator);
    }

    @ParameterizedTest
    @ArgumentsSource(EntityArgumentsProvider.class)
    void shouldValidateEntityWithNoDisplayContextParameter(GenericLayoutEntity entity) {
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity, Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @ParameterizedTest
    @ArgumentsSource(ResultEntityArgumentsProvider.class)
    void shouldValidateResultEntityWithDateTimeDisplayDisplayContextParameter(GenericLayoutEntity entity) {
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(HHmmss)");
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity,  Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @ParameterizedTest
    @ArgumentsSource(InputEntityArgumentsProvider.class)
    void shouldValidateInputEntityWithDateTimeEntryDisplayContextParameter(GenericLayoutEntity entity) {
        entity.setDisplayContextParameter("#DATETIMEENTRY(HHmmss)");
        entity.setCaseField(caseFieldEntity());
        final ValidationResult result = validator.validate(entity,  Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @ParameterizedTest
    @ArgumentsSource(ResultEntityArgumentsProvider.class)
    void shouldValidateEntityWithDisplayContextParameterForDateFieldType(GenericLayoutEntity entity) {
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(HHmmss)");
        entity.setCaseField(caseFieldEntity(fieldTypeEntity(FieldTypeUtils.BASE_DATE)));

        final ValidationResult result = validator.validate(entity,  Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @ParameterizedTest
    @ArgumentsSource(InputEntityArgumentsProvider.class)
    void shouldFailValidationForDisplayParameterOnInputTab(GenericLayoutEntity entity, String tab) throws Exception {
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(HHmmss)");
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity,  Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Unsupported display context parameter type '#DATETIMEDISPLAY(HHmmss)' for field 'CASE_FIELD' on tab '"  + tab + "'"))
        );
    }

    @ParameterizedTest
    @ArgumentsSource(ResultEntityArgumentsProvider.class)
    void shouldFailValidationForEntryParameterOnResultTab(GenericLayoutEntity entity, String tab) throws Exception {
        entity.setDisplayContextParameter("#DATETIMEENTRY(HHmmss)");
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity,  Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Unsupported display context parameter type '#DATETIMEENTRY(HHmmss)' for field 'CASE_FIELD' on tab '"  + tab + "'"))
        );
    }

    @ParameterizedTest
    @ArgumentsSource(InputEntityArgumentsProvider.class)
    void shouldFailValidationForInvalidDateTimeFormat(GenericLayoutEntity entity, String tab) throws Exception {
        entity.setDisplayContextParameter("#DATETIMEENTRY(0123456789)");
        entity.setCaseField(caseFieldEntity());
        doThrow(InvalidDateTimeFormatException.class).when(displayContextParameterValidator).validate(Mockito.any(), Mockito.any());

        final ValidationResult result = validator.validate(entity,  Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Display context parameter '#DATETIMEENTRY(0123456789)' has been incorrectly configured or is invalid for field 'CASE_FIELD' on tab '"  + tab + "'"))
        );
    }

    @ParameterizedTest
    @ArgumentsSource(EntityArgumentsProvider.class)
    void shouldFailValidationForInvalidDisplayContextParameterType(GenericLayoutEntity entity, String tab) {
        entity.setDisplayContextParameter("#INVALIDPARAMETER(hhmmss)");
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity,  Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Display context parameter '#INVALIDPARAMETER(hhmmss)' has been incorrectly configured or is invalid for field 'CASE_FIELD' on tab '" + tab + "'"))
        );
    }

    @ParameterizedTest
    @ArgumentsSource(EntityArgumentsProvider.class)
    void shouldFailValidationForNotAllowedDisplayContextParameterType(GenericLayoutEntity entity, String tab) {
        entity.setDisplayContextParameter("#TABLE(FieldId)");
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity,  Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Unsupported display context parameter type '#TABLE(FieldId)' for field 'CASE_FIELD' on tab '"  + tab + "'"))
        );
    }

    @ParameterizedTest
    @ArgumentsSource(EntityArgumentsProvider.class)
    void shouldFailValidationForNotAllowedFieldType(GenericLayoutEntity entity, String tab) {
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss)");
        entity.setCaseField(caseFieldEntity(fieldTypeEntity(FieldTypeUtils.BASE_TEXT)));

        final ValidationResult result = validator.validate(entity,  Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Display context parameter '#DATETIMEDISPLAY(hhmmss)' is unsupported for field type 'Text' of field 'CASE_FIELD' on tab '" + tab + "'"))
        );
    }

    static class EntityArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(new SearchInputCaseFieldEntity(), "SearchInputFields"),
                Arguments.of(new SearchResultCaseFieldEntity(), "SearchResultFields"),
                Arguments.of(new WorkBasketInputCaseFieldEntity(), "WorkBasketInputFields"),
                Arguments.of(new WorkBasketCaseFieldEntity(), "WorkBasketResultFields")
            );
        }
    }

    static class InputEntityArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(new SearchInputCaseFieldEntity(), "SearchInputFields"),
                Arguments.of(new WorkBasketInputCaseFieldEntity(), "WorkBasketInputFields")
            );
        }
    }

    static class ResultEntityArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(new SearchResultCaseFieldEntity(), "SearchResultFields"),
                Arguments.of(new WorkBasketCaseFieldEntity(), "WorkBasketResultFields")
            );
        }
    }

    private static CaseFieldEntity caseFieldEntity() {
        return caseFieldEntity(fieldTypeEntity(FieldTypeUtils.BASE_DATE_TIME));
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
