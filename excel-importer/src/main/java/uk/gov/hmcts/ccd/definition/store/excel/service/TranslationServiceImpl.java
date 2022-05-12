package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.Translation.DefinitionSheetsToTranslate;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TranslationServiceImpl implements TranslationService {

    private DefinitionSheetsToTranslate definitionSheetsToTranslate;

    @Override
    @Async
    public void processDefinitionSheets(Map<String, DefinitionSheet> definitionSheets) {
        Map<String, String> textToTranslate = retrieveTextToTranslate(definitionSheets);
        //call Translation service to PUT Dictionary
    }

    private Map<String, String> retrieveTextToTranslate(Map<String, DefinitionSheet> definitionSheets) {
        Map<SheetName, List<ColumnName>> sheetsToProcess = definitionSheetsToTranslate.generateSheetAndValues();
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
        return textToTranslate;
    }

}
