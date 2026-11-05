package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class DisplayGroupCaseFieldElementPathValidatorImplTest {

    private static final String CASE_TYPE = "Case Type I";

    private DisplayGroupCaseFieldElementPathValidatorImpl validator;
    private CaseTypeEntity caseType;
    private CaseFieldEntity applicant;
    private CaseFieldEntity simpleField;
    private CaseFieldEntity companies;

    @BeforeEach
    void setUp() {
        validator = new DisplayGroupCaseFieldElementPathValidatorImpl(new CaseFieldEntityUtil());

        applicant = caseFieldEntity("Applicant", complexFieldTypeEntity("ApplicantType", List.of(
            complexFieldEntity("Name", textFieldTypeEntity()),
            complexFieldEntity("Address", complexFieldTypeEntity("AddressType", List.of(
                complexFieldEntity("PostCode", textFieldTypeEntity())
            ))),
            complexFieldEntity("PreviousAddresses", collectionFieldTypeEntity("PreviousAddressesCollection",
                complexFieldTypeEntity("AddressType", List.of(
                    complexFieldEntity("PostCode", textFieldTypeEntity())
                )))
            )
        )));
        simpleField = caseFieldEntity("Description", textFieldTypeEntity());
        companies = caseFieldEntity("Companies", collectionFieldTypeEntity("CompaniesCollection",
            complexFieldTypeEntity("CompanyType", List.of(complexFieldEntity("Name", textFieldTypeEntity())))));

        caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE);
        caseType.addCaseField(applicant);
        caseType.addCaseField(simpleField);
        caseType.addCaseField(companies);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Name", "Address.PostCode", "Address"})
    void shouldValidateComplexSubfield(String listElementCode) {
        ValidationResult result = validator.validate(displayGroupCaseField(applicant, listElementCode));

        assertThat(result.isValid(), is(true));
    }

    @Test
    void shouldSkipValidationWhenListElementCodeIsBlank() {
        ValidationResult result = validator.validate(displayGroupCaseField(simpleField, null));

        assertThat(result.isValid(), is(true));
    }

    @Test
    void shouldFailValidationForInvalidListElementCode() {
        ValidationResult result = validator.validate(displayGroupCaseField(applicant, "DoesNotExist"));

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("Invalid ListElementCode 'DoesNotExist' for case type 'Case Type I', case field 'Applicant'"));
    }

    @Test
    void shouldFailValidationWhenListElementCodeDefinedForSimpleField() {
        ValidationResult result = validator.validate(displayGroupCaseField(simpleField, "Name"));

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("ListElementCode 'Name' can be only defined for complex fields. "
                + "Case Field 'Description', case type 'Case Type I'"));
    }

    @Test
    void shouldFailValidationWhenListElementCodeDefinedForCollectionField() {
        ValidationResult result = validator.validate(displayGroupCaseField(companies, "Name"));

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("ListElementCode 'Name' is not supported for collection fields. "
                + "Case Field 'Companies', case type 'Case Type I'"));
    }

    @Test
    void shouldFailValidationWhenListElementCodeTraversesNestedCollectionField() {
        ValidationResult result = validator.validate(displayGroupCaseField(applicant, "PreviousAddresses.PostCode"));

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("ListElementCode 'PreviousAddresses.PostCode' is not supported for collection fields. "
                + "Case Field 'Applicant', case type 'Case Type I'"));
    }

    private DisplayGroupCaseFieldEntity displayGroupCaseField(CaseFieldEntity caseField, String listElementCode) {
        DisplayGroupEntity displayGroup = new DisplayGroupEntity();
        displayGroup.setCaseType(caseType);

        DisplayGroupCaseFieldEntity displayGroupCaseField = new DisplayGroupCaseFieldEntity();
        displayGroupCaseField.setCaseField(caseField);
        displayGroupCaseField.setCaseFieldElementPath(listElementCode);
        displayGroup.addDisplayGroupCaseField(displayGroupCaseField);

        return displayGroupCaseField;
    }

    private static CaseFieldEntity caseFieldEntity(String reference, FieldTypeEntity fieldTypeEntity) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        caseFieldEntity.setFieldType(fieldTypeEntity);
        return caseFieldEntity;
    }

    private static ComplexFieldEntity complexFieldEntity(String reference, FieldTypeEntity fieldTypeEntity) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reference);
        complexFieldEntity.setFieldType(fieldTypeEntity);
        return complexFieldEntity;
    }

    private static FieldTypeEntity textFieldTypeEntity() {
        return fieldTypeEntity("Text");
    }

    private static FieldTypeEntity complexFieldTypeEntity(String reference,
                                                         List<ComplexFieldEntity> complexFieldEntities) {
        FieldTypeEntity fieldTypeEntity = fieldTypeEntity(reference);
        fieldTypeEntity.setBaseFieldType(fieldTypeEntity("Complex"));
        fieldTypeEntity.addComplexFields(complexFieldEntities);
        return fieldTypeEntity;
    }

    private static FieldTypeEntity collectionFieldTypeEntity(String reference,
                                                            FieldTypeEntity collectionFieldType) {
        FieldTypeEntity fieldTypeEntity = fieldTypeEntity(reference);
        fieldTypeEntity.setBaseFieldType(fieldTypeEntity("Collection"));
        fieldTypeEntity.setCollectionFieldType(collectionFieldType);
        return fieldTypeEntity;
    }

    private static FieldTypeEntity fieldTypeEntity(String reference) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.addComplexFields(emptyList());
        return fieldTypeEntity;
    }
}
