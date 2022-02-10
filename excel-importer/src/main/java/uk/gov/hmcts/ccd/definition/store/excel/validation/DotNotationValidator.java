package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
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

    public static final BiFunction<String, String, String[]> SPIT_FUNCTION =
        (expression, separator) -> Optional.ofNullable(expression)
            .map(x -> x.split(Pattern.quote(separator), -1))
            .orElse(new String[0]);

    public static final Function<String, String[]> DOT_SEPARATOR_SPLIT_FUNCTION =
        expression -> SPIT_FUNCTION.apply(expression, DOT_SEPARATOR);

    public void validate(ParseContext parseContext,
                         SheetName sheetName,
                         ColumnName columnName,
                         String caseType,
                         String expression) {

        if (!expression.contains(DOT_SEPARATOR)) {
            getTopLevelField(parseContext, sheetName, columnName, caseType, expression);
        } else {
            checkDotNotationField(parseContext, sheetName, columnName, caseType, expression);
        }
    }

    public ComplexFieldEntity findComplexFieldEntity(final Set<ComplexFieldEntity> complexFieldEntities,
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

    private void checkDotNotationField(ParseContext parseContext,
                                       SheetName sheetName,
                                       ColumnName columnName,
                                       String caseType,
                                       String expression) {
        try {
            // use split with -1 limit to force inclusion of empty values
            final String[] splitDotNotationExpression = DOT_SEPARATOR_SPLIT_FUNCTION.apply(expression);
            // NB: start from depth = 1 to ignore top level field that is already processed
            final List<String> segments = Arrays.asList(splitDotNotationExpression)
                .subList(1, splitDotNotationExpression.length);

            final FieldTypeEntity fieldType =
                getTopLevelField(parseContext, sheetName, columnName, caseType, splitDotNotationExpression[0]);

            final Set<ComplexFieldEntity> complexFieldsBelongingToParent = fieldType.getComplexFields();

            performCheck(
                segments.toArray(String[]::new),
                complexFieldsBelongingToParent,
                sheetName,
                columnName
            );
        } catch (InvalidImportException invalidImportException) {
            // throw a new Exception using original full expression (nb: previous exception already logged)
            throw new InvalidImportException(String.format(ERROR_MESSAGE, sheetName, expression, columnName));
        }
    }

    public void checkDotNotationField(final Set<ComplexFieldEntity> parentComplexFields,
                                      final SheetName sheetName,
                                      final ColumnName columnName,
                                      final String expression) {
        try {
            final String[] splitDotNotationExpression = DOT_SEPARATOR_SPLIT_FUNCTION.apply(expression);

            performCheck(splitDotNotationExpression, parentComplexFields, sheetName, columnName);
        } catch (InvalidImportException invalidImportException) {
            // throw a new Exception using original full expression (nb: previous exception already logged)
            throw new InvalidImportException(String.format(ERROR_MESSAGE, sheetName, expression, columnName));
        }
    }

    private void performCheck(final String[] splitDotNotationExpression,
                              final Set<ComplexFieldEntity> parentComplexFields,
                              final SheetName sheetName,
                              final ColumnName columnName) {
        Set<ComplexFieldEntity> complexFieldsBelongingToParent = parentComplexFields;

        for (String currentAttribute : splitDotNotationExpression) {
            final ComplexFieldEntity result = findComplexFieldEntity(
                complexFieldsBelongingToParent,
                currentAttribute,
                sheetName,
                columnName
            );

            complexFieldsBelongingToParent = result.getFieldType().getComplexFields();
        }
    }

}
