package uk.gov.hmcts.ccd.definition.store.excel.validation;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public class CategoryIdValidatorTest {

    private ParseContext parseContext;
    private static final String CASE_TYPE = "FT_MasterCaseType";

    private CategoryIdValidator categoryValidator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        categoryValidator = new CategoryIdValidator();
    }

    protected ParseContext buildParseContext(String reference, String collectionType, String category) {
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
        if (!reference.equals("Collection")) {
            fieldTypeEntity.setReference(reference);
        } else {
            FieldTypeEntity baseFieldTypeEntity = new FieldTypeEntity();
            FieldTypeEntity collectionFieldTypeEntity = new FieldTypeEntity();
            fieldTypeEntity.setReference("CollectionOfDocument");
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
    public void testValidateCategoryNull() {
        parseContext = buildParseContext("Text", null, null);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId("divorceDocs");
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test
    public void testValidateFieldTypeDocument() {
        parseContext = buildParseContext("Document", null, "divorceDocs");

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId("divorceDocs");
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test
    public void testValidateFieldTypeCollectionOfDocument() {
        parseContext = buildParseContext("Collection", "Document", "divorceDocs");

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId("divorceDocs");
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test(expected = InvalidImportException.class)
    public void testValidateFieldTypeDocumentInvalidCategoryThrowException() {
        parseContext = buildParseContext("Document", null, "divorceDocument");

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId("divorceDoc");
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test(expected = InvalidImportException.class)
    public void testValidateFieldTypeCollectionOfDocumentInvalidCategoryThrowException() {
        parseContext = buildParseContext("Collection", "Document", "divorceDocument");

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId("divorceDoc");
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test
    public void testValidateFieldTypeCollectionOfDocumentNullCategory() {
        parseContext = buildParseContext("Collection", "Document", null);

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId("divorceDoc");
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test(expected = InvalidImportException.class)
    public void testValidateFieldTypeNotDocumentValidCategoryThrowException() {
        parseContext = buildParseContext("Text", null, "divorceDoc");

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId("divorceDoc");
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

    @Test(expected = InvalidImportException.class)
    public void testValidateFieldTypeCollectionOfTextValidCategoryThrowException() {
        parseContext = buildParseContext("Collection", "Text", "divorceDoc");

        CategoryEntity category1 = new CategoryEntity();
        category1.setCategoryId("divorceDoc");
        parseContext.registerCaseTypeForCategory(CASE_TYPE, category1);
        categoryValidator.validate(parseContext);
    }

}
