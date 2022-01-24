package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;

import java.util.Arrays;
import java.util.List;

@Component
public class SearchPartyValidator {

    protected static final String NAME_FIELD_SEPARATOR = ",";

    @Autowired
    private final DotNotationValidator dotNotationValidator;

    public SearchPartyValidator(DotNotationValidator dotNotationValidator) {
        this.dotNotationValidator = dotNotationValidator;
    }

    public void validate(final List<SearchPartyEntity> searchPartyEntities,
                         final ParseContext parseContext) {

        searchPartyEntities.forEach(searchPartyEntity -> {
            String caseType = searchPartyEntity.getCaseType().getReference();

            if (StringUtils.isNoneBlank(searchPartyEntity.getSearchPartyCollectionFieldName())) {
                validateSearchPartyCollectionFieldName(parseContext, caseType, searchPartyEntity);
            } else {
                validateSearchPartyName(parseContext, caseType, searchPartyEntity);
                validateSearchPartyEmailAddress(parseContext, caseType, searchPartyEntity);
                validateSearchPartyAddressLine1(parseContext, caseType, searchPartyEntity);
                validateSearchPartyPostCode(parseContext, caseType, searchPartyEntity);
                validateSearchPartyDob(parseContext, caseType, searchPartyEntity);
                validateSearchPartyDod(parseContext, caseType, searchPartyEntity);
            }
        });

    }

    private void validateSearchPartyName(ParseContext parseContext,
                                         String caseType,
                                         SearchPartyEntity searchPartyEntity) {

        String spName = searchPartyEntity.getSearchPartyName();

        if (StringUtils.isNoneBlank(spName)) {
            // split CSV of fields
            Arrays.asList(spName.split(NAME_FIELD_SEPARATOR))
                .forEach(expression ->
                    validateDotNotation(parseContext, caseType, ColumnName.SEARCH_PARTY_NAME, expression.trim())
                );
        }
    }

    private void validateSearchPartyEmailAddress(ParseContext parseContext,
                                                 String caseType,
                                                 SearchPartyEntity searchPartyEntity) {

        String spEmailAddress = searchPartyEntity.getSearchPartyEmailAddress();

        if (StringUtils.isNoneBlank(spEmailAddress)) {
            validateDotNotation(parseContext,
                caseType,
                ColumnName.SEARCH_PARTY_EMAIL_ADDRESS,
                spEmailAddress
            );
        }
    }

    private void validateSearchPartyAddressLine1(ParseContext parseContext,
                                                 String caseType,
                                                 SearchPartyEntity searchPartyEntity) {

        String spAddressLine1 = searchPartyEntity.getSearchPartyAddressLine1();

        if (StringUtils.isNoneBlank(spAddressLine1)) {
            validateDotNotation(parseContext, caseType, ColumnName.SEARCH_PARTY_ADDRESS_LINE_1, spAddressLine1);
        }
    }

    private void validateSearchPartyPostCode(ParseContext parseContext,
                                             String caseType,
                                             SearchPartyEntity searchPartyEntity) {

        String spPostCode = searchPartyEntity.getSearchPartyPostCode();

        if (StringUtils.isNoneBlank(spPostCode)) {
            validateDotNotation(parseContext, caseType, ColumnName.SEARCH_PARTY_POST_CODE, spPostCode);
        }
    }

    private void validateSearchPartyDob(ParseContext parseContext,
                                             String caseType,
                                             SearchPartyEntity searchPartyEntity) {

        String searchPartyDob = searchPartyEntity.getSearchPartyDob();

        if (StringUtils.isNoneBlank(searchPartyDob)) {
            validateDotNotation(parseContext, caseType, ColumnName.SEARCH_PARTY_DOB, searchPartyDob);
        }
    }

    private void validateSearchPartyDod(ParseContext parseContext,
                                        String caseType,
                                        SearchPartyEntity searchPartyEntity) {

        String searchPartyDod = searchPartyEntity.getSearchPartyDod();

        if (StringUtils.isNoneBlank(searchPartyDod)) {
            validateDotNotation(parseContext, caseType, ColumnName.SEARCH_PARTY_DOD, searchPartyDod);
        }
    }

    private void validateSearchPartyCollectionFieldName(ParseContext parseContext,
                                         String caseType,
                                         SearchPartyEntity searchPartyEntity) {

        String spCollectionFieldName = searchPartyEntity.getSearchPartyCollectionFieldName();

        if (StringUtils.isNoneBlank(spCollectionFieldName)) {
            validateDotNotation(parseContext,
                caseType,
                ColumnName.SEARCH_PARTY_COLLECTION_FIELD_NAME,
                spCollectionFieldName
            );
        }
    }

    private void validateDotNotation(ParseContext parseContext,
                                     String caseType,
                                     ColumnName columnName,
                                     String expression) {

        dotNotationValidator.validate(
            parseContext,
            SheetName.SEARCH_PARTY,
            columnName,
            caseType,
            expression
        );
    }

}
