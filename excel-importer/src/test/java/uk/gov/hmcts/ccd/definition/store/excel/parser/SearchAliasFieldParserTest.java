package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

class SearchAliasFieldParserTest {

    private static final String SEARCH_ALIAS_ID = "alias";
    private static final String CASE_TYPE_ID = "caseTypeA";
    private static final String CASE_FIELD_ID = "caseField";
    private static final String COMPLEX_CASE_TYPE_ID = "complexCaseType";
    private static final String COMPLEX_SEARCH_ALIAS_ID = "alias";
    private static final String COMPLEX_CASE_FIELD_ID = "company.businessAddress.addressLine1";

    @Mock
    private ParseContext parseContext;

    private SearchAliasFieldParser parser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        parser = new SearchAliasFieldParser(parseContext);
    }

    @Nested
    @DisplayName("No search alias fields defined")
    class NoSearchAliasFieldsDefined {

        @Test
        @DisplayName("should return empty list when no search alias spreadsheet is defined")
        void shouldReturnEmptyListWhenNoSpreadsheet() {
            Map<String, DefinitionSheet> definitionSheets = new HashMap<>();
            CaseTypeEntity caseType = new CaseTypeEntity();

            List<SearchAliasFieldEntity> fields = parser.parseAll(definitionSheets, caseType);
            assertThat(fields.isEmpty(), is(true));
        }

        @Test
        @DisplayName("should return empty list when search alias spreadsheet is defined with no fields for the case type")
        void shouldReturnEmptyListWhenNoFieldsDefined() {
            CaseTypeEntity caseType = new CaseTypeBuilder().withReference("caseTypeB").build();

            List<SearchAliasFieldEntity> fields = parser.parseAll(createDefinitionSheet(), caseType);
            assertThat(fields.isEmpty(), is(true));
        }

    }

    @Nested
    @DisplayName("Search alias fields defined")
    class SearchAliasFieldsDefined {

        @Nested
        @DisplayName("Case field")
        class CaseField {

            @Test
            @DisplayName("should parse search alias field for a case field")
            void shouldParseSearchAliasFieldForACaseField() {
                CaseFieldEntity caseField = newField(CASE_FIELD_ID, BASE_TEXT).build();
                when(parseContext.getCaseFieldForCaseType(CASE_TYPE_ID, CASE_FIELD_ID)).thenReturn(caseField);
                CaseTypeEntity caseType = new CaseTypeBuilder().withReference(CASE_TYPE_ID).build();

                List<SearchAliasFieldEntity> fields = parser.parseAll(createDefinitionSheet(), caseType);

                assertThat(fields.isEmpty(), is(false));
                SearchAliasFieldEntity entity = fields.get(0);
                assertThat(entity.getCaseType(), is(caseType));
                assertThat(entity.getReference(), is(SEARCH_ALIAS_ID));
                assertThat(entity.getCaseFieldPath(), is(CASE_FIELD_ID));
                assertThat(entity.getFieldType(), is(caseField.getBaseType()));
                verify(parseContext).getCaseFieldForCaseType(CASE_TYPE_ID, CASE_FIELD_ID);
            }
        }

        @Nested
        @DisplayName("Complex field")
        class ComplexField {

            private CaseTypeEntity caseType;

            @BeforeEach
            void setUp() {
                caseType = new CaseTypeBuilder().withReference(COMPLEX_CASE_TYPE_ID).build();

                CaseFieldEntity companyDetails = newField("company", "company").buildComplex();
                when(parseContext.getCaseFieldForCaseType(COMPLEX_CASE_TYPE_ID, "company")).thenReturn(companyDetails);

                FieldTypeEntity company = newType("company").addFieldToComplex("businessAddress", newType("address").buildComplex()).buildComplex();
                when(parseContext.getType("company")).thenReturn(Optional.of(company));

                FieldTypeEntity address = newType("address").addFieldToComplex("addressLine1", newType(BASE_TEXT).build()).buildComplex();
                when(parseContext.getType("address")).thenReturn(Optional.of(address));
            }

            @Test
            @DisplayName("should parse search alias field for nested complex case field")
            void shouldParseSearchAliasFieldForNestedComplexCaseField() {
                List<SearchAliasFieldEntity> fields = parser.parseAll(createDefinitionSheet(), caseType);

                assertThat(fields.isEmpty(), is(false));
                SearchAliasFieldEntity entity = fields.get(0);
                assertThat(entity.getCaseType(), is(caseType));
                assertThat(entity.getReference(), is(COMPLEX_SEARCH_ALIAS_ID));
                assertThat(entity.getCaseFieldPath(), is(COMPLEX_CASE_FIELD_ID));
                assertThat(entity.getFieldType().getReference(), is(BASE_TEXT));

                verify(parseContext).getCaseFieldForCaseType(COMPLEX_CASE_TYPE_ID, "company");
                verify(parseContext).getType("company");
                verify(parseContext).getType("address");
            }

            @Test
            @DisplayName("should set field type as null when complex type is invalid")
            void shouldThrowParsingExceptionForInvalidComplexField() {
                when(parseContext.getType("company")).thenReturn(Optional.empty());

                List<SearchAliasFieldEntity> fields = parser.parseAll(createDefinitionSheet(), caseType);

                assertThat(fields.isEmpty(), is(false));
                SearchAliasFieldEntity entity = fields.get(0);
                assertThat(entity.getFieldType(), is(nullValue()));

                verify(parseContext).getCaseFieldForCaseType(COMPLEX_CASE_TYPE_ID, "company");
                verify(parseContext, times(1)).getType(anyString());
            }

            @Test
            @DisplayName("should set field type as null when nested complex field is invalid")
            void shouldThrowParsingExceptionForInvalidNestedComplexField() {
                when(parseContext.getType("address")).thenReturn(Optional.empty());

                List<SearchAliasFieldEntity> fields = parser.parseAll(createDefinitionSheet(), caseType);

                assertThat(fields.isEmpty(), is(false));
                SearchAliasFieldEntity entity = fields.get(0);
                assertThat(entity.getFieldType(), is(nullValue()));

                verify(parseContext).getCaseFieldForCaseType(COMPLEX_CASE_TYPE_ID, "company");
                verify(parseContext, times(2)).getType(anyString());
            }
        }
    }

    private Map<String, DefinitionSheet> createDefinitionSheet() {
        DefinitionDataItem dataItem1 = new DefinitionDataItem(SheetName.SEARCH_ALIAS.getName());
        dataItem1.addAttribute(ColumnName.SEARCH_ALIAS_ID, SEARCH_ALIAS_ID);
        dataItem1.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID);
        dataItem1.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);

        DefinitionDataItem dataItem2 = new DefinitionDataItem(SheetName.SEARCH_ALIAS.getName());
        dataItem2.addAttribute(ColumnName.SEARCH_ALIAS_ID, COMPLEX_SEARCH_ALIAS_ID);
        dataItem2.addAttribute(ColumnName.CASE_FIELD_ID, COMPLEX_CASE_FIELD_ID);
        dataItem2.addAttribute(ColumnName.CASE_TYPE_ID, COMPLEX_CASE_TYPE_ID);

        DefinitionSheet sheet = new DefinitionSheet();
        sheet.addDataItem(dataItem1);
        sheet.addDataItem(dataItem2);

        Map<String, DefinitionSheet> definitionSheets = new HashMap<>();
        definitionSheets.put(SheetName.SEARCH_ALIAS.getName(), sheet);

        return definitionSheets;
    }

}
