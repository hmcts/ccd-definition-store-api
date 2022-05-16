package uk.gov.hmcts.ccd.definition.store.excel.util.translation;

import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_EVENT;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_EVENT_TO_COMPLEX_TYPES;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_EVENT_TO_FIELDS;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CHALLENGE_QUESTION_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.COMPLEX_TYPES;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.FIXED_LISTS;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.JURISDICTION;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.SEARCH_CASES_RESULT_FIELDS;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.SEARCH_INPUT_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.SEARCH_RESULT_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.STATE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_INPUT_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_RESULT_FIELDS;

public class DefinitionSheetsToTranslate {

    private DefinitionSheetsToTranslate() {
    }

    public static final Map<SheetName, List<ColumnName>>  DEFINITION_SHEETS_TO_TRANSLATE;

    static {
        DEFINITION_SHEETS_TO_TRANSLATE = Collections.unmodifiableMap(generateSheetAndValues());
    }

    private static Map<SheetName, List<ColumnName>> generateSheetAndValues() {
        Map<SheetName, List<ColumnName>> definitionSheetMap = new HashMap();

        // create list for CaseEvent
        List<ColumnName> caseEventColumns = new ArrayList();
        caseEventColumns.add(ColumnName.NAME);
        caseEventColumns.add(ColumnName.DESCRIPTION);
        definitionSheetMap.put(CASE_EVENT, caseEventColumns);

        // create list for CaseEventToFields
        List<ColumnName> caseEventToFieldsColumns = new ArrayList();
        caseEventToFieldsColumns.add(ColumnName.PAGE_LABEL);
        definitionSheetMap.put(CASE_EVENT_TO_FIELDS, caseEventToFieldsColumns);

        // create list for CaseField
        List<ColumnName> caseFieldColumns = new ArrayList();
        caseFieldColumns.add(ColumnName.LABEL);
        caseFieldColumns.add(ColumnName.HINT_TEXT);
        definitionSheetMap.put(CASE_FIELD, caseFieldColumns);

        // create list for CaseType
        List<ColumnName> caseTypeColumns = new ArrayList();
        caseTypeColumns.add(ColumnName.NAME);
        caseTypeColumns.add(ColumnName.DESCRIPTION);
        definitionSheetMap.put(CASE_TYPE, caseTypeColumns);

        // create list for CaseTypeTab
        List<ColumnName> caseTypeTabColumns = new ArrayList();
        caseTypeTabColumns.add(ColumnName.TAB_LABEL);
        definitionSheetMap.put(CASE_TYPE_TAB, caseTypeTabColumns);

        //TODO verify tab name
        // create list for ChallengeQuestionTab
        List<ColumnName> challengeQuestionTabColumns = new ArrayList();
        challengeQuestionTabColumns.add(ColumnName.CHALLENGE_QUESTION_TEXT);
        definitionSheetMap.put(CHALLENGE_QUESTION_TAB, challengeQuestionTabColumns);

        // create list for ComplexTypes
        List<ColumnName> complexTypesColumns = new ArrayList();
        complexTypesColumns.add(ColumnName.ELEMENT_LABEL);
        complexTypesColumns.add(ColumnName.HINT_TEXT);
        definitionSheetMap.put(COMPLEX_TYPES, complexTypesColumns);

        // create list for eventToComplexTypes
        List<ColumnName> eventToComplexTypesColumns = new ArrayList();
        eventToComplexTypesColumns.add(ColumnName.EVENT_ELEMENT_LABEL);
        eventToComplexTypesColumns.add(ColumnName.EVENT_HINT_TEXT);
        definitionSheetMap.put(CASE_EVENT_TO_COMPLEX_TYPES, eventToComplexTypesColumns);

        // create list for FixedLists
        List<ColumnName> fixedListsColumns = new ArrayList();
        fixedListsColumns.add(ColumnName.LIST_ELEMENT);
        definitionSheetMap.put(FIXED_LISTS, fixedListsColumns);

        // create list for Jurisdiction
        List<ColumnName> jurisdictionColumns = new ArrayList();
        jurisdictionColumns.add(ColumnName.NAME);
        jurisdictionColumns.add(ColumnName.DESCRIPTION);
        definitionSheetMap.put(JURISDICTION, jurisdictionColumns);

        // create list for SearchCasesResultFields
        List<ColumnName> searchCasesResultFieldsColumns = new ArrayList();
        searchCasesResultFieldsColumns.add(ColumnName.LABEL);
        definitionSheetMap.put(SEARCH_CASES_RESULT_FIELDS, searchCasesResultFieldsColumns);

        // create list for SearchInputFields
        List<ColumnName> searchInputFieldsColumns = new ArrayList();
        searchInputFieldsColumns.add(ColumnName.LABEL);
        definitionSheetMap.put(SEARCH_INPUT_FIELD, searchInputFieldsColumns);

        // create list for SearchResultFields
        List<ColumnName> searchResultFieldsColumns = new ArrayList();
        searchResultFieldsColumns.add(ColumnName.LABEL);
        definitionSheetMap.put(SEARCH_RESULT_FIELD, searchResultFieldsColumns);

        // create list for State
        List<ColumnName> stateColumns = new ArrayList();
        stateColumns.add(ColumnName.NAME);
        stateColumns.add(ColumnName.DESCRIPTION);
        definitionSheetMap.put(STATE, stateColumns);

        // create list for WorkBasketInputFields
        List<ColumnName> workBasketInputFieldsColumns = new ArrayList();
        workBasketInputFieldsColumns.add(ColumnName.LABEL);
        definitionSheetMap.put(WORK_BASKET_INPUT_FIELD, workBasketInputFieldsColumns);

        // create list for WorkBasketResultFields
        List<ColumnName> workBasketResultFieldsColumns = new ArrayList();
        workBasketResultFieldsColumns.add(ColumnName.LABEL);
        definitionSheetMap.put(WORK_BASKET_RESULT_FIELDS, searchResultFieldsColumns);

        return definitionSheetMap;
    }
}
