package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.usecase.*;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.*;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.*;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.*;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.*;

public class UseCaseParser {

    private final ParseContext parseContext;

    public UseCaseParser(ParseContext parseContext) {
        this.parseContext = parseContext;
    }

    private static final String ERROR_MESSAGE_UNSUPPORTED_PARAMETER_TYPE =
        "Unsupported useCase parameter type '%s' for field '%s' on tab '%s'";

    public void parse(Map<String, DefinitionSheet> definitionSheets) {
        DefinitionSheet definitionSheet = definitionSheets.get(SheetName.SEARCH_CASES_RESULT_FIELDS.getName());
        if (definitionSheet != null) {
            final String sheetName = definitionSheet.getName();
            definitionSheet.getDataItems().forEach(item -> {
                if (!item.getUseCase().equals(UseCaseType.ORGCASES.toString())) {
                    throw new MapperException(String.format(ERROR_MESSAGE_UNSUPPORTED_PARAMETER_TYPE,
                        item.getUseCase(), item.getCaseFieldId(), sheetName));

                }
            });
        }
    }
}
