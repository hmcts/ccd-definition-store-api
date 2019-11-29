package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_FIELD;

class AuthorisationCaseFieldParser implements AuthorisationParser {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorisationCaseFieldParser.class);

    private final ParseContext parseContext;
    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    AuthorisationCaseFieldParser(final ParseContext parseContext, final EntityToDefinitionDataItemRegistry registry) {
        this.parseContext = parseContext;
        this.entityToDefinitionDataItemRegistry = registry;
    }

    Collection<CaseFieldACLEntity> parseAll(final Map<String, DefinitionSheet> definitionSheets,
                                            final CaseTypeEntity caseType,
                                            final CaseFieldEntity caseField) {
        final List<CaseFieldACLEntity> parseResults = Lists.newArrayList();

        final String caseTypeReference = caseType.getReference();
        final String caseFieldReference = caseField.getReference();
        LOG.debug("Parsing AuthorisationCaseField for case type {}, caseField {}...",
            caseTypeReference, caseFieldReference);

        DefinitionSheet definitionSheet = getDefinitionSheet(definitionSheets);

        final Map<String, List<DefinitionDataItem>> dataItemMap = definitionSheet.groupDataItemsByCaseType();
        validateCaseTypes(definitionSheets, dataItemMap);
        validateCaseFields(definitionSheets, definitionSheet, caseTypeReference);

        final List<DefinitionDataItem> dataItems = dataItemMap.get(caseTypeReference);

        if (null == dataItems) {
            LOG.warn("No data is found for case type '{} in AuthorisationCaseFields tab", caseTypeReference);
        } else {
            LOG.debug("Parsing user roles for case type '{}': '{}' AuthorisationCaseFields detected",
                caseTypeReference, dataItems.size());

            final Map<String, List<DefinitionDataItem>> collect = dataItems
                .stream()
                .collect(groupingBy(d -> d.getString(ColumnName.CASE_FIELD_ID)));

            if (null == collect.get(caseFieldReference)) {
                LOG.warn("No row is defined for case type '{}', case field '{}'", caseTypeReference, caseFieldReference);
                // and let validation handles this Exception
            } else {
                for (DefinitionDataItem definition : collect.get(caseFieldReference)) {

                    final CaseFieldACLEntity entity = new CaseFieldACLEntity();

                    parseUserRole(entity, definition, parseContext);
                    parseCrud(entity, definition);
                    parseResults.add(entity);
                    entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(entity, definition);

                    LOG.info("Parsing user role for case type '{}', case field '{}', user role '{}', crud '{}': OK",
                        caseTypeReference,
                        definition.getString(ColumnName.CASE_FIELD_ID),
                        definition.getString(ColumnName.USER_ROLE),
                        definition.getString(ColumnName.CRUD));
                }
            }
        }

        return parseResults;
    }

    @Override
    public String getSheetName() {
        return AUTHORISATION_CASE_FIELD.getName();
    }
}
