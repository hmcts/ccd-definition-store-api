package uk.gov.hmcts.ccd.definition.store.excel.validation;

import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.SEARCH_PARTY_ADDRESS_LINE_1;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.SEARCH_PARTY_COLLECTION_FIELD_NAME;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.SEARCH_PARTY_DOB;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.SEARCH_PARTY_DOD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.SEARCH_PARTY_EMAIL_ADDRESS;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.SEARCH_PARTY_NAME;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.SEARCH_PARTY_POST_CODE;
import static uk.gov.hmcts.ccd.definition.store.excel.validation.DotNotationValidator.SPLIT_FUNCTION;

@Component
public class SearchPartyValidator {
    protected static final String COMMA_SEPARATOR = ",";

    protected static final String COLLECTION_ERROR_MESSAGE = "SearchPartyTab Invalid value '%s' "
        + "is not a valid SearchPartyCollectionFieldName value "
        + "as it does not reference a collection of a complex type.";

    private static final Function<String, String[]> COMMA_SEPARATOR_SPLIT_FUNCTION =
        expression -> SPLIT_FUNCTION.apply(expression, COMMA_SEPARATOR);

    private final DotNotationValidator dotNotationValidator;

    @Autowired
    public SearchPartyValidator(DotNotationValidator dotNotationValidator) {
        this.dotNotationValidator = dotNotationValidator;
    }

    public void validate(final List<SearchPartyEntity> searchPartyEntities,
                         final ParseContext parseContext) {

        searchPartyEntities.forEach(searchPartyEntity -> {
            String caseType = searchPartyEntity.getCaseType().getReference();

            if (StringUtils.isNoneBlank(searchPartyEntity.getSearchPartyCollectionFieldName())) {
                validateSearchPartyCollectionFieldNameAndColumns(parseContext, caseType, searchPartyEntity);
            } else {
                validateSearchPartyName(parseContext, caseType, searchPartyEntity);
                validateDotNotation(
                    parseContext, caseType, SEARCH_PARTY_EMAIL_ADDRESS, searchPartyEntity.getSearchPartyEmailAddress()
                );
                validateDotNotation(
                    parseContext, caseType, SEARCH_PARTY_ADDRESS_LINE_1, searchPartyEntity.getSearchPartyAddressLine1()
                );
                validateDotNotation(
                    parseContext, caseType, SEARCH_PARTY_POST_CODE, searchPartyEntity.getSearchPartyPostCode()
                );
                validateDotNotation(
                    parseContext, caseType, SEARCH_PARTY_DOB, searchPartyEntity.getSearchPartyDob()
                );
                validateDotNotation(
                    parseContext, caseType, SEARCH_PARTY_DOD, searchPartyEntity.getSearchPartyDod()
                );
            }
        });

    }

    private FieldTypeEntity validateFieldIsACollectionOfComplexAndGetType(final FieldTypeEntity fieldTypeEntity,
                                                                          final String expression) {

        final FieldTypeEntity collectionFieldType = fieldTypeEntity.getCollectionFieldType();

        // NB: if complex type should have complex fields defined
        final Set<ComplexFieldEntity> complexFields = Optional.ofNullable(collectionFieldType)
            .map(FieldTypeEntity::getComplexFields)
            .orElse(Collections.emptySet());

        final String dataType = Optional.ofNullable(fieldTypeEntity.getBaseFieldType())
            .map(FieldTypeEntity::getReference)
            .orElse(null);

        // validate collection and of type complex
        if (!FieldTypeUtils.BASE_COLLECTION.equals(dataType) || complexFields.isEmpty()) {
            throw new InvalidImportException(String.format(COLLECTION_ERROR_MESSAGE, expression));
        }

        return collectionFieldType;
    }

    private void validateSearchPartyCollectionFieldNameAndColumns(final ParseContext parseContext,
                                                                  final String caseType,
                                                                  final SearchPartyEntity searchPartyEntity) {

        // nb: validate will throw exception if CollectionFieldName not found.
        final FieldTypeEntity spCollectionFieldName = validateAndFindSearchPartyCollectionFieldName(
            parseContext,
            caseType,
            searchPartyEntity
        );

        // now validate and extracct
        final FieldTypeEntity complexType = validateFieldIsACollectionOfComplexAndGetType(
            spCollectionFieldName,
            searchPartyEntity.getSearchPartyCollectionFieldName()
        );

        validateSearchPartyName(complexType, searchPartyEntity.getSearchPartyName());
        validateDotNotation(complexType, SEARCH_PARTY_EMAIL_ADDRESS, searchPartyEntity.getSearchPartyEmailAddress());
        validateDotNotation(complexType, SEARCH_PARTY_ADDRESS_LINE_1, searchPartyEntity.getSearchPartyAddressLine1());
        validateDotNotation(complexType, SEARCH_PARTY_POST_CODE, searchPartyEntity.getSearchPartyPostCode());
        validateDotNotation(complexType, SEARCH_PARTY_DOB, searchPartyEntity.getSearchPartyDob());
        validateDotNotation(complexType, SEARCH_PARTY_DOD, searchPartyEntity.getSearchPartyDod());
    }

    private void validateSearchPartyName(ParseContext parseContext,
                                         String caseType,
                                         SearchPartyEntity searchPartyEntity) {

        String spName = searchPartyEntity.getSearchPartyName();

        // split CSV of fields
        Arrays.asList(COMMA_SEPARATOR_SPLIT_FUNCTION.apply(spName))
            .forEach(expression ->
                validateDotNotation(parseContext, caseType, SEARCH_PARTY_NAME, expression)
            );
    }

    private void validateSearchPartyName(final FieldTypeEntity complexType,
                                         final String searchPartyName) {
        // split CSV of fields
        Arrays.asList(COMMA_SEPARATOR_SPLIT_FUNCTION.apply(searchPartyName))
            .forEach(expression ->
                validateDotNotation(complexType, SEARCH_PARTY_NAME, expression)
            );
    }

    private FieldTypeEntity validateAndFindSearchPartyCollectionFieldName(ParseContext parseContext,
                                                                         String caseType,
                                                                         SearchPartyEntity searchPartyEntity) {
        String spCollectionFieldName = searchPartyEntity.getSearchPartyCollectionFieldName();

        return dotNotationValidator.validateAndLoadFieldType(
                parseContext,
                SheetName.SEARCH_PARTY,
                SEARCH_PARTY_COLLECTION_FIELD_NAME,
                caseType,
                spCollectionFieldName
            );
    }

    private void validateDotNotation(ParseContext parseContext,
                                     String caseType,
                                     ColumnName columnName,
                                     String expression) {

        if (StringUtils.isNoneBlank(expression)) {
            dotNotationValidator.validate(
                parseContext,
                SheetName.SEARCH_PARTY,
                columnName,
                caseType,
                expression
            );
        }
    }

    private void validateDotNotation(FieldTypeEntity complexType,
                                     ColumnName columnName,
                                     String expression) {

        if (StringUtils.isNoneBlank(expression)) {
            dotNotationValidator.validate(
                complexType,
                SheetName.SEARCH_PARTY,
                columnName,
                expression
            );
        }
    }

}
