package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.regex.RegularExpressionValidationError;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DOCUMENT;

class DocumentTypeRegularExpressionValidatorTest {

    @Mock
    private FieldTypeValidationContext context;

    private final DocumentTypeRegularExpressionValidator validator = new DocumentTypeRegularExpressionValidator();

    private final FieldTypeEntity fieldType = new FieldTypeEntity();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        fieldType.setReference(BASE_DOCUMENT);
    }

    @DisplayName("Document field type regex")
    @Nested
    class DocumentFieldType {

        @DisplayName("should pass validation when the regex matches the expected pattern")
        @Test
        void shouldPassValidation() {
            fieldType.setRegularExpression(".pdf, .docx,  .xlsx");

            ValidationResult result = validator.validate(context, fieldType);

            assertThat(result.getValidationErrors(), hasSize(0));
        }

        @DisplayName("should pass validation when the regex is null")
        @Test
        void shouldPassValidationWhenRegexIsNull() {
            fieldType.setRegularExpression(null);

            ValidationResult result = validator.validate(context, fieldType);

            assertThat(result.getValidationErrors(), hasSize(0));
        }

        @DisplayName("should fail validation when the regex does not match the expected pattern")
        @Test
        void shouldFailValidation() {
            fieldType.setRegularExpression(".pdf,doc,xls");

            ValidationResult result = validator.validate(context, fieldType);

            assertThat(result.getValidationErrors(), hasSize(1));
            assertThat(result.getValidationErrors().get(0), instanceOf(RegularExpressionValidationError.class));
        }

        @DisplayName("should fail validation when the regex includes wildcard names")
        @Test
        void shouldFailValidationForRegexWithWildcardNames() {
            fieldType.setRegularExpression("*.pdf, *.doc");

            ValidationResult result = validator.validate(context, fieldType);

            assertThat(result.getValidationErrors(), hasSize(1));
            assertThat(result.getValidationErrors().get(0), instanceOf(RegularExpressionValidationError.class));
        }

    }

}
