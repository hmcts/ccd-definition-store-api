package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.AccessTypesValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AccessTypesParser {

    private final AccessTypesValidator accessTypesValidator;

    @Autowired
    public AccessTypesParser(AccessTypesValidator accessTypesValidator) {
        this.accessTypesValidator = accessTypesValidator;
    }

    public List<AccessTypeEntity> parse(final Map<String, DefinitionSheet> definitionSheets,
                                                   final ParseContext parseContext) {
        ValidationResult validationResult = new ValidationResult();
        try {

            final List<DefinitionDataItem> accessTypeRolesItems = definitionSheets
                .get(SheetName.ACCESS_TYPE_ROLES.getName())
                .getDataItems();

            final List<AccessTypeEntity> accessTypeEntities = accessTypeRolesItems
                .stream().map(accessTypeEntity ->
                    createAccessTypeEntity(parseContext, accessTypeEntity)
                ).collect(Collectors.toList());

            validationResult.merge(accessTypesValidator
                .validate(accessTypeEntities));

            if (!validationResult.isValid()) {
                throw new InvalidImportException();
            }

            return accessTypeEntities;

        } catch (InvalidImportException invalidImportException) {
            String errorMessage = invalidImportException.getMessage();
            if (StringUtils.hasLength(errorMessage)) {
                validationResult.addError(new ValidationError(invalidImportException.getMessage()) {
                    @Override
                    public String toString() {
                        return getDefaultMessage();
                    }
                });
            }
            throw new ValidationException(validationResult);
        }
    }

    private AccessTypeEntity createAccessTypeEntity(final ParseContext parseContext,
                                                    final DefinitionDataItem definitionDataItem) {
        AccessTypeEntity accessTypeEntity = new AccessTypeEntity();

        final String caseType = definitionDataItem.getString(ColumnName.CASE_TYPE_ID);
        CaseTypeEntity caseTypeEntity = parseContext.getCaseTypes()
            .stream()
            .filter(entity -> entity.getReference().equals(caseType))
            .findAny()
            .orElseThrow(() -> {
                String message = String.format("Case Type not found %s in column '%s' in the sheet '%s'",
                    caseType, ColumnName.CASE_TYPE_ID, SheetName.ACCESS_TYPE_ROLES);
                throw new InvalidImportException(message);

            });
        accessTypeEntity.setCaseTypeId(caseTypeEntity);

        accessTypeEntity.setLiveFrom(definitionDataItem.getLocalDate(ColumnName.LIVE_FROM));
        accessTypeEntity.setLiveTo(definitionDataItem.getLocalDate(ColumnName.LIVE_TO));
        accessTypeEntity.setCaseTypeId(caseTypeEntity);
        accessTypeEntity.setAccessTypeId(definitionDataItem.getString(ColumnName.ACCESS_TYPE_ID));
        accessTypeEntity.setOrganisationProfileId(definitionDataItem.getString(
            ColumnName.ORGANISATION_PROFILE_ID));
        accessTypeEntity.setAccessMandatory(
            definitionDataItem.getBooleanOrDefault(ColumnName.ACCESS_MANDATORY, false));
        accessTypeEntity.setAccessDefault(
            definitionDataItem.getBooleanOrDefault(ColumnName.ACCESS_DEFAULT, false));
        accessTypeEntity.setDisplay(definitionDataItem.getBooleanOrDefault(ColumnName.DISPLAY, false));
        accessTypeEntity.setDescription(definitionDataItem.getString(ColumnName.DESCRIPTION));
        accessTypeEntity.setHint(definitionDataItem.getString(ColumnName.HINT_TEXT));
        accessTypeEntity.setDisplayOrder(definitionDataItem.getInteger(ColumnName.DISPLAY_ORDER));

        return accessTypeEntity;
    }

}
