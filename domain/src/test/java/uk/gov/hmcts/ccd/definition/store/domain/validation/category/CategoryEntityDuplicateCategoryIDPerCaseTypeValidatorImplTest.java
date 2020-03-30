package uk.gov.hmcts.ccd.definition.store.domain.validation.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("Validate categories for duplicate CategoryId in the same CaseType")
class CategoryEntityDuplicateCategoryIDPerCaseTypeValidatorImplTest {

    public static final String CATEGORY_ID1 = "someCategoryId1";
    public static final String CATEGORY_ID2 = "someCategoryId2";
    public static final String CATEGORY_ID3 = "someCategoryId3";

    private CategoryEntityValidationContext categoryEntityValidationContext;

    private CategoryEntityDuplicateCategoryIDPerCaseTypeValidatorImpl instance =
        new CategoryEntityDuplicateCategoryIDPerCaseTypeValidatorImpl();

    @BeforeEach
    public void setUp() {
        categoryEntityValidationContext = mock(CategoryEntityValidationContext.class);
    }

    @DisplayName("Should not fire ValidationError when no duplicates found for CategoryId")
    @Test
    public void shouldNotFireValidationErrorWhenNoDuplicatesFound() {
        given(categoryEntityValidationContext.getCaseReference()).willReturn("caseRef");
        given(categoryEntityValidationContext.getCategoryEntities())
            .willReturn(asList(createCategoryEntity(CATEGORY_ID2), createCategoryEntity(CATEGORY_ID3)));

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryId(CATEGORY_ID1);

        ValidationResult validationResult = instance.validate(categoryEntity, categoryEntityValidationContext);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    @DisplayName("Should fire ValidationError when duplicates found for CategoryId")
    @Test
    public void shouldFireValidationErrorWhenDuplicatesFound() {
        given(categoryEntityValidationContext.getCaseReference()).willReturn("caseRef");
        given(categoryEntityValidationContext.getCategoryEntities())
            .willReturn(asList(createCategoryEntity(CATEGORY_ID1), createCategoryEntity(CATEGORY_ID2)));

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryId(CATEGORY_ID1);

        ValidationResult validationResult = instance.validate(categoryEntity, categoryEntityValidationContext);

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());

        assertTrue(validationResult.getValidationErrors().get(0) instanceof
            CategoryEntityDuplicateCategoryIDPerCaseTypeValidatorImpl.ValidationError);

        assertEquals(
            categoryEntity,
            ((CategoryEntityDuplicateCategoryIDPerCaseTypeValidatorImpl.ValidationError) validationResult
                .getValidationErrors().get(0)).getEntity()
        );
    }

    private static CategoryEntity createCategoryEntity(String categoryId) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryId(categoryId);
        return categoryEntity;
    }
}
