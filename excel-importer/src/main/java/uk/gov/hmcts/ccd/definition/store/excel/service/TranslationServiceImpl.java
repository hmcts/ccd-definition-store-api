package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.definition.store.domain.exception.TranslationServiceException;
import uk.gov.hmcts.ccd.definition.store.excel.client.translation.TranslationServiceApiClient;
import uk.gov.hmcts.ccd.definition.store.excel.client.translation.Translations;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.Translation.DefinitionSheetsToTranslate;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ccd.definition.store.excel.util.Translation.DefinitionSheetsToTranslate.DEFINITION_SHEETS_TO_TRANSLATE;

@Component
@Slf4j
public class TranslationServiceImpl implements TranslationService {

    private DefinitionSheetsToTranslate definitionSheetsToTranslate;

    private final TranslationServiceApiClient translationServiceApiClient;

    private final String TRANSLATION_SERVICE_FAILED_TO_RESPOND = "Translation Service failed to respond";

    public TranslationServiceImpl(TranslationServiceApiClient translationServiceApiClient) {
        this.translationServiceApiClient = translationServiceApiClient;
    }

    @Override
    @Async
    public void processDefinitionSheets(Map<String, DefinitionSheet> definitionSheets) {
        Translations textToTranslate = retrieveTextToTranslate(definitionSheets);
        try {
            log.info("calling Translation service.. start");
            translationServiceApiClient.uploadToDictionary(textToTranslate);
            log.info("calling Translation service.. end");
        } catch (Exception e) {
            log.info("Error calling Translation service");
            throw new TranslationServiceException(TRANSLATION_SERVICE_FAILED_TO_RESPOND);
        }
    }

    private Translations retrieveTextToTranslate(Map<String, DefinitionSheet> definitionSheets) {
        Translations translations = new Translations();
        Map<SheetName, List<ColumnName>> sheetsToProcess = DEFINITION_SHEETS_TO_TRANSLATE;
        Map<String, String> textToTranslate = new HashMap<>();
        for (var sheets : sheetsToProcess.entrySet()) {
            String sheetName = sheets.getKey().getName();
            if (definitionSheets.get(sheetName) != null) {
                List<DefinitionDataItem> definitionDataItems = definitionSheets.get(sheetName).getDataItems();
                for (DefinitionDataItem dataItem : definitionDataItems) {
                    DefinitionDataItem definitionDataItem = dataItem;
                    List<ColumnName> columns = sheets.getValue();
                    for (ColumnName column : columns) {
                        if (definitionDataItem.getString(column) != null) {
                            textToTranslate.put(definitionDataItem.getString(column), "");
                        }
                    }
                }
            }
        }
        translations.setTranslations(textToTranslate);
        return translations;
    }

}
