package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private final DefinitionSheetsToTranslate definitionSheetsToTranslate;

    public TranslationServiceImpl(DefinitionSheetsToTranslate definitionSheetsToTranslate) {
        this.definitionSheetsToTranslate = definitionSheetsToTranslate;

    }

    @Override
    public Map<String, String> processDefinitionSheets(Map<String, DefinitionSheet> definitionSheets) {
        Map<String, String> textToTranslate = new HashMap<>();
        //Retrieve definition sheets & Columns to translate
        Map<SheetName, List<ColumnName>> sheetsToProcess = definitionSheetsToTranslate.generateSheetAndValues();
        for (var sheets : sheetsToProcess.entrySet()) {
            String sheetName = sheets.getKey().getName();
            if (definitionSheets.get(sheetName) != null) {
                List<DefinitionDataItem> definitionDataItems = definitionSheets.get(sheetName).getDataItems();
                if (definitionDataItems.size() == 1) {
                    DefinitionDataItem definitionDataItem = definitionSheets.get(sheetName).getDataItems().get(0);
                    List<ColumnName> columns = sheets.getValue();
                    for (ColumnName column : columns) {
                        if (definitionDataItem.getString(column) != null) {
                            textToTranslate.put(definitionDataItem.getString(column), ":");
                        }
                    }
                }
            }
        }
        return textToTranslate;
    }

}
