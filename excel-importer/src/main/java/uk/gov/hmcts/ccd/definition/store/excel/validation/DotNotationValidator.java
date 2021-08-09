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
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Component
public class DotNotationValidator {

    private static final String DOT_SEPARATOR = ".";

    // "{SheetName}Tab Invalid value '{expression}' is not a valid {ColumnName} value.... "
    private static final String ERROR_MESSAGE = "%sTab Invalid value '%s' is not a valid %s value. "
        + "The expression dot notation values should be valid caseTypes fields.";

    public void validate(ParseContext parseContext,
                         SheetName sheetName,
                         ColumnName columnName,
                         String currentCaseType,
                         String expression) {

        try {
            if (!expression.contains(DOT_SEPARATOR)) {
                parseContext.getCaseFieldType(currentCaseType, expression);
            } else {
                final String[] splitDotNotationExpression = expression.split(Pattern.quote(DOT_SEPARATOR));
                final FieldTypeEntity fieldType = parseContext.getCaseFieldType(
                    currentCaseType,
                    splitDotNotationExpression[0]);
                final String[] attributesDotNotation = Arrays.copyOfRange(
                    splitDotNotationExpression, 1, splitDotNotationExpression.length);

                IntStream.range(0, attributesDotNotation.length).forEach(index -> {
                    String currentAttribute = attributesDotNotation[index];

                    validateAttributes(
                        currentAttribute,
                        fieldType.getComplexFields(),
                        attributesDotNotation,
                        index,
                        String.format(ERROR_MESSAGE, sheetName, currentAttribute, columnName));
                });
            }

        } catch (Exception spe) {
            throw new InvalidImportException(String.format(ERROR_MESSAGE, sheetName, expression, columnName));
        }
    }

    private void validateAttributes(String currentAttribute,
                                    List<ComplexFieldEntity> complexFieldACLEntity,
                                    String[] attributesDotNotation,
                                    int currentIndex,
                                    String errorMessage) {

        final Optional<ComplexFieldEntity> result = getComplexFieldEntity(complexFieldACLEntity, currentAttribute);

        if (result.isEmpty()) {
            if (currentIndex - 1 < 0) {
                throw new InvalidImportException(String.format(errorMessage, currentAttribute));
            } else {
                // it means that there is a parent component.
                final Optional<ComplexFieldEntity> parent = getComplexFieldEntity(
                    complexFieldACLEntity, attributesDotNotation[currentIndex - 1]);
                if (parent.isPresent()) {
                    final Optional<ComplexFieldEntity> attributeDefinition = getComplexFieldEntity(
                        parent.get().getFieldType().getComplexFields(), currentAttribute);

                    if (attributeDefinition.isEmpty()) {
                        throw new InvalidImportException(String.format(errorMessage, currentAttribute));
                    }
                }
            }
        }
    }

    private Optional<ComplexFieldEntity> getComplexFieldEntity(List<ComplexFieldEntity> complexFieldACLEntity,
                                                               String currentAttribute) {
        return complexFieldACLEntity.stream().filter(complexFieldACLEItem ->
            complexFieldACLEItem.getReference().equals(currentAttribute)).findAny();
    }

}
