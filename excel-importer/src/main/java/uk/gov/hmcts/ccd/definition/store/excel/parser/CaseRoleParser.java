package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;

public class CaseRoleParser {
    private static final Logger logger = LoggerFactory.getLogger(CaseRoleParser.class);

    private final ParseContext parseContext;

    public CaseRoleParser(ParseContext parseContext) {
        this.parseContext = parseContext;
    }

    public Collection<CaseRoleEntity> parseAll(Map<String, DefinitionSheet> definitionSheets, CaseTypeEntity caseType) {
        final String caseTypeId = caseType.getReference();

        logger.debug("Parsing case roles for case type '{}'...", caseTypeId);

        final List<CaseRoleEntity> caseRoleEntities = Lists.newArrayList();

        final DefinitionSheet definitionSheet = definitionSheets.get(SheetName.CASE_ROLE.getName());

        if (definitionSheet == null) {
            logger.debug("Worksheet 'CaseRoles' not found in the workbook...");
            return caseRoleEntities;
        }

        final Map<String, List<DefinitionDataItem>> caseRoleItemsByCaseTypes = definitionSheet
            .groupDataItemsByCaseType();

        final List<DefinitionDataItem> caseRoles = caseRoleItemsByCaseTypes.get(caseTypeId);
        if (caseRoles == null) {
            logger.debug("No case roles found for case type '{}'", caseTypeId);
            return caseRoleEntities;
        }
        logger.debug("Parsing case roles for case type '{}': {} case roles detected", caseTypeId, caseRoles.size());

        for (DefinitionDataItem caseRolesDefinition : caseRoles) {
            final String caseRoleId = caseRolesDefinition.getId();
            logger.debug("Parsing case roles for case type '{}': Parsing case role '{}'...", caseTypeId, caseRoleId);

            final CaseRoleEntity caseRoleEntity = parseCaseRole(caseRoleId, caseRolesDefinition);
            parseContext.registerCaseRoleForCaseType(caseTypeId, caseRoleEntity);
            caseRoleEntities.add(caseRoleEntity);

            logger.info("Parsing case roles for case type '{}': Parsing case role '{}': OK", caseTypeId, caseRoleId);
        }

        logger.info("Parsing case roles for case type '{}': OK: {} case roles parsed",
            caseTypeId, caseRoleEntities.size());

        return caseRoleEntities;
    }

    private CaseRoleEntity parseCaseRole(String caseRoleId, DefinitionDataItem stateDefinition) {
        final CaseRoleEntity caseRoleEntity = new CaseRoleEntity();

        caseRoleEntity.setReference(caseRoleId);
        caseRoleEntity.setName(stateDefinition.getString(ColumnName.NAME));
        caseRoleEntity.setDescription(stateDefinition.getString(ColumnName.DESCRIPTION));
        caseRoleEntity.setSecurityClassification(PUBLIC);

        return caseRoleEntity;
    }
}
