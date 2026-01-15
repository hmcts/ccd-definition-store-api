package uk.gov.hmcts.ccd.definition.store.domain.service;


import uk.gov.hmcts.ccd.definition.store.domain.validation.TestValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidationContextFactory;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidator;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class FieldTypeServiceImplTest {

    public static final JurisdictionEntity JURISDICTION = new JurisdictionEntity();
    private FieldTypeRepository repository;
    private FieldTypeValidator validator1;
    private FieldTypeValidator validator2;
    private FieldTypeServiceImpl fieldTypeService;
    private FieldTypeValidationContextFactory validationContextFactory;

    @BeforeEach
    void setUp() {
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
    void saveTypes_shouldValidateWithAllValidators() throws Exception {

        final FieldTypeValidationContext context = mock(FieldTypeValidationContext.class);
        doReturn(context).when(validationContextFactory).create();

        FieldTypeEntity type1 = new FieldTypeEntity();
        type1.setReference("type1");
        FieldTypeEntity type2 = new FieldTypeEntity();
        type2.setReference("type2");

        fieldTypeService.saveTypes(JURISDICTION, Arrays.asList(type1, type2));

        verify(validator1).validate(context, type1);
        verify(validator1).validate(context, type2);
        verify(validator2).validate(context, type1);
        verify(validator2).validate(context, type2);
    }

    @Test
    void saveTypes_shouldThrowExceptionOnValidationError() throws Exception {

        final FieldTypeValidationContext context = mock(FieldTypeValidationContext.class);
        doReturn(context).when(validationContextFactory).create();

        FieldTypeEntity type1 = new FieldTypeEntity();
        type1.setReference("type1");
        FieldTypeEntity type2 = new FieldTypeEntity();
        type2.setReference("type2");

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
    void save_shouldValidateWithAllValidators() throws Exception {

        final FieldTypeValidationContext context = mock(FieldTypeValidationContext.class);
        doReturn(context).when(validationContextFactory).create();

        final FieldTypeEntity type = new FieldTypeEntity();

        fieldTypeService.save(type);

        verify(validator1).validate(context, type);
        verify(validator2).validate(context, type);

        verify(repository).save(type);
    }

    @Test
    void save_shouldThrowExceptionOnValidationError() throws Exception {

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

        verifyNoMoreInteractions(repository);
    }

    @Test
    void saveTypes_shouldDeduplicateAndMergeFieldTypesWithSameReference() {
        final FieldTypeValidationContext context = mock(FieldTypeValidationContext.class);
        doReturn(context).when(validationContextFactory).create();

        FieldTypeEntity type1 = new FieldTypeEntity();
        type1.setReference("dup");
        type1.setRegularExpression("regex-1");
        type1.setMinimum("1");
        type1.setMaximum(null);
        type1.addListItems(Arrays.asList(listItem("A", "Label A", 1)));
        type1.addComplexFields(Arrays.asList(complexField("cf1")));

        FieldTypeEntity type2 = new FieldTypeEntity();
        type2.setReference("dup");
        type2.setRegularExpression("regex-2");
        type2.setMinimum(null);
        type2.setMaximum("9");
        type2.addListItems(Arrays.asList(listItem("B", "Label B", 2)));
        type2.addComplexFields(Arrays.asList(complexField("cf2")));

        ArgumentCaptor<Iterable<FieldTypeEntity>> captor = ArgumentCaptor.forClass(Iterable.class);

        fieldTypeService.saveTypes(JURISDICTION, Arrays.asList(type1, type2));

        verify(repository).saveAll(captor.capture());
        List<FieldTypeEntity> saved = toList(captor.getValue());
        assertEquals(1, saved.size());

        FieldTypeEntity merged = saved.get(0);
        assertSame(JURISDICTION, merged.getJurisdiction());
        assertEquals("regex-1", merged.getRegularExpression());
        assertEquals("1", merged.getMinimum());
        assertEquals("9", merged.getMaximum());
        assertEquals(2, merged.getListItems().size());
        assertEquals(2, merged.getComplexFields().size());
    }

    @Test
    void saveTypes_shouldKeepExistingBaseTypeOnConflict() {
        final FieldTypeValidationContext context = mock(FieldTypeValidationContext.class);
        doReturn(context).when(validationContextFactory).create();

        FieldTypeEntity base1 = new FieldTypeEntity();
        base1.setReference("Base1");
        FieldTypeEntity base2 = new FieldTypeEntity();
        base2.setReference("Base2");

        FieldTypeEntity type1 = new FieldTypeEntity();
        type1.setReference("dup");
        type1.setBaseFieldType(base1);

        FieldTypeEntity type2 = new FieldTypeEntity();
        type2.setReference("dup");
        type2.setBaseFieldType(base2);

        ArgumentCaptor<Iterable<FieldTypeEntity>> captor = ArgumentCaptor.forClass(Iterable.class);

        fieldTypeService.saveTypes(JURISDICTION, Arrays.asList(type1, type2));

        verify(repository).saveAll(captor.capture());
        List<FieldTypeEntity> saved = toList(captor.getValue());
        assertEquals(1, saved.size());
        assertSame(base1, saved.get(0).getBaseFieldType());
    }

    private ValidationResult validationResultWithError(ValidationError validationError) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.addError(validationError);
        return validationResult;
    }

    private ValidationError validationErrorWithDefaultMessage(String defaultMessage) {
        return new TestValidationError(defaultMessage);
    }

    private FieldTypeListItemEntity listItem(String value, String label, Integer order) {
        FieldTypeListItemEntity item = new FieldTypeListItemEntity();
        item.setValue(value);
        item.setLabel(label);
        item.setOrder(order);
        return item;
    }

    private ComplexFieldEntity complexField(String reference) {
        ComplexFieldEntity field = new ComplexFieldEntity();
        field.setReference(reference);
        field.setLabel(reference + "-label");
        field.setOrder(1);
        return field;
    }

    private List<FieldTypeEntity> toList(Iterable<FieldTypeEntity> iterable) {
        List<FieldTypeEntity> list = new ArrayList<>();
        for (FieldTypeEntity entity : iterable) {
            list.add(entity);
        }
        return list;
    }

}
