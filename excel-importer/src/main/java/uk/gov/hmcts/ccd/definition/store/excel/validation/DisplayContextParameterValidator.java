package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.*;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.*;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.*;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.*;

import java.util.*;

import static uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet.isDisplayContextParameter;

@Component
public class DisplayContextParameterValidator {

    public void validate(Map<String, DefinitionSheet> definitionSheets) {
        List<DefinitionSheet> definitionSheetList = new ArrayList<>();
        definitionSheetList.add(definitionSheets.get(SheetName.COMPLEX_TYPES.getName()));
        definitionSheetList.add(definitionSheets.get(SheetName.CASE_EVENT_TO_COMPLEX_TYPES.getName()));
        definitionSheetList.add(definitionSheets.get(SheetName.WORK_BASKET_INPUT_FIELD.getName()));
        definitionSheetList.add(definitionSheets.get(SheetName.WORK_BASKET_RESULT_FIELDS.getName()));
        definitionSheetList.add(definitionSheets.get(SheetName.SEARCH_INPUT_FIELD.getName()));
        definitionSheetList.add(definitionSheets.get(SheetName.SEARCH_RESULT_FIELD.getName()));

        definitionSheetList.forEach(definitionSheet -> {
            if (definitionSheet != null) {
                definitionSheet.getDataItems().forEach(item -> {
                    if (item.getDisplayContextParameter() != null) {
                        if (isDisplayContextParameter(item.getDisplayContextParameter(), DisplayContextParameter.DisplayContextParameterValues.LIST)
                            || isDisplayContextParameter(item.getDisplayContextParameter(), DisplayContextParameter.DisplayContextParameterValues.TABLE)) {
                            throw new InvalidImportException(item.getCaseFieldId() + " contains incorrect or invalid configuration in tab " + definitionSheet.getName());
                        }
                    }
                });
            }
        });
    }
}
