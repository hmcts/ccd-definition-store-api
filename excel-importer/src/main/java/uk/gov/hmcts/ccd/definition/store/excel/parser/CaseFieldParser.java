package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.SecurityClassificationColumn;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CaseFieldParser {
    private static final Logger logger = LoggerFactory.getLogger(CaseFieldParser.class);


    private final ParseContext parseContext;
    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    public CaseFieldParser(ParseContext parseContext,
                           EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry) {
        this.parseContext = parseContext;
        this.entityToDefinitionDataItemRegistry = entityToDefinitionDataItemRegistry;
    }

    public Collection<CaseFieldEntity> parseAll(Map<String, DefinitionSheet> definitionSheets,
                                                CaseTypeEntity caseType) {
        final String caseTypeId = caseType.getReference();

        logger.debug("Parsing case fields for case type {}...", caseTypeId);

        final List<CaseFieldEntity> caseFields = Lists.newArrayList();

        final Map<String, List<DefinitionDataItem>> caseFieldItemsByCaseTypes = definitionSheets.get(
            SheetName.CASE_FIELD.getName()).groupDataItemsByCaseType();

        if (!caseFieldItemsByCaseTypes.containsKey(caseTypeId)) {
            throw new SpreadsheetParsingException(
                "At least one case field must be defined for case type: " + caseTypeId);
        }

        final List<DefinitionDataItem> caseFieldItems = caseFieldItemsByCaseTypes.get(caseTypeId);

        logger.debug("Parsing case fields for case type {}: {} case fields detected",
            caseTypeId, caseFieldItems.size());

        for (DefinitionDataItem caseFieldDefinition : caseFieldItems) {
            final String caseFieldId = caseFieldDefinition.getId();
            logger.debug("Parsing case fields for case type {}: Parsing case field {}...", caseTypeId, caseFieldId);

            final CaseFieldEntity caseField = parseCaseField(caseTypeId, caseFieldId, caseFieldDefinition);
            parseContext.registerCaseFieldForCaseType(caseTypeId, caseField);
            caseFields.add(caseField);
            entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(caseField, caseFieldDefinition);

            logger.info("Parsing case fields for case type {}: Parsing case field {}: OK", caseTypeId, caseFieldId);
        }

        logger.info("Parsing case fields for case type {}: OK: {} case fields parsed", caseTypeId, caseFields.size());

        return caseFields;
    }

    private CaseFieldEntity parseCaseField(String caseTypeId,
                                           String caseFieldId,
                                           DefinitionDataItem caseFieldDefinition) {
        final CaseFieldEntity caseField = new CaseFieldEntity();

        caseField.setReference(caseFieldId);
        caseField.setFieldType(parseContext.getCaseFieldType(caseTypeId, caseFieldId));

        SecurityClassificationColumn securityClassificationColumn = caseFieldDefinition.getSecurityClassification();
        caseField.setSecurityClassification(securityClassificationColumn.getSecurityClassification());

        caseField.setLabel(caseFieldDefinition.getString(ColumnName.LABEL));
        caseField.setHidden(caseFieldDefinition.getBoolean(ColumnName.DEFAULT_HIDDEN));
        caseField.setHint(caseFieldDefinition.getString(ColumnName.HINT_TEXT));
        caseField.setLiveFrom(caseFieldDefinition.getLocalDate(ColumnName.LIVE_FROM));
        caseField.setLiveTo(caseFieldDefinition.getLocalDate(ColumnName.LIVE_TO));
        caseField.setSearchable(caseFieldDefinition.getBooleanOrDefault(ColumnName.SEARCHABLE, true));

        return caseField;
    }

}
