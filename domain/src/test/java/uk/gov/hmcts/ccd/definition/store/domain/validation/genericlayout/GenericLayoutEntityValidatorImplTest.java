package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout.GenericLayoutEntityValidatorImpl.ERROR_MESSAGE_DUPLICATES_FOUND_FOR_TYPE_REF_FIELD_REF_AND_PATH;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchResultCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketInputCaseFieldEntity;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

@DisplayName("Generic Layout Entity Validator Implementation Tests")
class GenericLayoutEntityValidatorImplTest {
    private static final String CASE_FIELD = "Case Field I";

    private GenericLayoutEntityValidatorImpl validator;

    private CaseTypeEntity caseType;
    private CaseFieldEntity caseField;

    @BeforeEach
    void setUp() {
        validator = new GenericLayoutEntityValidatorImpl(new CaseFieldEntityUtil());

        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setBaseFieldType(fieldTypeEntity("Text", emptyList()));

        caseField = new CaseFieldEntity();
        caseField.setReference(CASE_FIELD);
        caseField.setFieldType(fieldTypeEntity);

        caseType = new CaseTypeEntity();
        caseType.setReference("Case Type I");
        caseType.addCaseField(caseField);
    }

    static class EntityArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(new SearchInputCaseFieldEntity()),
                Arguments.of(new SearchResultCaseFieldEntity()),
                Arguments.of(new WorkBasketInputCaseFieldEntity()),
                Arguments.of(new WorkBasketCaseFieldEntity())
            );
        }
    }

    static class ValidateGoodCollectionEntityArgProvider implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("ClassName", new SearchInputCaseFieldEntity()),
                Arguments.of("ClassName", new SearchResultCaseFieldEntity()),
                Arguments.of("ClassName", new WorkBasketInputCaseFieldEntity()),
                Arguments.of("ClassName", new WorkBasketCaseFieldEntity()),
                Arguments.of("ClassAddress.AddressLine1", new SearchInputCaseFieldEntity()),
                Arguments.of("ClassAddress.AddressLine1", new SearchResultCaseFieldEntity()),
                Arguments.of("ClassAddress.AddressLine1", new WorkBasketInputCaseFieldEntity()),
                Arguments.of("ClassAddress.AddressLine1", new WorkBasketCaseFieldEntity()),
                Arguments.of("ClassMembers.Name", new SearchInputCaseFieldEntity()),
                Arguments.of("ClassMembers.Name", new SearchResultCaseFieldEntity()),
                Arguments.of("ClassMembers.Name", new WorkBasketInputCaseFieldEntity()),
                Arguments.of("ClassMembers.Name", new WorkBasketCaseFieldEntity()),
                Arguments.of("ClassMembers.FixedListGender", new SearchInputCaseFieldEntity()),
                Arguments.of("ClassMembers.FixedListGender", new SearchResultCaseFieldEntity()),
                Arguments.of("ClassMembers.FixedListGender", new WorkBasketInputCaseFieldEntity()),
                Arguments.of("ClassMembers.FixedListGender", new WorkBasketCaseFieldEntity())
            );
        }
    }

    static class ValidateGoodComplexEntityArgProvider implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("Name", new SearchInputCaseFieldEntity()),
                Arguments.of("Name", new SearchResultCaseFieldEntity()),
                Arguments.of("Name", new WorkBasketInputCaseFieldEntity()),
                Arguments.of("Name", new WorkBasketCaseFieldEntity()),
                Arguments.of("ProvidesSupport", new SearchInputCaseFieldEntity()),
                Arguments.of("ProvidesSupport", new SearchResultCaseFieldEntity()),
                Arguments.of("ProvidesSupport", new WorkBasketInputCaseFieldEntity()),
                Arguments.of("ProvidesSupport", new WorkBasketCaseFieldEntity()),
                Arguments.of("Class.ClassName", new SearchInputCaseFieldEntity()),
                Arguments.of("Class.ClassName", new SearchResultCaseFieldEntity()),
                Arguments.of("Class.ClassName", new WorkBasketInputCaseFieldEntity()),
                Arguments.of("Class.ClassName", new WorkBasketCaseFieldEntity()),
                Arguments.of("Class.ClassAddress.AddressLine1", new SearchInputCaseFieldEntity()),
                Arguments.of("Class.ClassAddress.AddressLine1", new SearchResultCaseFieldEntity()),
                Arguments.of("Class.ClassAddress.AddressLine1", new WorkBasketInputCaseFieldEntity()),
                Arguments.of("Class.ClassAddress.AddressLine1", new WorkBasketCaseFieldEntity()),
                Arguments.of("Class.ClassMembers.FixedListGender", new SearchInputCaseFieldEntity()),
                Arguments.of("Class.ClassMembers.FixedListGender", new SearchResultCaseFieldEntity()),
                Arguments.of("Class.ClassMembers.FixedListGender", new WorkBasketInputCaseFieldEntity()),
                Arguments.of("Class.ClassMembers.FixedListGender", new WorkBasketCaseFieldEntity()),
                Arguments.of("Class.ClassMembers.Name", new SearchInputCaseFieldEntity()),
                Arguments.of("Class.ClassMembers.Name", new SearchResultCaseFieldEntity()),
                Arguments.of("Class.ClassMembers.Name", new WorkBasketInputCaseFieldEntity()),
                Arguments.of("Class.ClassMembers.Name", new WorkBasketCaseFieldEntity())
            );
        }
    }

    @Nested
    @DisplayName("SearchInputCaseFieldEntity validation tests")
    class SearchInputCaseFieldEntityTests {

        @ParameterizedTest
        @ArgumentsSource(EntityArgumentsProvider.class)
        void shouldValidateGoodEntity(GenericLayoutEntity entity) {
//            System.out.println("testing... " + entity.getClass().getSimpleName());
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            final ValidationResult result = validator.validate(singletonList(entity));

            assertAll(
                () -> assertThat(result.isValid(), is(true))
            );
        }

        @ParameterizedTest
        @ArgumentsSource(ValidateGoodComplexEntityArgProvider.class)
        void shouldValidateGoodComplexEntity(String path, GenericLayoutEntity entity) {
//            System.out.println("testing... " + path + "   " + entity.getClass().getSimpleName());
            CaseFieldEntity complexCaseField = complexCaseField();
            entity.setCaseField(complexCaseField);
            entity.setCaseType(caseType);
            caseType.addCaseField(complexCaseField);
            entity.setCaseFieldElementPath(path);

            final ValidationResult result = validator.validate(singletonList(entity));

            assertAll(
                () -> assertThat(result.isValid(), is(true))
            );
        }

        @ParameterizedTest
        @ArgumentsSource(ValidateGoodCollectionEntityArgProvider.class)
        void shouldValidateGoodCollectionEntity(String path, GenericLayoutEntity entity) {
            // System.out.println("testing... " + path + "   " + entity.getClass().getSimpleName());
            CaseFieldEntity collectionCaseField = collectionCaseField();
            entity.setCaseField(collectionCaseField);
            entity.setCaseType(caseType);
            caseType.addCaseField(collectionCaseField);
            entity.setCaseFieldElementPath(path);

            final ValidationResult result = validator.validate(singletonList(entity));

            assertAll(
                () -> assertThat(result.isValid(), is(true))
            );
        }

        @ParameterizedTest
        @ArgumentsSource(EntityArgumentsProvider.class)
        void shouldFailValidationWhenListElementCodeDefinedForNonComplexField(GenericLayoutEntity entity) {
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            entity.setCaseFieldElementPath("SomeNonExistingPath");

            final ValidationResult result = validator.validate(singletonList(entity));

            assertAll(
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("ListElementCode 'SomeNonExistingPath' can be only defined for complex fields. Case Field 'Case Field I', case type 'Case Type I'"))
            );
        }

        @ParameterizedTest
        @ArgumentsSource(EntityArgumentsProvider.class)
        void shouldFailValidationForInvalidListElementCode(GenericLayoutEntity entity) {
            entity.setLabel("Label");
            CaseFieldEntity collectionComplexCaseField = complexCaseField();
            entity.setCaseField(collectionComplexCaseField);
            entity.setCaseType(caseType);
            caseType.addCaseField(collectionComplexCaseField);
            entity.setCaseFieldElementPath("SomeNonExistingPath");

            final ValidationResult result = validator.validate(singletonList(entity));

            assertAll(
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Invalid ListElementCode 'SomeNonExistingPath' for case type 'Case Type I', case field 'MySchool' with label 'Label'"))
            );
        }

        @ParameterizedTest
        @ArgumentsSource(EntityArgumentsProvider.class)
        void shouldFailWhenDisplayOrderIsNotPositive(GenericLayoutEntity entity) {
            entity.setLabel("Label");
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            entity.setOrder(-1);
            final ValidationResult result = validator.validate(singletonList(entity));

            assertAll(
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                         is("DisplayOrder '-1' needs to be a valid integer for row with label 'Label', case field '" +
                                CASE_FIELD + "'"))
            );
        }

        @ParameterizedTest
        @ArgumentsSource(EntityArgumentsProvider.class)
        void shouldFailWhenCaseTypeIsEmpty(GenericLayoutEntity entity) {
            entity.setLabel("Label");
            entity.setCaseField(caseField);
            final ValidationResult result = validator.validate(singletonList(entity));

            assertAll(
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Type cannot be empty for row with label 'Label', case field 'Case Field I'"))
            );
        }

        @ParameterizedTest
        @ArgumentsSource(EntityArgumentsProvider.class)
        void shouldFailWhenCaseFieldIsEmpty(GenericLayoutEntity entity) {
            entity.setLabel("Label");
            entity.setCaseType(caseType);
            final ValidationResult result = validator.validate(singletonList(entity));

            assertAll(
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Field cannot be empty for row with label 'Label', case type 'Case Type I'"))
            );
        }

        @ParameterizedTest
        @ArgumentsSource(EntityArgumentsProvider.class)
        void shouldFailWhenBothCaseTypeAndCaseFieldAreEmpty(GenericLayoutEntity entity) {
            entity.setLabel("Label");
            final ValidationResult result = validator.validate(singletonList(entity));

            assertAll(
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(2)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Type cannot be empty for row with label 'Label', case field ''")),
                () ->
                    assertThat(result.getValidationErrors().get(1).getDefaultMessage(),
                        is("Case Field cannot be empty for row with label 'Label', case type ''"))
            );
        }

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
                () -> assertThat(result.getValidationErrors().size(), is(4)),
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

    private CaseFieldEntity complexCaseField() {
        return caseFieldEntity("MySchool", complexFieldTypeEntity("School",
            asList(
                complexFieldEntity("Name", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("ProvidesSupport", fieldTypeEntity("YesOrNo", emptyList())),
                complexFieldEntity("Class", collectionFieldTypeEntity("Class-8fcabcec-327f-4b4b-99b9-9dadb8317da8",
                    complexFieldTypeEntity("SchoolClass", asList(
                        complexFieldEntity("ClassName", fieldTypeEntity("Text", emptyList())),
                        complexFieldEntity("ClassAddress", addressUKFieldTypeEntity()),
                        complexFieldEntity("ClassMembers", collectionFieldTypeEntity("ClassMembers-f07e3000-a3c4-4232-ac69-586b7b013bf1",
                            complexFieldTypeEntity("Child",
                                asList(
                                    complexFieldEntity("FixedListGender",
                                        fixedListFieldTypeEntity(
                                            "FixedList-PreFix",
                                            asList(
                                                fieldTypeListItemEntity("Male.", "Male."),
                                                fieldTypeListItemEntity("Female.", "Female.")
                                            ))),
                                    complexFieldEntity("Name", fieldTypeEntity("Text", emptyList()))
                                )
                            )))
                        )
                    )))
            )
        ));
    }

    private CaseFieldEntity collectionCaseField() {
        return caseFieldEntity("schoolClasses",
            collectionFieldTypeEntity("Class-8fcabcec-327f-4b4b-99b9-9dadb8317da8",
                complexFieldTypeEntity("SchoolClass", asList(
                    complexFieldEntity("ClassName", fieldTypeEntity("Text", emptyList())),
                    complexFieldEntity("ClassAddress", addressUKFieldTypeEntity()),
                    complexFieldEntity("ClassMembers", collectionFieldTypeEntity("ClassMembers-f07e3000-a3c4-4232-ac69-586b7b013bf1",
                        complexFieldTypeEntity("Child",
                            asList(
                                complexFieldEntity("FixedListGender",
                                    fixedListFieldTypeEntity(
                                        "FixedList-PreFix",
                                        asList(
                                            fieldTypeListItemEntity("Male.", "Male."),
                                            fieldTypeListItemEntity("Female.", "Female.")
                                        ))),
                                complexFieldEntity("Name", fieldTypeEntity("Text", emptyList()))
                            )
                        )))
                    )
                )));
    }

    private static FieldTypeEntity addressUKFieldTypeEntity() {
        return fieldTypeEntity("AddressUK",
            asList(
                complexFieldEntity("AddressLine1", fieldTypeEntity("TextMax150", emptyList())),
                complexFieldEntity("AddressLine2", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("AddressLine3", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("PostTown", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("County", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("PostCode", fieldTypeEntity("TextMax14", emptyList())),
                complexFieldEntity("Country", fieldTypeEntity("TextMax50", emptyList()))
            ));
    }

    private static CaseFieldEntity caseFieldEntity(String reference, FieldTypeEntity fieldTypeEntity) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        caseFieldEntity.setFieldType(fieldTypeEntity);
        return caseFieldEntity;
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

    private static FieldTypeEntity collectionFieldTypeEntity(String reference,
                                                             FieldTypeEntity collectionFieldType) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.setBaseFieldType(fieldTypeEntity("Collection", emptyList()));
        fieldTypeEntity.setCollectionFieldType(collectionFieldType);
        return fieldTypeEntity;
    }

    private static ComplexFieldEntity complexFieldEntity(String reference, FieldTypeEntity fieldTypeEntity) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reference);
        complexFieldEntity.setFieldType(fieldTypeEntity);
        return complexFieldEntity;
    }

    private static FieldTypeEntity fixedListFieldTypeEntity(String reference,
                                                            List<FieldTypeListItemEntity> listItemEntities) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.addListItems(listItemEntities);
        return fieldTypeEntity;
    }

    private static FieldTypeListItemEntity fieldTypeListItemEntity(String label, String value) {
        FieldTypeListItemEntity fieldTypeListItemEntity = new FieldTypeListItemEntity();
        fieldTypeListItemEntity.setLabel(label);
        fieldTypeListItemEntity.setValue(value);
        return fieldTypeListItemEntity;
    }
}
