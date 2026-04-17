package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

@Component
public class DotNotationValidator {

    protected static final String DOT_SEPARATOR = ".";

    // format: "{SheetName}Tab Invalid value '{expression}' is not a valid {ColumnName} value.... "
    protected static final String ERROR_MESSAGE = "%sTab Invalid value '%s' is not a valid %s value. "
        + "The expression dot notation values should be valid caseTypes fields.";

    protected static final BiFunction<String, String, String[]> SPLIT_FUNCTION =
        (expression, separator) -> Optional.ofNullable(expression)
            .map(x -> x.split(Pattern.quote(separator), -1))
            .orElse(new String[0]);

    private static final Function<String, String[]> DOT_SEPARATOR_SPLIT_FUNCTION =
        expression -> SPLIT_FUNCTION.apply(expression, DOT_SEPARATOR);

    public void validate(ParseContext parseContext,
                         SheetName sheetName,
                         ColumnName columnName,
                         String caseType,
                         String expression) {

        validateAndLoadFieldType(parseContext, sheetName, columnName, caseType, expression);
    }

    public void validate(final FieldTypeEntity parentComplexType,
                         final SheetName sheetName,
                         final ColumnName columnName,
                         final String expression) {
        try {
            final String[] splitDotNotationExpression = DOT_SEPARATOR_SPLIT_FUNCTION.apply(expression);

            // NB: find will throw exception if not found
            findDotNotationFieldType(null, parentComplexType, splitDotNotationExpression, sheetName, columnName);

        } catch (InvalidImportException invalidImportException) {
            // throw a new Exception using original full expression (nb: previous exception already logged)
            throw new InvalidImportException(String.format(ERROR_MESSAGE, sheetName, expression, columnName));
        }
    }

    public FieldTypeEntity validateAndLoadFieldType(ParseContext parseContext,
                                                    SheetName sheetName,
                                                    ColumnName columnName,
                                                    String caseType,
                                                    String expression) {

        if (!expression.contains(DOT_SEPARATOR)) {
            return getTopLevelField(parseContext, sheetName, columnName, caseType, expression);
        } else {
            return getDotNotationField(parseContext, sheetName, columnName, caseType, expression);
        }
    }

    private ComplexFieldEntity findComplexFieldEntity(final Set<ComplexFieldEntity> complexFieldEntities,
                                                     final String attribute,
                                                     final SheetName sheetName,
                                                     final ColumnName columnName) {
        return complexFieldEntities.stream()
            .filter(complexFieldEntity -> complexFieldEntity.getReference().equals(attribute))
            .findAny()
            .orElseThrow(() -> new InvalidImportException(
                    String.format(ERROR_MESSAGE, sheetName, attribute, columnName)
                )
            );
    }

    private FieldTypeEntity getTopLevelField(ParseContext parseContext,
                                             SheetName sheetName,
                                             ColumnName columnName,
                                             String caseType,
                                             String expression) {
        try {
            return parseContext.getCaseFieldType(caseType, expression);

        } catch (Exception e) {
            throw new InvalidImportException(String.format(ERROR_MESSAGE, sheetName, expression, columnName));
        }
    }

    private FieldTypeEntity getDotNotationField(ParseContext parseContext,
                                                SheetName sheetName,
                                                ColumnName columnName,
                                                String caseType,
                                                String expression) {
        try {
            final String[] splitDotNotationExpression = DOT_SEPARATOR_SPLIT_FUNCTION.apply(expression);

            final FieldTypeEntity topLevelFieldType =
                getTopLevelField(parseContext, sheetName, columnName, caseType, splitDotNotationExpression[0]);

            // NB: start from depth = 1 to ignore top level field that is already processed
            final List<String> segments = Arrays.asList(splitDotNotationExpression)
                .subList(1, splitDotNotationExpression.length);

            return findDotNotationFieldType(
                parseContext,
                topLevelFieldType,
                segments.toArray(String[]::new),
                sheetName,
                columnName
            );
        } catch (InvalidImportException invalidImportException) {
            // throw a new Exception using original full expression (nb: previous exception already logged)
            throw new InvalidImportException(String.format(ERROR_MESSAGE, sheetName, expression, columnName));
        }
    }

    private FieldTypeEntity findDotNotationFieldType(final ParseContext parseContext,
                                                     final FieldTypeEntity parentComplexFieldType,
                                                     final String[] splitDotNotationExpression,
                                                     final SheetName sheetName,
                                                     final ColumnName columnName) {
        FieldTypeEntity currentFieldType = parentComplexFieldType;

        for (int i = 0; i < splitDotNotationExpression.length; i++) {
            final String currentAttribute = splitDotNotationExpression[i];

            // DynamicList / DynamicRadioList store their selection under {value:{code,label}}.
            // These are base types with no declared ComplexField children, so the standard
            // lookup would reject the path — permit the known sub-paths explicitly.
            if (isSingleSelectDynamicList(currentFieldType)
                && "value".equals(currentAttribute)
                && i + 1 < splitDotNotationExpression.length
                && i + 1 == splitDotNotationExpression.length - 1
                && isDynamicListLeaf(splitDotNotationExpression[i + 1])) {
                return resolveTextType(parseContext);
            }

            // search complex fields to find next attribute
            final ComplexFieldEntity complexField = findComplexFieldEntity(
                currentFieldType.getComplexFields(),
                currentAttribute,
                sheetName,
                columnName
            );

            currentFieldType = complexField.getFieldType();
        }

        return currentFieldType;
    }

    private boolean isSingleSelectDynamicList(final FieldTypeEntity fieldType) {
        final String reference = fieldType.getReference();
        return FieldTypeUtils.BASE_DYNAMIC_LIST.equals(reference)
            || FieldTypeUtils.BASE_DYNAMIC_RADIO_LIST.equals(reference);
    }

    private boolean isDynamicListLeaf(final String segment) {
        return "code".equals(segment) || "label".equals(segment);
    }

    private FieldTypeEntity resolveTextType(final ParseContext parseContext) {
        if (parseContext != null) {
            final Optional<FieldTypeEntity> registered = parseContext.getType(FieldTypeUtils.BASE_TEXT);
            if (registered.isPresent()) {
                return registered.get();
            }
        }
        final FieldTypeEntity textFieldType = new FieldTypeEntity();
        textFieldType.setReference(FieldTypeUtils.BASE_TEXT);
        return textFieldType;
    }

}
