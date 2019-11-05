package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.SecurityClassificationColumn;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WebhookEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CaseTypeParser {
    private static final Logger logger = LoggerFactory.getLogger(CaseTypeParser.class);

    private final ParseContext parseContext;
    private final CaseFieldParser caseFieldParser;
    private final CaseRoleParser caseRoleParser;
    private final StateParser stateParser;
    private final EventParser eventParser;
    private final AuthorisationCaseTypeParser authorisationCaseTypeParser;
    private final AuthorisationCaseFieldParser authorisationCaseFieldParser;
    private final AuthorisationComplexTypeParser authorisationComplexTypeParser;
    private final AuthorisationCaseEventParser authorisationCaseEventParser;
    private final AuthorisationCaseStateParser authorisationCaseStateParser;
    private final MetadataCaseFieldParser metadataCaseFieldParser;
    private final SearchAliasFieldParser searchAliasFieldParser;

    public CaseTypeParser(ParseContext parseContext,
                          CaseFieldParser caseFieldParser,
                          StateParser stateParser,
                          EventParser eventParser,
                          AuthorisationCaseTypeParser authorisationParser,
                          AuthorisationCaseFieldParser authorisationCaseFieldParser,
                          AuthorisationComplexTypeParser authorisationComplexTypeParser,
                          AuthorisationCaseEventParser authorisationCaseEventParser,
                          AuthorisationCaseStateParser authorisationCaseStateParser,
                          MetadataCaseFieldParser metadataCaseFieldParser,
                          CaseRoleParser caseRoleParser,
                          SearchAliasFieldParser searchAliasFieldParser) {
        this.parseContext = parseContext;
        this.caseFieldParser = caseFieldParser;
        this.stateParser = stateParser;
        this.eventParser = eventParser;
        this.authorisationCaseTypeParser = authorisationParser;
        this.authorisationCaseFieldParser = authorisationCaseFieldParser;
        this.authorisationComplexTypeParser = authorisationComplexTypeParser;
        this.authorisationCaseEventParser = authorisationCaseEventParser;
        this.authorisationCaseStateParser = authorisationCaseStateParser;
        this.metadataCaseFieldParser = metadataCaseFieldParser;
        this.caseRoleParser = caseRoleParser;
        this.searchAliasFieldParser = searchAliasFieldParser;
    }

        public ParseResult<CaseTypeEntity> parseAll(Map<String, DefinitionSheet> definitionSheets) {
        logger.debug("Case types parsing...");

        final ParseResult<CaseTypeEntity> result = new ParseResult<>();

        final Map<String, List<DefinitionDataItem>> caseTypeItems = definitionSheets.get(SheetName.CASE_TYPE.getName()).groupDataItemsById();

        logger.debug("Case types parsing: {} case types detected", caseTypeItems.size());

        for (Map.Entry<String, List<DefinitionDataItem>> caseTypeItem : caseTypeItems.entrySet()) {
            final String caseTypeId = caseTypeItem.getKey();

            logger.debug("Case types parsing: Parsing case type {}...", caseTypeId);

            if (1 != caseTypeItem.getValue().size()) {
                throw new SpreadsheetParsingException("Multiple case type definitions for ID: " + caseTypeId);
            }

            final DefinitionDataItem caseTypeDefinition = caseTypeItem.getValue().get(0);

            final CaseTypeEntity caseType = parseCaseType(caseTypeItem, caseTypeDefinition);
            Collection<CaseRoleEntity> caseRoleEntities = caseRoleParser.parseAll(definitionSheets, caseType);
            caseType.addCaseFields(caseFieldParser.parseAll(definitionSheets, caseType))
                .addStates(stateParser.parseAll(definitionSheets, caseType))
                .addCaseFields(metadataCaseFieldParser.parseAll(caseType))
                .addEvents(eventParser.parseAll(definitionSheets, caseType))
                .addCaseRoles(caseRoleEntities)
                .addSearchAliasFields(searchAliasFieldParser.parseAll(definitionSheets, caseType));

            parseContext.registerCaseRoles(new ArrayList<>(caseRoleEntities));
            Collection<CaseTypeACLEntity> caseTypeACLEntities = authorisationCaseTypeParser.parseAll(definitionSheets, caseType);
            caseType.addCaseTypeACLEntities(caseTypeACLEntities);

            for (CaseFieldEntity caseField : caseType.getCaseFields()) {
                Collection<CaseFieldACLEntity> caseFieldACLEntities = authorisationCaseFieldParser.parseAll(definitionSheets, caseType, caseField);
                caseField.addCaseACLEntities(caseFieldACLEntities);
            }

            authorisationComplexTypeParser.parseAll(definitionSheets, caseType);


            for (EventEntity event : caseType.getEvents()) {
                Collection<EventACLEntity> eventACLEntities = authorisationCaseEventParser.parseAll(definitionSheets, caseType, event);
                event.addEventACLEntities(eventACLEntities);
            }

            for (StateEntity stateEntity : caseType.getStates()) {
                Collection<StateACLEntity> stateACLEntities = authorisationCaseStateParser.parseAll(definitionSheets, caseType, stateEntity);
                stateEntity.addStateACLEntities(stateACLEntities);
            }
            result.addNew(caseType);
            parseContext.registerCaseType(caseType);

            logger.info("Case types parsing: Parsing case type {}: OK", caseTypeId);
        }

        return result;
    }

    private CaseTypeEntity parseCaseType(Map.Entry<String, List<DefinitionDataItem>> caseTypeItem, DefinitionDataItem caseTypeDefinition) {
        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setJurisdiction(parseContext.getJurisdiction());
        caseType.setReference(caseTypeItem.getKey());
        caseType.setName(caseTypeDefinition.getString(ColumnName.NAME));
        caseType.setDescription(caseTypeDefinition.getString(ColumnName.DESCRIPTION));
        caseType.setLiveFrom(caseTypeDefinition.getLocalDate(ColumnName.LIVE_FROM));
        caseType.setLiveTo(caseTypeDefinition.getLocalDate(ColumnName.LIVE_TO));
        caseType.setPrintWebhook(parsePrintWebhook(caseTypeDefinition));

        SecurityClassificationColumn securityClassificationColumn = caseTypeDefinition.getSecurityClassification();
        caseType.setSecurityClassification(securityClassificationColumn.getSecurityClassification());

        return caseType;
    }

    private WebhookEntity parsePrintWebhook(DefinitionDataItem caseTypeDefinition) {
        WebhookEntity printWebhook = null;

        final String printUrl = caseTypeDefinition.getString(ColumnName.PRINTABLE_DOCUMENTS_URL);

        if (!StringUtils.isBlank(printUrl)) {
            // TODO Optimise: Try to re-use existing webhook
            printWebhook = new WebhookEntity();
            printWebhook.setUrl(printUrl);
        }

        return printWebhook;
    }
}
