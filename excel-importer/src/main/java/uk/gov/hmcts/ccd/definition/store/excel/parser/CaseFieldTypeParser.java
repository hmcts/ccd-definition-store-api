package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;
import java.util.Map;

/**
 * Parses Field types defined as part of tab `CaseField`.
 */
public class CaseFieldTypeParser {

    private static final Logger logger = LoggerFactory.getLogger(CaseFieldTypeParser.class);

    private ParseContext parseContext;
    private final FieldTypeParser fieldTypeParser;

    public CaseFieldTypeParser(ParseContext parseContext, FieldTypeParser fieldTypeParser) {
        this.parseContext = parseContext;
        this.fieldTypeParser = fieldTypeParser;
    }

    public ParseResult<FieldTypeEntity> parse(Map<String, DefinitionSheet> definitionSheets) {
        logger.debug("Case field types parsing...");

        final ParseResult<FieldTypeEntity> result = new ParseResult<>();

        final List<DefinitionDataItem> caseFields = definitionSheets.get(SheetName.CASE_FIELD.getName()).getDataItems();

        logger.debug("Case field types parsing: {} fields detected", caseFields.size());

        for (DefinitionDataItem caseField : caseFields) {
            // TODO Check for already existing types with same identity
            final String caseTypeId = caseField.getString(ColumnName.CASE_TYPE_ID);
            final String fieldId = caseField.getString(ColumnName.ID);

            final ParseResult.Entry<FieldTypeEntity> resultEntry = fieldTypeParser.parse(fieldId, caseField);

            parseContext.registerCaseFieldType(caseTypeId, fieldId, resultEntry.getValue());

            result.add(resultEntry);
        }

        logger.info("Case field types parsing: OK: {} types parsed", result.getAllResults().size());

        return result;
    }
}
