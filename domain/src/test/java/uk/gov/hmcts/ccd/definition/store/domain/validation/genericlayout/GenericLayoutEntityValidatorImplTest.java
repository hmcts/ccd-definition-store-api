package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.google.common.collect.Lists;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchResultCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketInputCaseFieldEntity;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

@DisplayName("Generic Layout Entity Validator Implementation Tests")
class GenericLayoutEntityValidatorImplTest {
    private static final String CASE_FIELD = "Case Field I";

    private GenericLayoutValidator validator;

    private CaseTypeEntity caseType;
    private CaseFieldEntity caseField;

    @BeforeEach
    void setUp() {
        validator = new GenericLayoutEntityValidatorImpl();

        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setBaseFieldType(textFieldTypeEntity());

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

    @Nested
    @DisplayName("SearchInputCaseFieldEntity validation tests")
    class SearchInputCaseFieldEntityTests {

        @ParameterizedTest
        @ArgumentsSource(EntityArgumentsProvider.class)
        void shouldValidateGoodEntity(GenericLayoutEntity entity) {
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            final ValidationResult result = validator.validate(entity, Lists.newArrayList(entity));

            assertAll(
                () -> assertThat(result.isValid(), is(true))
            );
        }

        @ParameterizedTest
        @ArgumentsSource(EntityArgumentsProvider.class)
        void shouldFailWhenCaseTypeIsEmpty(GenericLayoutEntity entity) {
            entity.setLabel("Label");
            entity.setCaseField(caseField);
            final ValidationResult result = validator.validate(entity, Lists.newArrayList(entity));

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
            final ValidationResult result = validator.validate(entity, Lists.newArrayList(entity));

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
            final ValidationResult result = validator.validate(entity, Lists.newArrayList(entity));

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
    }

    private static FieldTypeEntity textFieldTypeEntity() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference("Text");
        fieldTypeEntity.addComplexFields(emptyList());
        return fieldTypeEntity;
    }
}
