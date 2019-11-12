package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_TYPE;

class AuthorisationCaseTypeParser implements AuthorisationParser {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorisationCaseTypeParser.class);

    private final ParseContext parseContext;

    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    AuthorisationCaseTypeParser(final ParseContext parseContext, final EntityToDefinitionDataItemRegistry registry) {
        this.parseContext = parseContext;
        this.entityToDefinitionDataItemRegistry = registry;
    }

    Collection<CaseTypeACLEntity> parseAll(final Map<String, DefinitionSheet> definitionSheets,
                                           final CaseTypeEntity caseType) {

        final List<CaseTypeACLEntity> parseResults = Lists.newArrayList();

        final String caseTypeReference = caseType.getReference();
        LOG.debug("Parsing AuthorisationCaseType for case type {}...", caseTypeReference);

        DefinitionSheet definitionSheet = getDefinitionSheet(definitionSheets);

        final Map<String, List<DefinitionDataItem>> dataItemMap = definitionSheet.groupDataItemsByCaseType();

        validateCaseTypes(definitionSheets, dataItemMap);

        final List<DefinitionDataItem> dataItems = dataItemMap.get(caseTypeReference);

        if (null == dataItems) {
            LOG.warn("No row were defined for case type '{}' in AuthorisationCaseTypes tab", caseTypeReference);
        } else {
            LOG.debug("Parsing user roles for case type {}: {} AuthorisationCaseTypes detected",
                caseTypeReference, dataItems.size());

            for (DefinitionDataItem definition : dataItems) {

                final CaseTypeACLEntity entity = new CaseTypeACLEntity();
                parseUserRole(entity, definition, parseContext);
                parseCrud(entity, definition);

                parseResults.add(entity);
                entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(entity, definition);

                LOG.info("Parsing user role {} for case type {}: OK",
                    definition.getString(ColumnName.USER_ROLE), caseTypeReference);
            }

        }
        return parseResults;
    }

    @Override
    public String getSheetName() {
        return AUTHORISATION_CASE_TYPE.getName();
    }
}
