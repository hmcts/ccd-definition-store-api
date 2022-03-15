package uk.gov.hmcts.ccd.definition.store.excel.validation;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public class CategoryIdValidatorTest {

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
    public void setup() {
        categoryValidator = new CategoryIdValidator();
    }

    protected ParseContext buildParseContextCaseTypeEntity(String reference, String collectionType, String category) {
        val parseContext = new ParseContext();
        val caseTypeEntity = new CaseTypeEntity();

        caseTypeEntity.setReference(CASE_TYPE);
        caseTypeEntity.addCaseField(buildCaseTypeDocumentReference(reference, collectionType, category));

        parseContext.registerCaseType(caseTypeEntity);
        return parseContext;
    }

    public CaseFieldEntity buildCaseTypeDocumentReference(String reference, String collectionType, String category) {
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
    public void testValidateCaseFieldFieldTypeCollectionOfTextValidCategoryThrowException() {
        parseContext = buildParseContextCaseTypeEntity(COLLECTION, TEXT_STRING, DIVORCE_DOCS);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId(DIVORCE_DOCS);
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        Assertions.assertThrows(InvalidImportException.class, () -> {
            categoryValidator.validate(parseContext);
        });
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
