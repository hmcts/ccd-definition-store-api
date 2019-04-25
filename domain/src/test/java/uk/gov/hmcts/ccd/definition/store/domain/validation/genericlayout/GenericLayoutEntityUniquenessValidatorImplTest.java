package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout.GenericLayoutEntityUniquenessValidatorImpl.ERROR_MESSAGE_DUPLICATES_FOUND_FOR_TYPE_REF_FIELD_REF_AND_PATH;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketInputCaseFieldEntity;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Generic Layout Entity Uniqueness Validator Implementation Tests")
class GenericLayoutEntityUniquenessValidatorImplTest {
    private static final String CASE_FIELD = "Case Field I";

    private GenericLayoutValidator validator;

    private CaseTypeEntity caseType;
    private CaseFieldEntity caseField;

    @BeforeEach
    void setUp() {
        validator = new GenericLayoutEntityUniquenessValidatorImpl();

        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setBaseFieldType(fieldTypeEntity("Text", emptyList()));

        caseField = new CaseFieldEntity();
        caseField.setReference(CASE_FIELD);
        caseField.setFieldType(fieldTypeEntity);

        caseType = new CaseTypeEntity();
        caseType.setReference("Case Type I");
        caseType.addCaseField(caseField);
    }

    @Nested
    @DisplayName("GenericLayoutEntity uniqueness validation tests")
    class GenericLayoutEntityUniquenessTests {

        @Test
        public void shouldFailIfCaseTypeCaseFieldAndListElementCodeNotUniqueForWorkbasketInput() {
            String caseTypeRef = "ComplexCollectionComplex";
            String caseFieldRef = "FamilyDetails";
            String elementPath = "MotherName";

            GenericLayoutEntity entity1 = new WorkBasketInputCaseFieldEntity();
            entity1.setCaseField(caseFieldEntity(caseFieldRef, complexFieldTypeEntity("Family",
                singletonList(complexFieldEntity(elementPath, fieldTypeEntity("Text", emptyList()))))));
            entity1.setCaseType(createCaseTypeEntity(caseTypeRef));
            entity1.setCaseFieldElementPath(elementPath);
            entity1.setLabel("label1");

            GenericLayoutEntity entity2 = new WorkBasketInputCaseFieldEntity();
            entity2.setCaseField(caseFieldEntity(caseFieldRef, complexFieldTypeEntity("Family",
                singletonList(complexFieldEntity(elementPath, fieldTypeEntity("Text", emptyList()))))));
            entity2.setCaseType(createCaseTypeEntity(caseTypeRef));
            entity2.setCaseFieldElementPath(elementPath);
            entity2.setLabel("label2");

            GenericLayoutEntity otherEntity = new WorkBasketInputCaseFieldEntity();
            otherEntity.setCaseField(caseFieldEntity(caseFieldRef, textFieldTypeEntity()));
            otherEntity.setCaseType(createCaseTypeEntity("School"));
            otherEntity.setCaseFieldElementPath("");

            final ValidationResult result = validator.validate(asList(entity1, entity2, otherEntity));

            assertAll(
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(2)),
                () -> assertEquals(result.getValidationErrors().stream().filter(e -> e.getDefaultMessage()
                    .equals(format(ERROR_MESSAGE_DUPLICATES_FOUND_FOR_TYPE_REF_FIELD_REF_AND_PATH,
                        caseTypeRef, caseFieldRef, elementPath, "label1")))
                    .count(),1),
                () -> assertEquals(result.getValidationErrors().stream().filter(e -> e.getDefaultMessage()
                    .equals(format(ERROR_MESSAGE_DUPLICATES_FOUND_FOR_TYPE_REF_FIELD_REF_AND_PATH,
                        caseTypeRef, caseFieldRef, elementPath, "label2")))
                    .count(),1)
            );
        }

        @Test
        void shouldFailIfCaseTypeCaseFieldNotUniqueAndListElementCodeNullForWorkbasketInput() {
            String caseTypeRef = "ComplexCollectionComplex";
            String caseFieldRef = "FamilyDetails";
            String elementPath = null;

            GenericLayoutEntity entity1 = new WorkBasketInputCaseFieldEntity();
            entity1.setCaseField(caseFieldEntity(caseFieldRef, textFieldTypeEntity()));
            entity1.setCaseType(createCaseTypeEntity(caseTypeRef));
            entity1.setCaseFieldElementPath(elementPath);
            entity1.setLabel("label1");

            GenericLayoutEntity entity2 = new WorkBasketInputCaseFieldEntity();
            entity2.setCaseField(caseFieldEntity(caseFieldRef, textFieldTypeEntity()));
            entity2.setCaseType(createCaseTypeEntity(caseTypeRef));
            entity2.setCaseFieldElementPath(elementPath);
            entity2.setLabel("label2");

            GenericLayoutEntity otherEntity = new WorkBasketInputCaseFieldEntity();
            otherEntity.setCaseField(caseFieldEntity(caseFieldRef, textFieldTypeEntity()));
            otherEntity.setCaseType(createCaseTypeEntity("School"));
            otherEntity.setCaseFieldElementPath("");

            final ValidationResult result = validator.validate(asList(entity1, entity2, otherEntity));

            assertAll(
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(2)),
                () -> assertEquals(result.getValidationErrors().stream().filter(e -> e.getDefaultMessage()
                    .equals(format(ERROR_MESSAGE_DUPLICATES_FOUND_FOR_TYPE_REF_FIELD_REF_AND_PATH,
                        caseTypeRef, caseFieldRef, elementPath, "label1")))
                    .count(),1),
                () -> assertEquals(result.getValidationErrors().stream().filter(e -> e.getDefaultMessage()
                    .equals(format(ERROR_MESSAGE_DUPLICATES_FOUND_FOR_TYPE_REF_FIELD_REF_AND_PATH,
                        caseTypeRef, caseFieldRef, elementPath, "label2")))
                    .count(),1)
            );
        }

    }

    private static FieldTypeEntity fieldTypeEntity(String reference,
                                                   List<ComplexFieldEntity> complexFieldEntityList) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.addComplexFields(complexFieldEntityList);
        return fieldTypeEntity;
    }

    private static FieldTypeEntity textFieldTypeEntity() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference("Text");
        fieldTypeEntity.addComplexFields(emptyList());
        return fieldTypeEntity;
    }

    private static CaseFieldEntity caseFieldEntity(String reference, FieldTypeEntity fieldTypeEntity) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        caseFieldEntity.setFieldType(fieldTypeEntity);
        return caseFieldEntity;
    }

    private CaseTypeEntity createCaseTypeEntity(String reference) {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(reference);
        return caseTypeEntity;
    }

    private static FieldTypeEntity complexFieldTypeEntity(String reference,
                                                          List<ComplexFieldEntity> complexFieldEntityList) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.setBaseFieldType(fieldTypeEntity("Complex", emptyList()));
        fieldTypeEntity.addComplexFields(complexFieldEntityList);
        return fieldTypeEntity;
    }

    private static ComplexFieldEntity complexFieldEntity(String reference, FieldTypeEntity fieldTypeEntity) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reference);
        complexFieldEntity.setFieldType(fieldTypeEntity);
        return complexFieldEntity;
    }
}
