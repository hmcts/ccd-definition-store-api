package uk.gov.hmcts.ccd.definition.store.excel.validation;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class CategoryIdValidatorTest {

    private static final String COLLECTION_OF_DOCUMENT = "CollectionOfDocument";
    private static final String CASE_TYPE = "FT_MasterCaseType";
    private static final String COMPLEX = "Complex";
    private static final String COLLECTION = "Collection";
    private static final String DIVORCE_DOCS = "divorceDocs";
    private static final String DOCUMENT = "Document";
    private static final String DIVORCE_DOCUMENT = "divorceDocument";
    private static final String TEXT_STRING = "TEXT";

    private ParseContext parseContext;
    private CategoryIdValidator categoryValidator;

    @BeforeEach
    void setup() {
        categoryValidator = new CategoryIdValidator();
    }

    private ParseContext buildParseContextCaseTypeEntity(String reference, String collectionType, String category) {
        val parseContext = new ParseContext();
        val caseTypeEntity = new CaseTypeEntity();

        caseTypeEntity.setReference(CASE_TYPE);
        caseTypeEntity.addCaseField(buildCaseTypeDocumentReference(reference, collectionType, category));

        parseContext.registerCaseType(caseTypeEntity);
        return spy(parseContext);
    }

    private CaseFieldEntity buildCaseTypeDocumentReference(String reference, String collectionType, String category) {
        CaseFieldEntity caseField = new CaseFieldEntity();
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
        caseField.setFieldType(fieldTypeEntity);
        caseField.setCategoryId(category);
        return caseField;
    }

    protected ParseContext buildParseContextComplexType(String reference, String collectionType, String category) {
        val fieldTypeEntity1 = new FieldTypeEntity();
        val fieldTypeEntity2 = new FieldTypeEntity();

        fieldTypeEntity1.setReference(collectionType);
        fieldTypeEntity2.setReference(COMPLEX);
        fieldTypeEntity1.setBaseFieldType(fieldTypeEntity2);
        fieldTypeEntity1.getComplexFields().add(buildComplexTypeDocumentReference(reference, collectionType, category));

        ParseContext parseContext = new ParseContext();
        parseContext.addToAllTypes(fieldTypeEntity1);
        return parseContext;
    }

    public ComplexFieldEntity buildComplexTypeDocumentReference(String reference, String collectionType,
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

    /*CASE FIELD*/

    @Test
    public void testValidateCaseFieldCategoryNull() {
        parseContext = buildParseContextCaseTypeEntity(TEXT_STRING, null, null);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test
    public void testValidateCaseFieldFieldTypeDocument() {
        parseContext = buildParseContextCaseTypeEntity(DOCUMENT, null, DIVORCE_DOCS);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test
    public void testValidateCaseFieldFieldTypeCollectionOfDocument() {
        parseContext = buildParseContextCaseTypeEntity(COLLECTION, DOCUMENT, DIVORCE_DOCS);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test
    public void testValidateCaseFieldFieldTypeDocumentInvalidCategoryThrowException() {
        parseContext = buildParseContextCaseTypeEntity(DOCUMENT, null, DIVORCE_DOCUMENT);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        Assertions.assertThrows(InvalidImportException.class, () -> {
            categoryValidator.validate(parseContext);
        });
    }

    @Test
    public void testValidateCaseFieldFieldTypeCollectionOfDocumentInvalidCategoryThrowException() {
        parseContext = buildParseContextCaseTypeEntity(COLLECTION, DOCUMENT, DIVORCE_DOCUMENT);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        Assertions.assertThrows(InvalidImportException.class, () -> {
            categoryValidator.validate(parseContext);
        });
    }

    @Test
    public void testValidateCaseFieldFieldTypeCollectionOfDocumentNullCategory() {
        parseContext = buildParseContextCaseTypeEntity(COLLECTION, DOCUMENT, null);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test
    public void testValidateCaseFieldFieldTypeNotDocumentValidCategoryThrowException() {
        parseContext = buildParseContextCaseTypeEntity(TEXT_STRING, null, DIVORCE_DOCS);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        Assertions.assertThrows(InvalidImportException.class, () -> {
            categoryValidator.validate(parseContext);
        });
    }

    @Test
    void testValidateCategoryNull() {
        parseContext = buildParseContext(TEXT_STRING, null, null);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
        verify(parseContext, never()).getCategory(anyString(), anyString());
    }

    @Test
    void testValidateFieldTypeDocument() {
        parseContext = buildParseContext(DOCUMENT, null, DIVORCE_DOCS);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
        verify(parseContext).getCategory(CASE_TYPE, DIVORCE_DOCS);
    }

    @Test
    void testValidateFieldTypeCollectionOfDocument() {
        parseContext = buildParseContext(COLLECTION, DOCUMENT, DIVORCE_DOCS);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
        verify(parseContext).getCategory(CASE_TYPE, DIVORCE_DOCS);
    }

    @Test
    void testValidateFieldTypeDocumentInvalidCategoryThrowException() {
        parseContext = buildParseContext(DOCUMENT, null, DIVORCE_DOCUMENT);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        final InvalidImportException invalidImportException =
                assertThrows(InvalidImportException.class, () -> categoryValidator.validate(parseContext));

        assertEquals(
                "CaseFieldTab Invalid value 'divorceDocument' is not a valid CategoryID value."
                    + " Category cannot be found.",
                invalidImportException.getMessage());
    }

    @Test
    void testValidateFieldTypeCollectionOfDocumentInvalidCategoryThrowException() {
        parseContext = buildParseContext(COLLECTION, DOCUMENT, DIVORCE_DOCUMENT);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        final InvalidImportException invalidImportException =
                assertThrows(InvalidImportException.class, () -> categoryValidator.validate(parseContext));

        assertEquals(
                "CaseFieldTab Invalid value 'divorceDocument' is not a valid CategoryID value."
                    + " Category cannot be found.",
                invalidImportException.getMessage());
    }

    @Test
    void testValidateFieldTypeCollectionOfDocumentNullCategory() {
        parseContext = buildParseContext(COLLECTION, DOCUMENT, null);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
        verify(parseContext, never()).getCategory(anyString(), anyString());
    }

    @Test
    void testValidateFieldTypeNotDocumentValidCategoryThrowException() {
        parseContext = buildParseContext(TEXT_STRING, null, DIVORCE_DOCS);

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
    void testValidateFieldTypeCollectionOfTextValidCategoryThrowException() {
        parseContext = buildParseContext(COLLECTION, TEXT_STRING, DIVORCE_DOCS);

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


    /*COMPLEX TYPE*/

    @Test
    public void testValidateComplexTypeCategoryNull() {
        parseContext = buildParseContextComplexType(TEXT_STRING, null, null);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test
    public void testValidateComplexTypeFieldTypeDocument() {
        parseContext = buildParseContextComplexType(DOCUMENT, null, DIVORCE_DOCS);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test
    public void testValidateComplexTypeFieldTypeCollectionOfDocument() {
        parseContext = buildParseContextComplexType(COLLECTION, DOCUMENT, DIVORCE_DOCS);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test
    public void testValidateComplexTypeFieldTypeDocumentInvalidCategoryThrowException() {
        parseContext = buildParseContextComplexType(DOCUMENT, null, DIVORCE_DOCUMENT);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        Assertions.assertThrows(InvalidImportException.class, () -> {
            categoryValidator.validate(parseContext);
        });
    }

    @Test
    public void testValidateComplexTypeFieldTypeCollectionOfDocumentInvalidCategoryThrowException() {
        parseContext = buildParseContextComplexType(COLLECTION, DOCUMENT, DIVORCE_DOCUMENT);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        Assertions.assertThrows(InvalidImportException.class, () -> {
            categoryValidator.validate(parseContext);
        });
    }

    @Test
    public void testValidateComplexTypeFieldTypeCollectionOfDocumentNullCategory() {
        parseContext = buildParseContextComplexType(COLLECTION, DOCUMENT, null);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test
    public void testValidateComplexTypeFieldTypeNotDocumentValidCategoryThrowException() {
        parseContext = buildParseContextComplexType(TEXT_STRING, null, DIVORCE_DOCS);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        Assertions.assertThrows(InvalidImportException.class, () -> {
            categoryValidator.validate(parseContext);
        });
    }

    @Test
    public void testValidateComplexTypeFieldTypeCollectionOfTextValidCategoryThrowException() {
        parseContext = buildParseContextComplexType(COLLECTION, TEXT_STRING, DIVORCE_DOCS);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        Assertions.assertThrows(InvalidImportException.class, () -> {
            categoryValidator.validate(parseContext);
        });
    }
}
