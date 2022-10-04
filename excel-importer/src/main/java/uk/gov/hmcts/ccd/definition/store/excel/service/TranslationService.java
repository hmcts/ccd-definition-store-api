package uk.gov.hmcts.ccd.definition.store.excel.service;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;

import java.util.Map;

public interface TranslationService {

    void processDefinitionSheets(Map<String, DefinitionSheet> definitionSheets);
}
