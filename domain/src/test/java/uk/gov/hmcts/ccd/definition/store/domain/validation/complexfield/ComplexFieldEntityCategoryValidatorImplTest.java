package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("Categories can be only set on the Document ComplexField type")
class ComplexFieldEntityCategoryValidatorImplTest {
    public static final String VALID_CATEGORY_ID = "validCategoryId";
    public static final String INVALID_CATEGORY_ID1 = "invalid^CategoryId";
    public static final String INVALID_CATEGORY_ID2 = "##someInvalidCategoryId";

    private FieldTypeEntity fieldTypeEntity;
    private ComplexFieldEntity complexFieldEntity;

    private ComplexFieldEntityCategoryValidatorImpl instance = new ComplexFieldEntityCategoryValidatorImpl();

    @BeforeEach
    public void setUp() {
        fieldTypeEntity = mock(FieldTypeEntity.class);

        given(fieldTypeEntity.getReference()).willReturn("Document");
        given(fieldTypeEntity.isDocumentType()).willReturn(true);

        complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setCategoryId("someCategoryId");
        complexFieldEntity.setFieldType(fieldTypeEntity);
    }

    @DisplayName("Category can be only set for Document complex type")
    @Nested
    class DocumentFieldType {

        @DisplayName("Should not fire ValidationError when Category set on Document type")
        @Test
        public void shouldNotFireValidationErrorWhenCategorySetOnDocumentType() {
            given(fieldTypeEntity.getReference()).willReturn("Document");
            given(fieldTypeEntity.isDocumentType()).willReturn(true);

            ValidationResult validationResult = instance.validate(complexFieldEntity, null);

            assertTrue(validationResult.isValid());
        }

        @DisplayName("Should fire ValidationError when Category set on non Document complex type")
        @Test
        public void shouldFireValidationErrorWhenCategorySetOnNonDocumentType() {
            given(fieldTypeEntity.getReference()).willReturn("Text");
            given(fieldTypeEntity.isDocumentType()).willReturn(false);

            ValidationResult validationResult = instance.validate(complexFieldEntity, null);

            assertFalse(validationResult.isValid());
            assertEquals(1, validationResult.getValidationErrors().size());

            assertTrue(validationResult.getValidationErrors().get(0) instanceof
                ComplexFieldEntityCategoryValidatorImpl.ValidationError);

            assertEquals(
                complexFieldEntity,
                ((ComplexFieldEntityCategoryValidatorImpl.ValidationError) validationResult.getValidationErrors().get(0))
                    .getEntity()
            );
        }
    }

    @DisplayName("CategoryId format")
    @Nested
    class CategoryIdFormat {

        @DisplayName("Should not fire ValidationError for 'validCategoryId' CategoryId")
        @Test
        public void shouldNotFireValidationErrorWhenCategoryIdValidReferencingCategoriesTab() {
            complexFieldEntity.setCategoryId(VALID_CATEGORY_ID);

            ValidationResult validationResult = instance.validate(complexFieldEntity, null);

            assertTrue(validationResult.isValid());
        }

        @DisplayName("Should fire ValidationError for 'invalid^CategoryId' CategoryId")
        @Test
        public void shouldFireValidationErrorWhenCategoryIdInvalid() {
            complexFieldEntity.setCategoryId(INVALID_CATEGORY_ID1);

            ValidationResult validationResult = instance.validate(complexFieldEntity, null);

            assertFalse(validationResult.isValid());
            assertEquals(1, validationResult.getValidationErrors().size());

            assertTrue(validationResult.getValidationErrors().get(0) instanceof
                ComplexFieldEntityCategoryValidatorImpl.ValidationError);

            assertEquals(
                complexFieldEntity,
                ((ComplexFieldEntityCategoryValidatorImpl.ValidationError) validationResult.getValidationErrors().get(0))
                    .getEntity()
            );
        }

        @DisplayName("Should fire ValidationError for '##someInvalidCategoryId' CategoryId")
        @Test
        public void shouldFireValidationErrorWhenCategoryIdInvalid1() {
            complexFieldEntity.setCategoryId(INVALID_CATEGORY_ID2);

            ValidationResult validationResult = instance.validate(complexFieldEntity, null);

            assertFalse(validationResult.isValid());
            assertEquals(1, validationResult.getValidationErrors().size());
        }
    }
}
