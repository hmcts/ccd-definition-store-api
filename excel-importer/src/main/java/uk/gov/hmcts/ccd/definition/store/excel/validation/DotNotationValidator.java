package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class DotNotationValidator {

    protected static final String DOT_SEPARATOR = ".";

    // format: "{SheetName}Tab Invalid value '{expression}' is not a valid {ColumnName} value.... "
    protected static final String ERROR_MESSAGE = "%sTab Invalid value '%s' is not a valid %s value. "
        + "The expression dot notation values should be valid caseTypes fields.";

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

    private Optional<ComplexFieldEntity> getComplexFieldEntity(Set<ComplexFieldEntity> complexFieldEntities,
                                                               String currentAttribute) {
        return complexFieldEntities.stream().filter(complexFieldEntity ->
            complexFieldEntity.getReference().equals(currentAttribute)).findAny();
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
            final String[] splitDotNotationExpression = expression.split(Pattern.quote(DOT_SEPARATOR), -1);

            final FieldTypeEntity fieldType =
                getTopLevelField(parseContext, sheetName, columnName, caseType, splitDotNotationExpression[0]);

            Set<ComplexFieldEntity> complexFieldsBelongingToParent = fieldType.getComplexFields();

            // NB: start from depth = 1 to ignore top level field that is already processed
            for (int depth = 1; depth < splitDotNotationExpression.length; depth++) {

                String currentAttribute = splitDotNotationExpression[depth];

                // search complexFields belonging to parent for current attribute
                final Optional<ComplexFieldEntity> result =
                    getComplexFieldEntity(complexFieldsBelongingToParent, currentAttribute);

                if (result.isEmpty()) {
                    throw new InvalidImportException(
                        String.format(ERROR_MESSAGE, sheetName, currentAttribute, columnName)
                    );
                } else {
                    // update fields list based on new field type
                    complexFieldsBelongingToParent = result.get().getFieldType().getComplexFields();
                }
            }

        } catch (InvalidImportException invalidImportException) {
            // throw a new Exception using original full expression (nb: previous exception already logged)
            throw new InvalidImportException(String.format(ERROR_MESSAGE, sheetName, expression, columnName));
        }
    }

}
