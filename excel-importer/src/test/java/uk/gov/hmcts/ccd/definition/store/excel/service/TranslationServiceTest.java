package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.client.translation.TranslationServiceApiClient;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.definition.store.excel.service.TranslationHelper.buildSheetForCaseEvent;
import static uk.gov.hmcts.ccd.definition.store.excel.service.TranslationHelper.buildSheetForCaseType;
import static uk.gov.hmcts.ccd.definition.store.excel.service.TranslationHelper.buildSheetForGenerics;
import static uk.gov.hmcts.ccd.definition.store.excel.service.TranslationHelper.getDictionaryRequest;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_EVENT;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_RESULT_FIELDS;

class TranslationServiceTest {

    private TranslationServiceImpl translationService;

    @Mock
    private TranslationServiceApiClient translationServiceApiClient;

    private Map<String, DefinitionSheet> definitionSheets;
    private DefinitionSheet definitionSheet;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        translationService = new TranslationServiceImpl(translationServiceApiClient);
        definitionSheets = new HashMap<>();
        definitionSheet = new DefinitionSheet();
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), buildSheetForGenerics(WORK_BASKET_RESULT_FIELDS));
        definitionSheets.put(CASE_TYPE.getName(), buildSheetForCaseType());
        definitionSheets.put(CASE_EVENT.getName(), buildSheetForCaseEvent());
    }

    @Test
    public void processDefinitionSheets_TranslationReturn200() {
        given(translationServiceApiClient.uploadToDictionary(getDictionaryRequest()));
        translationService.processDefinitionSheets(definitionSheets);
        verify(translationServiceApiClient, times(1)).uploadToDictionary(getDictionaryRequest());
    }

    /*@Test
    public void processDefinitionSheets_TranslationReturn400() {
        assertThrows(Exception.class, () -> translationServiceApiClient
            .uploadToDictionary(getDictionaryRequest()));
        translationService.processDefinitionSheets(definitionSheets);
        verify(translationServiceApiClient, times(1)).uploadToDictionary(getDictionaryRequest());
    }*/


}
