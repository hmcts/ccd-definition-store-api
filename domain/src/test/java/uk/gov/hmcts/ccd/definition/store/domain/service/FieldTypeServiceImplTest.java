package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.ccd.definition.store.domain.validation.TestValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidationContextFactory;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidator;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class FieldTypeServiceImplTest {

    public static final JurisdictionEntity JURISDICTION = new JurisdictionEntity();
    private FieldTypeRepository repository;
    private FieldTypeValidator validator1;
    private FieldTypeValidator validator2;
    private FieldTypeServiceImpl fieldTypeService;
    private FieldTypeValidationContextFactory validationContextFactory;

    @Before
    public void setUp() {
        repository = mock(FieldTypeRepository.class);
        doReturn(Optional.of(0)).when(repository).findLastVersion(Mockito.anyString());

        validationContextFactory = mock(FieldTypeValidationContextFactory.class);
        validator1 = mock(FieldTypeValidator.class);
        doReturn(new ValidationResult()).when(validator1).validate(any(), any());
        validator2 = mock(FieldTypeValidator.class);
        doReturn(new ValidationResult()).when(validator2).validate(any(), any());

        final List<FieldTypeValidator> validators = Arrays.asList(validator1, validator2);

        fieldTypeService = new FieldTypeServiceImpl(repository, validationContextFactory, validators);
    }

    @Test
    public void saveTypes_shouldValidateWithAllValidators() throws Exception {

        final FieldTypeValidationContext context = mock(FieldTypeValidationContext.class);
        doReturn(context).when(validationContextFactory).create();

        final FieldTypeEntity type1 = new FieldTypeEntity();
        final FieldTypeEntity type2 = new FieldTypeEntity();

        fieldTypeService.saveTypes(JURISDICTION, Arrays.asList(type1, type2));

        verify(validator1).validate(context, type1);
        verify(validator1).validate(context, type2);
        verify(validator2).validate(context, type1);
        verify(validator2).validate(context, type2);
    }

    @Test
    public void saveTypes_shouldThrowExceptionOnValidationError() throws Exception {

        final FieldTypeValidationContext context = mock(FieldTypeValidationContext.class);
        doReturn(context).when(validationContextFactory).create();

        final FieldTypeEntity type1 = new FieldTypeEntity();
        final FieldTypeEntity type2 = new FieldTypeEntity();

        doReturn(validationResultWithError(validationErrorWithDefaultMessage("Invalid type")))
            .when(validator1).validate(context, type1);

        try {
            fieldTypeService.saveTypes(JURISDICTION, Arrays.asList(type1, type2));
            fail("No validation exception thrown");
        } catch (ValidationException ex) {
            verify(validator1).validate(context, type1);
            verify(validator1).validate(context, type2);
            verify(validator2).validate(context, type1);
            verify(validator2).validate(context, type2);

            assertEquals(1, ex.getValidationResult().getValidationErrors().size());
            assertEquals("Invalid type", ex.getValidationResult().getValidationErrors().get(0).getDefaultMessage());
        }
    }

    @Test
    public void save_shouldValidateWithAllValidators() throws Exception {

        final FieldTypeValidationContext context = mock(FieldTypeValidationContext.class);
        doReturn(context).when(validationContextFactory).create();

        final FieldTypeEntity type = new FieldTypeEntity();

        fieldTypeService.save(type);

        verify(validator1).validate(context, type);
        verify(validator2).validate(context, type);

        verify(repository).save(type);
    }

    @Test
    public void save_shouldThrowExceptionOnValidationError() throws Exception {

        final FieldTypeValidationContext context = mock(FieldTypeValidationContext.class);
        doReturn(context).when(validationContextFactory).create();

        final FieldTypeEntity type = new FieldTypeEntity();

        doReturn(validationResultWithError(validationErrorWithDefaultMessage("Invalid type")))
            .when(validator1).validate(context, type);

        try {
            fieldTypeService.save(type);
            fail("No validation exception thrown");
        } catch (ValidationException ex) {
            verify(validator1).validate(context, type);
            verify(validator2).validate(context, type);

            assertEquals(1, ex.getValidationResult().getValidationErrors().size());
            assertEquals("Invalid type", ex.getValidationResult().getValidationErrors().get(0).getDefaultMessage());
        }

        verifyZeroInteractions(repository);
    }

    private ValidationResult validationResultWithError(ValidationError validationError) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.addError(validationError);
        return validationResult;
    }

    private ValidationError validationErrorWithDefaultMessage(String defaultMessage) {
        return new TestValidationError(defaultMessage);
    }

}
