package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

public class DisplayGroupArgumentDisplayContextParameterValidatorImplTest {

    private DisplayGroupCaseFieldValidator validator;
    private static final String TEST_ARGUMENT = "#ARGUMENT(TestArgument)";

    @Mock
    private DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory;

    @Mock
    private DisplayContextParameterValidator displayContextParameterValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new DisplayGroupDateTimeDisplayContextParameterValidatorImpl(
            displayContextParameterValidatorFactory);
        when(displayContextParameterValidatorFactory.getValidator(Mockito.any()))
            .thenReturn(displayContextParameterValidator);
    }

    @Test
    void shouldValidateEntityWithNoDisplayContextParameter() {
        DisplayGroupArgumentDisplayContextParameterValidatorImpl argumentDisplayContextParameterValidator
            = new DisplayGroupArgumentDisplayContextParameterValidatorImpl(displayContextParameterValidatorFactory);

        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = argumentDisplayContextParameterValidator.validate(entity);

        assertAll(
            () -> MatcherAssert.assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithArgumentDisplayContextParameter() {
        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setDisplayContextParameter(TEST_ARGUMENT);
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity);

        assertAll(
            () -> MatcherAssert.assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldReturnSheetName() {
        DisplayGroupArgumentDisplayContextParameterValidatorImpl argumentDisplayContextParameterValidator
            = new DisplayGroupArgumentDisplayContextParameterValidatorImpl(displayContextParameterValidatorFactory);

        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        assertAll(
            () -> MatcherAssert.assertThat(argumentDisplayContextParameterValidator.getSheetName(entity),
                is("CaseTypeTab"))
        );
    }

    @Test
    void shouldReturnCaseReference() {
        DisplayGroupArgumentDisplayContextParameterValidatorImpl argumentDisplayContextParameterValidator
            = new DisplayGroupArgumentDisplayContextParameterValidatorImpl(displayContextParameterValidatorFactory);

        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        assertAll(
            () -> MatcherAssert.assertThat(argumentDisplayContextParameterValidator.getCaseFieldReference(entity),
                is("CASE_FIELD"))
        );
    }

    @Test
    void shouldReturnFieldType() {
        DisplayGroupArgumentDisplayContextParameterValidatorImpl argumentDisplayContextParameterValidator
            = new DisplayGroupArgumentDisplayContextParameterValidatorImpl(displayContextParameterValidatorFactory);

        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        assertAll(
            () -> MatcherAssert.assertThat(argumentDisplayContextParameterValidator.getFieldTypeEntity(entity)
                .getReference(), is(FieldTypeUtils.BASE_TEXT))
        );
    }

    @Test
    void shouldReturnDisplayContextParameter() {
        DisplayGroupArgumentDisplayContextParameterValidatorImpl argumentDisplayContextParameterValidator
            = new DisplayGroupArgumentDisplayContextParameterValidatorImpl(displayContextParameterValidatorFactory);

        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setDisplayContextParameter(TEST_ARGUMENT);
        entity.setCaseField(caseFieldEntity());

        assertAll(
            () -> MatcherAssert.assertThat(argumentDisplayContextParameterValidator.getDisplayContextParameter(entity),
                is(TEST_ARGUMENT))
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

