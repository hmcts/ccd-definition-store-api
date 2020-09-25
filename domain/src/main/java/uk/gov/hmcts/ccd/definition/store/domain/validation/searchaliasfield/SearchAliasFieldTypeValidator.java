package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.SearchAliasFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Component
@RequestScope
public class SearchAliasFieldTypeValidator implements SearchAliasFieldValidator {

    private final Map<String, SearchAliasFieldEntity> searchFieldAliasReferenceMap = new HashMap<>();

    private final SearchAliasFieldRepository repository;

    @Autowired
    public SearchAliasFieldTypeValidator(SearchAliasFieldRepository repository) {
        this.repository = repository;
    }

    @Override
    public ValidationResult validate(SearchAliasFieldEntity searchAliasField) {

        ValidationResult validationResult = new ValidationResult();

        if (searchAliasField.getFieldType() == null) {
            validationResult.addError(new ValidationError(
                String.format("Invalid case field '%s' for search alias ID '%s' and case type '%s'. Case field should"
                    + " point to a concrete field with full object notation in case of a complex type.",
                searchAliasField.getCaseFieldPath(),
                searchAliasField.getReference(),
                searchAliasField.getCaseType().getReference()),
                searchAliasField));
        } else {
            validateFieldTypeAgainstExistingAliases(searchAliasField, validationResult);
            validateFieldTypeAgainstNewAliases(searchAliasField, validationResult);
        }

        searchFieldAliasReferenceMap.put(searchAliasField.getReference(), searchAliasField);

        return validationResult;
    }

    private void validateFieldTypeAgainstExistingAliases(SearchAliasFieldEntity searchAliasField,
                                                         ValidationResult validationResult) {
        repository.findByReference(searchAliasField.getReference())
            .stream()
            .filter(aliasField -> areFieldTypesDifferent(aliasField, searchAliasField))
            .findFirst()
            .ifPresent(aliasField ->
                validationResult.addError(new ValidationError(
                    String.format("Invalid search alias ID '%s' for case field '%s'. This search alias ID "
                        + "has already been registered for case type '%s', case field '%s'. "
                        + "This search alias ID must be of type '%s'.",
                    searchAliasField.getReference(),
                    searchAliasField.getCaseFieldPath(),
                    aliasField.getCaseType().getReference(),
                    aliasField.getCaseFieldPath(),
                    aliasField.getFieldType().getReference()),
                    searchAliasField))
            );
    }

    private void validateFieldTypeAgainstNewAliases(SearchAliasFieldEntity searchAliasField,
                                                    ValidationResult validationResult) {
        ofNullable(searchFieldAliasReferenceMap.get(searchAliasField.getReference()))
            .filter(aliasField -> areFieldTypesDifferent(aliasField, searchAliasField))
            .ifPresent(aliasField ->
                validationResult.addError(new ValidationError(
                    String.format("Invalid search alias ID '%s' for case field '%s'. This search alias ID "
                        + "has already been defined for case type '%s', case field '%s'. "
                        + "This search alias ID must point to case fields with same type.",
                    searchAliasField.getReference(),
                    searchAliasField.getCaseFieldPath(),
                    aliasField.getCaseType().getReference(),
                    aliasField.getCaseFieldPath()),
                    searchAliasField))
            );
    }

    private boolean areFieldTypesDifferent(SearchAliasFieldEntity entity1, SearchAliasFieldEntity entity2) {
        return !entity1.getFieldType().getReference().equalsIgnoreCase(entity2.getFieldType().getReference());
    }

}
