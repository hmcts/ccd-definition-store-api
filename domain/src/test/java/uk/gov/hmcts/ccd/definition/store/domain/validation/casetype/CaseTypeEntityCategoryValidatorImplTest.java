package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.category.CategoryEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.category.CategoryEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Categories Validator Tests")
class CaseTypeEntityCategoryValidatorImplTest {

    private CaseTypeEntityCategoryValidatorImpl classUnderTest;

    @Mock
    private CategoryEntity categoryEntity1;
    @Mock
    private CategoryEntity categoryEntity2;

    private CaseTypeEntity caseType;

    @Mock
    private CategoryEntityValidator categoryEntityValidator1;
    @Mock
    private CategoryEntityValidator categoryEntityValidator2;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        caseType = caseTypeWithCategories(Arrays.asList(categoryEntity1, categoryEntity2));

        classUnderTest = new CaseTypeEntityCategoryValidatorImpl(
            Arrays.asList(categoryEntityValidator1, categoryEntityValidator2)
        );
    }

    @Test
    public void validate_allValidatorsCalledWithCategoryEntityValidationContext_EmptyValidationResultReturned() {

        when(categoryEntityValidator1.validate(eq(categoryEntity1), any(CategoryEntityValidationContext.class)))
            .thenReturn(new ValidationResult());
        when(categoryEntityValidator1.validate(eq(categoryEntity2), any(CategoryEntityValidationContext.class)))
            .thenReturn(new ValidationResult());
        when(categoryEntityValidator2.validate(eq(categoryEntity1), any(CategoryEntityValidationContext.class)))
            .thenReturn(new ValidationResult());
        when(categoryEntityValidator2.validate(eq(categoryEntity2), any(CategoryEntityValidationContext.class)))
            .thenReturn(new ValidationResult());

        ValidationResult validationResult = classUnderTest.validate(caseType);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getValidationErrors().isEmpty());
    }

    @Test
    public void caseFieldsAllValid_allValidatorsCalledWithContextBuiltFromCaseType_EmptyValidationResultReturned() {
        ValidationError validationError1 = mock(ValidationError.class);
        ValidationResult validationResult1 = new ValidationResult();
        validationResult1.addError(validationError1);

        ValidationError validationError2 = mock(ValidationError.class);
        ValidationResult validationResult2 = new ValidationResult();
        validationResult2.addError(validationError2);

        ValidationError validationError3 = mock(ValidationError.class);
        ValidationResult validationResult3 = new ValidationResult();
        validationResult3.addError(validationError3);

        when(categoryEntityValidator1.validate(eq(categoryEntity1), any())).thenReturn(new ValidationResult());
        when(categoryEntityValidator1.validate(eq(categoryEntity2), any())).thenReturn(validationResult1);
        when(categoryEntityValidator2.validate(eq(categoryEntity1), any())).thenReturn(validationResult2);
        when(categoryEntityValidator2.validate(eq(categoryEntity2), any())).thenReturn(validationResult3);

        ValidationResult validationResult = classUnderTest.validate(caseType);

        assertFalse(validationResult.isValid());
        assertEquals(3, validationResult.getValidationErrors().size());
        assertTrue(validationResult.getValidationErrors().contains(validationError1));
        assertTrue(validationResult.getValidationErrors().contains(validationError2));
        assertTrue(validationResult.getValidationErrors().contains(validationError3));
    }

    private static CaseTypeEntity caseTypeWithCategories(Collection<CategoryEntity> categories) {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.addCategories(categories);
        caseType.setName("someCaseType");
        return caseType;
    }
}
