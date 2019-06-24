package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.*;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_COMPLEX_TYPE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;

class AuthorisationComplexTypeParser implements AuthorisationParser {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorisationComplexTypeParser.class);

    private final ParseContext parseContext;
    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;
    private CaseFieldEntityUtil caseFieldEntityUtil = new CaseFieldEntityUtil();

    AuthorisationComplexTypeParser(final ParseContext parseContext, final EntityToDefinitionDataItemRegistry registry) {
        this.parseContext = parseContext;
        this.entityToDefinitionDataItemRegistry = registry;
    }

    void parseAll(final Map<String, DefinitionSheet> definitionSheets,
                  final CaseTypeEntity caseType) {
        final String caseTypeReference = caseType.getReference();
        DefinitionSheet definitionSheet = definitionSheets.get(getSheetName());
        if (definitionSheet == null) {
            throw new MapperException("No AuthorisationComplexType tab found in configuration");
        } else {
            final Map<String, List<DefinitionDataItem>> dataItemMap = definitionSheet.groupDataItemsByCaseType();
            validateCaseTypes(definitionSheets, dataItemMap);
            validateCaseFields(definitionSheets, definitionSheet, caseTypeReference);

            final List<DefinitionDataItem> dataItems = dataItemMap.get(caseTypeReference);

            if (null == dataItems) {
                LOG.warn("No data is found for case type '{} in AuthorisationComplexTypes tab", caseTypeReference);
            } else {
                LOG.debug("Parsing user roles for case type '{}': '{}' AuthorisationComplexTypes detected", caseTypeReference, dataItems.size());
                List<String> allPaths = caseFieldEntityUtil.buildDottedComplexFieldPossibilitiesIncludingParentComplexFields(caseType.getCaseFields());
                for (DefinitionDataItem definition : dataItems) {
                    final String caseFieldReference = definition.getString(CASE_FIELD_ID);
                    final String listElementCode = definition.getString(LIST_ELEMENT_CODE);
                    final CaseFieldEntity caseFieldEntity = caseType.findCaseField(caseFieldReference)
                        .orElseThrow(() -> new MapperException(String.format("Unknown CaseField '%s' in worksheet '%s'", caseFieldReference, getSheetName())));
                    if (!allPaths.contains(caseFieldReference + "." + listElementCode)) {
                        throw new MapperException(String.format("Unknown List Element Code '%s' for CaseField '%s' in worksheet '%s'", listElementCode, caseFieldReference, getSheetName()));
                    }
                    final ComplexFieldACLEntity complexFieldACLEntity = new ComplexFieldACLEntity();
                    parseUserRole(complexFieldACLEntity, definition, parseContext);
                    parseCrud(complexFieldACLEntity, definition);
                    complexFieldACLEntity.setListElementCode(listElementCode);
                    caseFieldEntity.addComplexFieldACL(complexFieldACLEntity);
                    entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(complexFieldACLEntity, definition);

                    LOG.info("Parsing complexType authorisation for case type '{}', case field '{}', complexFieldReference '{}', user role '{}', crud '{}': OK",
                        definition.getString(CASE_TYPE_ID),
                        definition.getString(CASE_FIELD_ID),
                        definition.getString(LIST_ELEMENT_CODE),
                        definition.getString(USER_ROLE),
                        definition.getString(CRUD));
                }
            }
        }
    }

    @Override
    public String getSheetName() {
        return AUTHORISATION_COMPLEX_TYPE.getName();
    }
}
