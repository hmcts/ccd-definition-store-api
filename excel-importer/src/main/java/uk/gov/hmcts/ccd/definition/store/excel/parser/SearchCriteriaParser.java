package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchCriteriaParser {

    public List<SearchCriteriaEntity> parse(Map<String, DefinitionSheet> definitionSheets,
                                            ParseContext parseContext) {
        try {
            final List<DefinitionDataItem> searchCriterias = definitionSheets
                .get(SheetName.ROLE_TO_ACCESS_PROFILES.getName()).getDataItems();
            return searchCriterias
                .stream()
                .map(questionItem ->
                    createSearchCriteriaEntity(parseContext, questionItem)).collect(Collectors.toList());
        } catch (InvalidImportException invalidImportException) {
            ValidationResult validationResult = new ValidationResult();
            validationResult.addError(new ValidationError(invalidImportException.getMessage()) {
                @Override
                public String toString() {
                    return getDefaultMessage();
                }
            });
            throw new ValidationException(validationResult);
        }
    }

    public SearchCriteriaEntity createSearchCriteriaEntity(ParseContext parseContext,
                                                           DefinitionDataItem definitionDataItem) {
        SearchCriteriaEntity searchCriteriaEntity = new SearchCriteriaEntity();

        final String caseType = definitionDataItem.getString(ColumnName.CASE_TYPE_ID);
        CaseTypeEntity caseTypeEntity = parseContext.getCaseTypes()
            .stream()
            .filter(entity -> entity.getReference().equals(caseType))
            .findAny()
            .orElseThrow(() -> {
                String message = String.format("Case Type not found %s in column '%s' in the sheet '%s'",
                    caseType, ColumnName.CASE_TYPE_ID, SheetName.SEARCH_CRITERIA);
                throw new InvalidImportException(message);

            });
        searchCriteriaEntity.setCaseType(caseTypeEntity);
        searchCriteriaEntity.setLiveFrom(definitionDataItem.getDate(ColumnName.LIVE_FROM));
        searchCriteriaEntity.setLiveTo(definitionDataItem.getDate(ColumnName.LIVE_TO));
        searchCriteriaEntity.setOtherCaseReference(definitionDataItem.getString(ColumnName.OTHER_CASE_REFERENCE));

        return searchCriteriaEntity;
    }
}
