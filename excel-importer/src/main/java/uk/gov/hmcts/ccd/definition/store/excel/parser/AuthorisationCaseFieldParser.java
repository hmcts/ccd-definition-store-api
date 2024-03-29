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

    void parseAndSetACLEntities(final Map<String, DefinitionSheet> definitionSheets,
                                final CaseTypeEntity caseType,
                                final Collection<CaseFieldEntity> caseFields) {
        DefinitionSheet definitionSheet = getDefinitionSheet(definitionSheets);
        final Map<String, List<DefinitionDataItem>> dataItemMap = definitionSheet.groupDataItemsByCaseType();
        validateCaseTypes(definitionSheets, dataItemMap);
        final String caseTypeReference = caseType.getReference();
        validateCaseFields(definitionSheets, definitionSheet, caseTypeReference);

        final List<DefinitionDataItem> dataItems = dataItemMap.getOrDefault(caseTypeReference, List.of());
        final Map<String, List<DefinitionDataItem>> collect =
            dataItems.stream().collect(groupingBy(d -> d.getString(ColumnName.CASE_FIELD_ID)));

        for (CaseFieldEntity caseField : caseFields) {
            final List<CaseFieldACLEntity> parseResults = Lists.newArrayList();
            final String caseFieldReference = caseField.getReference();

            LOG.debug("Parsing AuthorisationCaseField for case type {}, caseField {}...",
                caseTypeReference, caseFieldReference);

            if (dataItems.isEmpty()) {
                LOG.warn("No data is found for case type '{}' in AuthorisationCaseFields tab", caseTypeReference);
            } else {
                LOG.debug("Parsing access profiles for case type '{}': '{}' AuthorisationCaseFields detected",
                    caseTypeReference, dataItems.size());


                List<DefinitionDataItem> definitionDataItems = collect.get(caseFieldReference);

                if (null == definitionDataItems || definitionDataItems.isEmpty()) {
                    LOG.warn("No row is defined for case type '{}', case field '{}'",
                        caseTypeReference, caseFieldReference);
                    // and let validation handles this Exception
                } else {
                    for (DefinitionDataItem definition : definitionDataItems) {

                        final CaseFieldACLEntity entity = new CaseFieldACLEntity();

                        parseAccessProfile(entity, definition, parseContext);
                        parseCrud(entity, definition);
                        parseResults.add(entity);
                        entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(entity, definition);

                        LOG.info(
                            "Parsing access profile for case type '{}', case field '{}', access profile '{}', "
                                + "crud '{}': OK",
                            caseTypeReference,
                            definition.getString(ColumnName.CASE_FIELD_ID),
                            definition.getString(ColumnName.ACCESS_PROFILE),
                            definition.getString(ColumnName.CRUD));
                    }
                }
            }

            caseField.addCaseACLEntities(parseResults);
        }
    }

    @Override
    public String getSheetName() {
        return AUTHORISATION_CASE_FIELD.getName();
    }
}
