package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Map;

public class FieldsTypeParser {

    private final ListFieldTypeParser fixedListParser;
    private final ComplexFieldTypeParser complexParser;
    private final CaseFieldTypeParser fieldParser;

    public FieldsTypeParser(ListFieldTypeParser fixedListParser,
                            ComplexFieldTypeParser complexParser,
                            CaseFieldTypeParser fieldParser) {
        this.fixedListParser = fixedListParser;
        this.complexParser = complexParser;
        this.fieldParser = fieldParser;
    }

    public ParseResult<FieldTypeEntity> parseAll(Map<String, DefinitionSheet> definitionSheets) {
        return new ParseResult<FieldTypeEntity>()
            .add(fixedListParser.parse(definitionSheets))
            .add(complexParser.parse(definitionSheets))
            .add(fieldParser.parse(definitionSheets));
    }
}

