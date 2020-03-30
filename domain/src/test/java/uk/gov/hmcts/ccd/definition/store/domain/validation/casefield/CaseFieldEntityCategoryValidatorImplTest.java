package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("Categories can be only set on the Document CaseField type")
class CaseFieldEntityCategoryValidatorImplTest {

    private FieldTypeEntity fieldTypeEntity;
    private CaseFieldEntity caseFieldEntity;

    private CaseFieldEntityCategoryValidatorImpl instance = new CaseFieldEntityCategoryValidatorImpl();

    @BeforeEach
    public void setUp() {
        fieldTypeEntity = mock(FieldTypeEntity.class);

        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        given(caseTypeEntity.getReference()).willReturn("someCaseType");

        caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setCategoryId("someCategoryId");
        caseFieldEntity.setCaseType(caseTypeEntity);
        caseFieldEntity.setFieldType(fieldTypeEntity);
    }

    @DisplayName("Should not fire ValidationError when Category set on Document type")
    @Test
    public void shouldNotFireValidationErrorWhenCategorySetOnDocumentType() {
        given(fieldTypeEntity.getReference()).willReturn("Document");
        given(fieldTypeEntity.isDocumentType()).willReturn(true);

        ValidationResult validationResult = instance.validate(caseFieldEntity, null);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    @DisplayName("Should fire ValidationError when Category set on non Document type")
    @Test
    public void shouldFireValidationErrorWhenCategorySetOnNonDocumentType() {
        given(fieldTypeEntity.getReference()).willReturn("Text");
        given(fieldTypeEntity.isDocumentType()).willReturn(false);

        ValidationResult validationResult = instance.validate(caseFieldEntity, null);

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());

        assertTrue(validationResult.getValidationErrors().get(0) instanceof
            CaseFieldEntityCategoryValidatorImpl.ValidationError);

        assertEquals(
            caseFieldEntity,
            ((CaseFieldEntityCategoryValidatorImpl.ValidationError) validationResult.getValidationErrors().get(0))
                .getEntity()
        );
    }
}
