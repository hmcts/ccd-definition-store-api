package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class SearchPartyValidator {

    private static final String COLLECTION_FIELD_TYPE = "Collection";
    private static final String ERROR_MESSAGE = "SearchPartyTab Invalid value '%s' "
        + "is not a valid SearchPartyCollectionFieldName value "
        + "as it does not reference a collection of a complex type.";

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
                // Is this validation #validateSearchPartyCollectionFieldName() still needed?
                validateSearchPartyCollectionFieldName(parseContext, caseType, searchPartyEntity);

                validateSearchPartyCollectionFieldNameColumns(parseContext, caseType, searchPartyEntity);
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

    private ComplexFieldEntity findComplexFieldEntity(final Set<ComplexFieldEntity> complexFieldEntities,
                                                      final String attribute,
                                                      final String expression) {
        return complexFieldEntities.stream()
            .filter(complexFieldEntity -> complexFieldEntity.getReference().equals(attribute))
            .findAny()
            .orElseThrow(() -> new InvalidImportException(String.format(ERROR_MESSAGE, expression)));
    }

    private FieldTypeEntity findFieldTypeEntity(final ParseContext parseContext,
                                                final String caseType,
                                                final SearchPartyEntity searchPartyEntity) {

        final String expression = searchPartyEntity.getSearchPartyCollectionFieldName();
        final String[] segments = dotNotationValidator.dotSeparatorSplitFunction.apply(expression);

        FieldTypeEntity result = parseContext.getCaseFieldType(caseType, segments[0]);

        Set<ComplexFieldEntity> complexFields = result.getComplexFields();
        final List<String> attributes = Arrays.asList(segments)
            .subList(1, segments.length);

        for (String segment : attributes) {
            final ComplexFieldEntity complexFieldEntity = findComplexFieldEntity(complexFields, segment, expression);

            result = complexFieldEntity.getFieldType();
        }

        return result;
    }

    private void validateDataType(final Set<ComplexFieldEntity> complexFields,
                                  final String dataType,
                                  final String expression) {
        if (!COLLECTION_FIELD_TYPE.equals(dataType) || complexFields.isEmpty()) {
            throw new InvalidImportException(String.format(ERROR_MESSAGE, expression));
        }
    }

    private void validateSearchPartyCollectionFieldNameColumns(final ParseContext parseContext,
                                                               final String caseType,
                                                               final SearchPartyEntity searchPartyEntity) {
        final FieldTypeEntity fieldTypeEntity = findFieldTypeEntity(parseContext, caseType, searchPartyEntity);
        final Set<ComplexFieldEntity> complexFields = fieldTypeEntity.getCollectionFieldType().getComplexFields();
        final String dataType = fieldTypeEntity.getBaseFieldType().getReference();

        validateDataType(complexFields, dataType, searchPartyEntity.getSearchPartyCollectionFieldName());

        validateSearchPartyName(complexFields, caseType, searchPartyEntity.getSearchPartyName());
        validateSearchPartyEmailAddress(complexFields, caseType, searchPartyEntity.getSearchPartyEmailAddress());
        validateSearchPartyAddressLine1(complexFields, caseType, searchPartyEntity.getSearchPartyAddressLine1());
        validateSearchPartyPostCode(complexFields, caseType, searchPartyEntity.getSearchPartyPostCode());
        validateSearchPartyDob(complexFields, caseType, searchPartyEntity.getSearchPartyDob());
        validateSearchPartyDod(complexFields, caseType, searchPartyEntity.getSearchPartyDod());
    }

    private void validateSearchPartyName(ParseContext parseContext,
                                         String caseType,
                                         SearchPartyEntity searchPartyEntity) {

        String spName = searchPartyEntity.getSearchPartyName();

        Arrays.asList(dotNotationValidator.commaSeparatorSplitFunction.apply(spName))
            .forEach(expression ->
                validateDotNotation(parseContext, caseType, ColumnName.SEARCH_PARTY_NAME, expression)
            );
    }

    private void validateSearchPartyName(final Set<ComplexFieldEntity> complexFields,
                                         final String caseType,
                                         final String searchPartyName) {
        // split CSV of fields
        Arrays.asList(dotNotationValidator.commaSeparatorSplitFunction.apply(searchPartyName))
            .forEach(expression ->
                validateDotNotation(complexFields, caseType, ColumnName.SEARCH_PARTY_NAME, expression)
            );
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

    private void validateSearchPartyEmailAddress(final Set<ComplexFieldEntity> complexFields,
                                                 final String caseType,
                                                 final String searchPartyEmailAddress) {
        validateDotNotation(complexFields, caseType, ColumnName.SEARCH_PARTY_EMAIL_ADDRESS, searchPartyEmailAddress);
    }

    private void validateSearchPartyAddressLine1(ParseContext parseContext,
                                                 String caseType,
                                                 SearchPartyEntity searchPartyEntity) {

        String spAddressLine1 = searchPartyEntity.getSearchPartyAddressLine1();

        if (StringUtils.isNoneBlank(spAddressLine1)) {
            validateDotNotation(parseContext, caseType, ColumnName.SEARCH_PARTY_ADDRESS_LINE_1, spAddressLine1);
        }
    }

    private void validateSearchPartyAddressLine1(final Set<ComplexFieldEntity> complexFields,
                                                 final String caseType,
                                                 final String searchPartyAddressLine1) {
            validateDotNotation(
                complexFields,
                caseType,
                ColumnName.SEARCH_PARTY_ADDRESS_LINE_1,
                searchPartyAddressLine1
            );
    }

    private void validateSearchPartyPostCode(ParseContext parseContext,
                                             String caseType,
                                             SearchPartyEntity searchPartyEntity) {

        String spPostCode = searchPartyEntity.getSearchPartyPostCode();

        if (StringUtils.isNoneBlank(spPostCode)) {
            validateDotNotation(parseContext, caseType, ColumnName.SEARCH_PARTY_POST_CODE, spPostCode);
        }
    }

    private void validateSearchPartyPostCode(final Set<ComplexFieldEntity> complexFields,
                                             final String caseType,
                                             final String searchPartyPostCode) {
        validateDotNotation(
            complexFields,
            caseType,
            ColumnName.SEARCH_PARTY_POST_CODE,
            searchPartyPostCode
        );
    }

    private void validateSearchPartyDob(ParseContext parseContext,
                                        String caseType,
                                        SearchPartyEntity searchPartyEntity) {

        String searchPartyDob = searchPartyEntity.getSearchPartyDob();

        if (StringUtils.isNoneBlank(searchPartyDob)) {
            validateDotNotation(parseContext, caseType, ColumnName.SEARCH_PARTY_DOB, searchPartyDob);
        }
    }

    private void validateSearchPartyDob(final Set<ComplexFieldEntity> complexFields,
                                        final String caseType,
                                        final String searchPartyDob) {
        validateDotNotation(complexFields, caseType, ColumnName.SEARCH_PARTY_DOB, searchPartyDob);
    }

    private void validateSearchPartyDod(ParseContext parseContext,
                                        String caseType,
                                        SearchPartyEntity searchPartyEntity) {

        String searchPartyDod = searchPartyEntity.getSearchPartyDod();

        if (StringUtils.isNoneBlank(searchPartyDod)) {
            validateDotNotation(parseContext, caseType, ColumnName.SEARCH_PARTY_DOD, searchPartyDod);
        }
    }

    private void validateSearchPartyDod(Set<ComplexFieldEntity> complexFields,
                                        String caseType,
                                        String searchPartyDod) {
        validateDotNotation(complexFields, caseType, ColumnName.SEARCH_PARTY_DOD, searchPartyDod);
    }

    private void validateSearchPartyCollectionFieldName(ParseContext parseContext,
                                                        String caseType,
                                                        SearchPartyEntity searchPartyEntity) {

        String spCollectionFieldValue = searchPartyEntity.getSearchPartyCollectionFieldName();

        if (StringUtils.isNoneBlank(spCollectionFieldValue)) {
            validateDotNotation(parseContext,
                caseType,
                ColumnName.SEARCH_PARTY_COLLECTION_FIELD_NAME,
                spCollectionFieldValue
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

    private void validateDotNotation(Set<ComplexFieldEntity> complexFields,
                                     String caseType,
                                     ColumnName columnName,
                                     String expression) {
        if (StringUtils.isNoneBlank(expression.strip())) {
            dotNotationValidator.checkDotNotationField(
                complexFields,
                caseType,
                SheetName.SEARCH_PARTY,
                columnName,
                expression.strip()
            );
        }
    }

}
