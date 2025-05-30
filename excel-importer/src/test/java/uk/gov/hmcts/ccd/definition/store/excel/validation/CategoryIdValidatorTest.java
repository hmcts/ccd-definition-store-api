package uk.gov.hmcts.ccd.definition.store.excel.validation;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class CategoryIdValidatorTest {

    private static final String COLLECTION_OF_DOCUMENT = "CollectionOfDocument";
    private static final String CASE_TYPE = "FT_MasterCaseType";
    private static final String COLLECTION = "Collection";
    private static final String DIVORCE_DOCS = "divorceDocs";
    private static final String DOCUMENT = "Document";
    private static final String DIVORCE_DOCUMENT = "divorceDocument";
    private static final String TEXT_STRING = "TEXT";
    private static final String UUID_STRING = "e7e67248-9c0e-11ed-a8fc-0242ac120002";

    private ParseContext parseContext;
    private CategoryIdValidator categoryValidator;
    private CategoryEntity divorceDocsCategoryEntity = new CategoryEntity();

    @BeforeEach
    void setUp() {
        categoryValidator = new CategoryIdValidator();
        divorceDocsCategoryEntity.setCategoryId(DIVORCE_DOCS);
    }

    @Nested
    @DisplayName("Case Field")
    class CaseField {

        private ParseContext buildParseContextCaseTypeEntity(String reference,
                                                             String collectionType,
                                                             String category) {
            return buildParseContextCaseTypeEntityWithBaseFieldType(reference, collectionType, category, null);
        }

        private ParseContext buildParseContextCaseTypeEntityWithBaseFieldType(String reference,
                                                                              String collectionType,
                                                                              String category,
                                                                              String baseFieldTypeReference) {
            val parseContext = new ParseContext();
            val caseTypeEntity = new CaseTypeEntity();

            caseTypeEntity.setReference(CASE_TYPE);
            caseTypeEntity.addCaseField(buildCaseTypeDocumentReferenceWithBaseFieldType(reference, collectionType,
                category, baseFieldTypeReference));

            parseContext.registerCaseType(caseTypeEntity);
            return spy(parseContext);
        }

        private CaseFieldEntity buildCaseTypeDocumentReferenceWithBaseFieldType(String reference,
                                                                                String collectionType,
                                                                                String category,
                                                                                String baseFieldTypeReference) {
            CaseFieldEntity caseField = new CaseFieldEntity();
            FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
            if (!reference.equals(COLLECTION)) {
                fieldTypeEntity.setReference(reference);
                FieldTypeEntity baseFieldTypeEntity = new FieldTypeEntity();
                baseFieldTypeEntity.setReference(baseFieldTypeReference);
                fieldTypeEntity.setBaseFieldType(baseFieldTypeEntity);
            } else {
                FieldTypeEntity baseFieldTypeEntity = new FieldTypeEntity();
                FieldTypeEntity collectionFieldTypeEntity = new FieldTypeEntity();
                fieldTypeEntity.setReference(COLLECTION_OF_DOCUMENT);
                baseFieldTypeEntity.setReference(reference);
                collectionFieldTypeEntity.setReference(collectionType);
                fieldTypeEntity.setBaseFieldType(baseFieldTypeEntity);
                fieldTypeEntity.setCollectionFieldType(collectionFieldTypeEntity);
            }
            caseField.setFieldType(fieldTypeEntity);
            caseField.setCategoryId(category);
            return caseField;
        }

        @Test
        void testValidateCaseFieldCategoryNull() {
            parseContext = buildParseContextCaseTypeEntity(TEXT_STRING, null, null);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);
            categoryValidator.validate(parseContext);
            verify(parseContext, never()).getCategory(anyString(), anyString());
        }

        @Test
        void testValidateCaseFieldFieldTypeDocument() {
            parseContext = buildParseContextCaseTypeEntity(DOCUMENT, null, DIVORCE_DOCS);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);
            categoryValidator.validate(parseContext);
            verify(parseContext).getCategory(CASE_TYPE, DIVORCE_DOCS);
        }

        @Test
        void testValidateCaseFieldFieldTypeCollectionOfDocument() {
            parseContext = buildParseContextCaseTypeEntity(COLLECTION, DOCUMENT, DIVORCE_DOCS);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);
            categoryValidator.validate(parseContext);
            verify(parseContext).getCategory(CASE_TYPE, DIVORCE_DOCS);
        }

        @Test
        void testValidateCaseFieldFieldTypeDocumentInvalidCategoryThrowException() {
            parseContext = buildParseContextCaseTypeEntity(DOCUMENT, null, DIVORCE_DOCUMENT);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);

            final InvalidImportException invalidImportException =
                    assertThrows(InvalidImportException.class, () -> categoryValidator.validate(parseContext));

            assertEquals(
                    "CaseFieldTab Invalid value 'divorceDocument' is not a valid CategoryID value."
                            + " Category cannot be found.",
                    invalidImportException.getMessage());
        }

        @Test
        void testValidateCaseFieldFieldTypeCollectionOfDocumentInvalidCategoryThrowException() {
            parseContext = buildParseContextCaseTypeEntity(COLLECTION, DOCUMENT, DIVORCE_DOCUMENT);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);

            final InvalidImportException invalidImportException =
                    assertThrows(InvalidImportException.class, () -> categoryValidator.validate(parseContext));

            assertEquals(
                    "CaseFieldTab Invalid value 'divorceDocument' is not a valid CategoryID value."
                            + " Category cannot be found.",
                    invalidImportException.getMessage());
        }

        @Test
        void testValidateCaseFieldFieldTypeCollectionOfDocumentNullCategory() {
            parseContext = buildParseContextCaseTypeEntity(COLLECTION, DOCUMENT, null);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);
            categoryValidator.validate(parseContext);
            verify(parseContext, never()).getCategory(anyString(), anyString());
        }

        @Test
        void testValidateCaseFieldFieldTypeNotDocumentValidCategoryThrowException() {
            parseContext = buildParseContextCaseTypeEntity(TEXT_STRING, null, DIVORCE_DOCS);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);

            final InvalidImportException invalidImportException = assertThrows(InvalidImportException.class,
                () -> categoryValidator.validate(parseContext));

            assertEquals(
                    "CaseFieldTab Invalid value 'divorceDocs' is not a valid CategoryID value."
                            + " Category not permitted for this field type.",
                    invalidImportException.getMessage());
        }

        @Test
        void testValidateFieldTypeCollectionOfTextValidCategoryThrowException() {
            parseContext = buildParseContextCaseTypeEntity(COLLECTION, TEXT_STRING, DIVORCE_DOCS);

            CategoryEntity category1 = new CategoryEntity();
            category1.setCategoryId(DIVORCE_DOCS);
            parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
            final InvalidImportException invalidImportException = assertThrows(InvalidImportException.class,
                () -> categoryValidator.validate(parseContext));

            assertEquals(
                    "CaseFieldTab Invalid value 'divorceDocs' is not a valid CategoryID value."
                            + " Category not permitted for this field type.",
                    invalidImportException.getMessage());
        }

        @Test
        void testValidateCaseFieldFieldTypeDocumentWithBaseFieldTypeNullNoNullPointer() {
            parseContext = buildParseContextCaseTypeEntityWithBaseFieldType(DOCUMENT + "-" + UUID_STRING,
                null, DIVORCE_DOCS, null);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);

            final InvalidImportException invalidImportException =
                assertThrows(InvalidImportException.class, () -> categoryValidator.validate(parseContext));

            assertEquals(
                "CaseFieldTab Invalid value 'divorceDocs' is not a valid CategoryID value."
                    + " Category not permitted for this field type.",
                invalidImportException.getMessage());
        }

        @Test
        void testValidateCaseFieldFieldTypeDocumentWithBaseFieldTypeEqualsDocument() {
            parseContext = buildParseContextCaseTypeEntityWithBaseFieldType(DOCUMENT + "-" + UUID_STRING,
                null, DIVORCE_DOCS, DOCUMENT);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);
            categoryValidator.validate(parseContext);
            verify(parseContext).getCategory(CASE_TYPE, DIVORCE_DOCS);
        }

        @Test
        void testValidateCaseFieldFieldTypeDocumentWithBaseFieldTypeEqualsCollectionThrowException() {
            parseContext = buildParseContextCaseTypeEntityWithBaseFieldType(DOCUMENT + "-" + UUID_STRING,
                null, DIVORCE_DOCS, COLLECTION);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);

            final InvalidImportException invalidImportException =
                assertThrows(InvalidImportException.class, () -> categoryValidator.validate(parseContext));

            assertEquals(
                "CaseFieldTab Invalid value 'divorceDocs' is not a valid CategoryID value."
                    + " Category not permitted for this field type.",
                invalidImportException.getMessage());
        }
    }

    @Nested
    @DisplayName("Complex Type")
    class ComplexType {
        private static final String COMPLEX = "Complex";

        private ParseContext buildParseContextComplexType(String reference, String collectionType, String category) {
            val fieldTypeEntity1 = new FieldTypeEntity();
            val fieldTypeEntity2 = new FieldTypeEntity();

            fieldTypeEntity1.setReference(collectionType);
            fieldTypeEntity2.setReference(COMPLEX);
            fieldTypeEntity1.setBaseFieldType(fieldTypeEntity2);
            fieldTypeEntity1.getComplexFields()
                    .add(buildComplexTypeDocumentReference(reference, collectionType, category));

            ParseContext parseContext = new ParseContext();
            parseContext.addToAllTypes(fieldTypeEntity1);
            return spy(parseContext);
        }

        private ComplexFieldEntity buildComplexTypeDocumentReference(String reference, String collectionType,
                                                                    String category) {
            ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
            FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
            if (!reference.equals(COLLECTION)) {
                fieldTypeEntity.setReference(reference);
            } else {
                FieldTypeEntity baseFieldTypeEntity = new FieldTypeEntity();
                FieldTypeEntity collectionFieldTypeEntity = new FieldTypeEntity();
                fieldTypeEntity.setReference(COLLECTION_OF_DOCUMENT);
                baseFieldTypeEntity.setReference(reference);
                collectionFieldTypeEntity.setReference(collectionType);
                fieldTypeEntity.setBaseFieldType(baseFieldTypeEntity);
                fieldTypeEntity.setCollectionFieldType(collectionFieldTypeEntity);
            }
            complexFieldEntity.setFieldType(fieldTypeEntity);
            complexFieldEntity.setCategoryId(category);
            return complexFieldEntity;
        }

        @Test
        void testValidateComplexTypeCategoryNull() {
            parseContext = buildParseContextComplexType(TEXT_STRING, null, null);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);
            categoryValidator.validate(parseContext);
            verify(parseContext, never()).getCategory(anyString(), anyString());
        }

        @Test
        void testValidateComplexTypeFieldTypeDocument() {
            parseContext = buildParseContextComplexType(DOCUMENT, null, DIVORCE_DOCS);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);
            categoryValidator.validate(parseContext);
            verify(parseContext).checkCategoryExists(DIVORCE_DOCS);
        }

        @Test
        void testValidateComplexTypeFieldTypeCollectionOfDocument() {
            parseContext = buildParseContextComplexType(COLLECTION, DOCUMENT, DIVORCE_DOCS);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);
            categoryValidator.validate(parseContext);
            verify(parseContext).checkCategoryExists(DIVORCE_DOCS);
        }

        @Test
        void testValidateComplexTypeFieldTypeDocumentInvalidCategoryThrowException() {
            parseContext = buildParseContextComplexType(DOCUMENT, null, DIVORCE_DOCUMENT);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);

            final InvalidImportException invalidImportException =
                    assertThrows(InvalidImportException.class, () -> categoryValidator.validate(parseContext));

            assertEquals(
                    "ComplexTypesTab Invalid value 'divorceDocument' is not a valid CategoryID value."
                            + " Category cannot be found.",
                    invalidImportException.getMessage());
        }

        @Test
        void testValidateComplexTypeFieldTypeCollectionOfDocumentInvalidCategoryThrowException() {
            parseContext = buildParseContextComplexType(COLLECTION, DOCUMENT, DIVORCE_DOCUMENT);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);

            final InvalidImportException invalidImportException =
                    assertThrows(InvalidImportException.class, () -> categoryValidator.validate(parseContext));

            assertEquals(
                    "ComplexTypesTab Invalid value 'divorceDocument' is not a valid CategoryID value."
                            + " Category cannot be found.",
                    invalidImportException.getMessage());
        }

        @Test
        void testValidateComplexTypeFieldTypeCollectionOfDocumentNullCategory() {
            parseContext = buildParseContextComplexType(COLLECTION, DOCUMENT, null);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);
            categoryValidator.validate(parseContext);
            verify(parseContext, never()).checkCategoryExists(anyString());
        }

        @Test
        void testValidateComplexTypeFieldTypeNotDocumentValidCategoryThrowException() {
            parseContext = buildParseContextComplexType(TEXT_STRING, null, DIVORCE_DOCS);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);

            final InvalidImportException invalidImportException = assertThrows(InvalidImportException.class,
                () -> categoryValidator.validate(parseContext));

            assertEquals(
                    "ComplexTypesTab Invalid value 'divorceDocs' is not a valid CategoryID value."
                            + " Category not permitted for this field type.",
                    invalidImportException.getMessage());
        }

        @Test
        void testValidateComplexTypeFieldTypeCollectionOfTextValidCategoryThrowException() {
            parseContext = buildParseContextComplexType(COLLECTION, TEXT_STRING, DIVORCE_DOCS);

            parseContext.registerCaseTypeForCategory(CASE_TYPE, divorceDocsCategoryEntity);

            final InvalidImportException invalidImportException = assertThrows(InvalidImportException.class,
                () -> categoryValidator.validate(parseContext));

            assertEquals(
                    "ComplexTypesTab Invalid value 'divorceDocs' is not a valid CategoryID value."
                            + " Category not permitted for this field type.",
                    invalidImportException.getMessage());
        }
    }
}
