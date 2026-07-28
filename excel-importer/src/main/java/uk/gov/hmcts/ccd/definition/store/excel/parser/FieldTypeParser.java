package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.ReferenceUtils;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Optional;

import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COLLECTION;

public class FieldTypeParser {

    private final ParseContext parseContext;

    public FieldTypeParser(ParseContext parseContext) {
        this.parseContext = parseContext;
    }

    public ParseResult.Entry<FieldTypeEntity> parse(String fieldId, DefinitionDataItem element) {
        final ParseResult.Entry<FieldTypeEntity> resultEntry;

        final String baseTypeReference = element.getString(ColumnName.FIELD_TYPE);
        final String fieldTypeParameter = element.getString(ColumnName.FIELD_TYPE_PARAMETER);
        final String regularExpression = element.getString(ColumnName.REGULAR_EXPRESSION);
        final String min = element.getString(ColumnName.MIN);
        final String max = element.getString(ColumnName.MAX);

        final String actualTypeReference = FieldTypeUtils.isList(baseTypeReference)
            ? listTypeReference(baseTypeReference, fieldTypeParameter, element.getCaseTypeId()) : baseTypeReference;

        final Optional<FieldTypeEntity> baseFieldTypeOptional = parseContext.getType(actualTypeReference);
        final FieldTypeEntity baseFieldType = baseFieldTypeOptional.orElseThrow(() ->
            new SpreadsheetParsingException("Missing field type: " + actualTypeReference));

        final FieldTypeEntity fieldType;
        if (anyDefined(regularExpression, min, max)
            || BASE_COLLECTION.equals(baseTypeReference)) {
            // Register new type

            // TODO Optimise: Look for matching existing type rather than always create new ones

            fieldType = new FieldTypeEntity();

            fieldType.setBaseFieldType(baseFieldType);
            fieldType.setReference(FieldTypeEntity.uniqueReference(fieldId));
            fieldType.setRegularExpression(regularExpression);
            fieldType.setMinimum(min);
            fieldType.setMaximum(max);

            if ("Collection".equals(baseTypeReference)) {
                final FieldTypeEntity collectionFieldType = parseContext.getType(fieldTypeParameter).orElseThrow(() ->
                    new SpreadsheetParsingException("No type found for collection of: " + fieldTypeParameter));
                fieldType.setCollectionFieldType(collectionFieldType);
            }

            resultEntry = ParseResult.Entry.createNew(fieldType);
            parseContext.addToAllTypes(fieldType);
        } else {
            fieldType = baseFieldType;
            resultEntry = ParseResult.Entry.createExisting(fieldType);
        }

        return resultEntry;
    }

    /**
     * A list declared on the FixedLists tab against a CaseTypeID is registered under a case type scoped reference,
     * so look that up first for fields belonging to the same case type. Lists declared without a CaseTypeID remain
     * shared across the jurisdiction, hence the fall back to the plain reference.
     */
    private String listTypeReference(String baseTypeReference, String fieldTypeParameter, String caseTypeId) {
        if (StringUtils.isNotBlank(caseTypeId)) {
            final String scopedReference = ReferenceUtils.listReference(
                baseTypeReference, ReferenceUtils.caseTypeScopedListId(fieldTypeParameter, caseTypeId));
            if (parseContext.getType(scopedReference).isPresent()) {
                return scopedReference;
            }
        }
        return ReferenceUtils.listReference(baseTypeReference, fieldTypeParameter);
    }

    private boolean anyDefined(String... args) {
        for (String arg : args) {
            if (!StringUtils.isBlank(arg)) {
                return true;
            }

        }
        return false;
    }
}
