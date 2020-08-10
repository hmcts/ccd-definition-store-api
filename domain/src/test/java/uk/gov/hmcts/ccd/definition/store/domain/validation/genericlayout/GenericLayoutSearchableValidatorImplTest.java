package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.LayoutSheetType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

class GenericLayoutSearchableValidatorImplTest {

    private GenericLayoutValidator validator;

    @Mock
    private GenericLayoutEntity entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new GenericLayoutSearchableValidatorImpl();
    }

    @Test
    void shouldValidateSearchableInputField() {
        prepareEntity(LayoutSheetType.INPUT, true);

        ValidationResult result = validator.validate(entity, Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateNonSearchableResultField() {
        prepareEntity(LayoutSheetType.RESULT, false);

        ValidationResult result = validator.validate(entity, Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldReturnValidationErrorWhenInputFieldIsNotSearchable() {
        prepareEntity(LayoutSheetType.INPUT, false);

        ValidationResult result = validator.validate(entity, Collections.emptyList());

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Layout sheet 'SheetName' contains a non-searchable field 'FieldPath'."))
        );
    }

    private void prepareEntity(LayoutSheetType layoutSheetType, boolean searchable) {
        when(entity.getLayoutSheetType()).thenReturn(layoutSheetType);
        when(entity.isSearchable()).thenReturn(searchable);
        when(entity.buildFieldPath()).thenReturn("FieldPath");
        when(entity.getSheetName()).thenReturn("SheetName");
    }
}
