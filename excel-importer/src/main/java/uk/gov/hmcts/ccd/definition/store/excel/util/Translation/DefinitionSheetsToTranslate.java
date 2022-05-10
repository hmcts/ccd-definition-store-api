package uk.gov.hmcts.ccd.definition.store.excel.util.Translation;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.CASE_EVENT_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.CASE_EVENT_TO_FIELDS_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.CASE_FIELD_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.CASE_TYPE_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.CASE_TYPE_TAB_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.CHALLENGE_QUESTION_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.COMPLEX_TYPES_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.EVENT_TO_COMPLEX_TYPES_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.FIXED_LISTS_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.JURISDICTION_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.SEARCH_CASES_RESULT_FIELDS_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.SEARCH_INPUT_FIELDS_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.SEARCH_RESULT_FIELDS_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.STATE_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.WORK_BASKET_INPUT_FIELDS_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.constants.Constants.WORK_BASKET_RESULT_FIELDS_TAB;

@Component
public class DefinitionSheetsToTranslate {

    public Map<String, List<ColumnName>> generateSheetAndValues() {
        Map<String, List<ColumnName>> definitionSheetMap = new HashMap();
        // create list for CaseEvent
        List<ColumnName> caseEventColumns = new ArrayList();
        caseEventColumns.add(ColumnName.NAME);
        caseEventColumns.add(ColumnName.DESCRIPTION);
        definitionSheetMap.put(CASE_EVENT_TAB, caseEventColumns);

        // create list for CaseEventToFields
        List<ColumnName> caseEventToFieldsColumns = new ArrayList();
        caseEventToFieldsColumns.add(ColumnName.PAGE_LABEL);
        definitionSheetMap.put(CASE_EVENT_TO_FIELDS_TAB, caseEventToFieldsColumns);

        // create list for CaseField
        List<ColumnName> caseFieldColumns = new ArrayList();
        caseFieldColumns.add(ColumnName.LABEL);
        caseFieldColumns.add(ColumnName.HINT_TEXT);
        definitionSheetMap.put(CASE_FIELD_TAB, caseFieldColumns);

        // create list for CaseType
        List<ColumnName> caseTypeColumns = new ArrayList();
        caseTypeColumns.add(ColumnName.NAME);
        caseTypeColumns.add(ColumnName.DESCRIPTION);
        definitionSheetMap.put(CASE_TYPE_TAB, caseTypeColumns);

        // create list for CaseTypeTab
        List<ColumnName> caseTypeTabColumns = new ArrayList();
        caseTypeTabColumns.add(ColumnName.TAB_LABEL);
        definitionSheetMap.put(CASE_TYPE_TAB_TAB, caseTypeTabColumns);

        //TODO verify tab name
        // create list for ChallengeQuestionTab
        List<ColumnName> challengeQuestionTabColumns = new ArrayList();
        challengeQuestionTabColumns.add(ColumnName.CHALLENGE_QUESTION_TEXT);
        definitionSheetMap.put(CHALLENGE_QUESTION_TAB, challengeQuestionTabColumns);

        // create list for ComplexTypes
        List<ColumnName> complexTypesColumns = new ArrayList();
        complexTypesColumns.add(ColumnName.ELEMENT_LABEL);
        complexTypesColumns.add(ColumnName.HINT_TEXT);
        definitionSheetMap.put(COMPLEX_TYPES_TAB, complexTypesColumns);

        // create list for eventToComplexTypes
        List<ColumnName> eventToComplexTypesColumns = new ArrayList();
        eventToComplexTypesColumns.add(ColumnName.EVENT_ELEMENT_LABEL);
        eventToComplexTypesColumns.add(ColumnName.EVENT_HINT_TEXT);
        definitionSheetMap.put(EVENT_TO_COMPLEX_TYPES_TAB, eventToComplexTypesColumns);

        // create list for FixedLists
        List<ColumnName> fixedListsColumns = new ArrayList();
        fixedListsColumns.add(ColumnName.LIST_ELEMENT);
        definitionSheetMap.put(FIXED_LISTS_TAB, fixedListsColumns);

        // create list for Jurisdiction
        List<ColumnName> jurisdictionColumns = new ArrayList();
        jurisdictionColumns.add(ColumnName.NAME);
        jurisdictionColumns.add(ColumnName.DESCRIPTION);
        definitionSheetMap.put(JURISDICTION_TAB, jurisdictionColumns);

        // create list for SearchCasesResultFields
        List<ColumnName> searchCasesResultFieldsColumns = new ArrayList();
        searchCasesResultFieldsColumns.add(ColumnName.LABEL);
        definitionSheetMap.put(SEARCH_CASES_RESULT_FIELDS_TAB, searchCasesResultFieldsColumns);

        // create list for SearchInputFields
        List<ColumnName> searchInputFieldsColumns = new ArrayList();
        searchInputFieldsColumns.add(ColumnName.LABEL);
        definitionSheetMap.put(SEARCH_INPUT_FIELDS_TAB, searchInputFieldsColumns);

        // create list for SearchResultFields
        List<ColumnName> searchResultFieldsColumns = new ArrayList();
        searchResultFieldsColumns.add(ColumnName.LABEL);
        definitionSheetMap.put(SEARCH_RESULT_FIELDS_TAB, searchResultFieldsColumns);

        // create list for State
        List<ColumnName> stateColumns = new ArrayList();
        stateColumns.add(ColumnName.NAME);
        stateColumns.add(ColumnName.DESCRIPTION);
        definitionSheetMap.put(STATE_TAB, stateColumns);

        // create list for WorkBasketInputFields
        List<ColumnName> workBasketInputFieldsColumns = new ArrayList();
        workBasketInputFieldsColumns.add(ColumnName.LABEL);
        definitionSheetMap.put(WORK_BASKET_INPUT_FIELDS_TAB, workBasketInputFieldsColumns);

        // create list for WorkBasketResultFields
        List<ColumnName> workBasketResultFieldsColumns = new ArrayList();
        workBasketResultFieldsColumns.add(ColumnName.LABEL);
        definitionSheetMap.put(WORK_BASKET_RESULT_FIELDS_TAB, searchResultFieldsColumns);

        return definitionSheetMap;
    }
}
