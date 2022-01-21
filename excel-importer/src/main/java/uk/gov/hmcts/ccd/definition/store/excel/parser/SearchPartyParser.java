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
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchPartyParser {

    public List<SearchPartyEntity> parse(Map<String, DefinitionSheet> definitionSheets,
                                         ParseContext parseContext) {
        try {
            final List<DefinitionDataItem> searchParties = definitionSheets
                .get(SheetName.SEARCH_PARTY.getName()).getDataItems();
            return searchParties
                .stream()
                .map(questionItem ->
                    createSearchPartyEntity(parseContext, questionItem)).collect(Collectors.toList());
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

    public SearchPartyEntity createSearchPartyEntity(ParseContext parseContext,
                                                     DefinitionDataItem definitionDataItem) {
        SearchPartyEntity searchPartyEntity = new SearchPartyEntity();

        final String caseType = definitionDataItem.getString(ColumnName.CASE_TYPE_ID);
        CaseTypeEntity caseTypeEntity = parseContext.getCaseTypes()
            .stream()
            .filter(entity -> entity.getReference().equals(caseType))
            .findAny()
            .orElseThrow(() -> {
                String message = String.format("Case Type not found %s in column '%s' in the sheet '%s'",
                    caseType, ColumnName.CASE_TYPE_ID, SheetName.SEARCH_PARTY);
                throw new InvalidImportException(message);

            });
        searchPartyEntity.setCaseType(caseTypeEntity);

        searchPartyEntity.setSearchPartyName(definitionDataItem.getString(ColumnName.SEARCH_PARTY_NAME));
        searchPartyEntity.setLiveFrom(definitionDataItem.getDate(ColumnName.LIVE_FROM));
        searchPartyEntity.setLiveTo(definitionDataItem.getDate(ColumnName.LIVE_TO));
        searchPartyEntity.setSearchPartyEmailAddress(definitionDataItem
            .getString(ColumnName.SEARCH_PARTY_EMAIL_ADDRESS));
        searchPartyEntity.setSearchPartyAddressLine1(definitionDataItem
            .getString(ColumnName.SEARCH_PARTY_ADDRESS_LINE_1));
        searchPartyEntity.setSearchPartyPostCode(definitionDataItem.getString(ColumnName.SEARCH_PARTY_POST_CODE));
        searchPartyEntity.setSearchPartyDob(definitionDataItem.getString(ColumnName.SEARCH_PARTY_DOB));
        searchPartyEntity.setSearchPartyDod(definitionDataItem.getString(ColumnName.SEARCH_PARTY_DOD));

        return searchPartyEntity;
    }

}
