package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;

import java.util.List;

@Component
public class AccessTypeRolesValidator {

    public void validate(final List<AccessTypeRolesEntity> accessTypeRolesEntities) {

        accessTypeRolesEntities.stream()
            .forEach(entity -> {
                ValidationResult validationResult = new ValidationResult();

                validateRequiredFields(validationResult, entity);

            });

    }


    private void validateRequiredFields(ValidationResult validationResult,
                                                  AccessTypeRolesEntity entity) {

        if (StringUtils.isEmpty(entity.getLiveFrom())) {
            String formattedMessage = String.format("Live From should not be null or empty in column '%s' "
                + "in the sheet '%s'", ColumnName.LIVE_FROM, SheetName.ACCESS_TYPE_ROLES);
            createErrorMessage(validationResult, formattedMessage);
            throw new ValidationException(validationResult);
        }

        if (StringUtils.isEmpty(entity.getCaseTypeId())) {
            String formattedMessage = String.format("Case Type ID should not be null or empty in column '%s' "
                + "in the sheet '%s'", ColumnName.CASE_TYPE_ID, SheetName.ACCESS_TYPE_ROLES);
            createErrorMessage(validationResult, formattedMessage);
            throw new ValidationException(validationResult);
        }

        if (StringUtils.isEmpty(entity.getAccessTypeId())) {
            String formattedMessage = String.format("Access Type ID should not be null or empty in column '%s' "
                + "in the sheet '%s'", ColumnName.ACCESS_TYPE_ID, SheetName.ACCESS_TYPE_ROLES);
            createErrorMessage(validationResult, formattedMessage);
            throw new ValidationException(validationResult);
        }

        if (StringUtils.isEmpty(entity.getOrganisationProfileId())) {
            String formattedMessage = String.format("Organisation Profile ID should not be null or empty in column '%s'"
                + " in the sheet '%s'", ColumnName.ORGANISATION_PROFILE_ID, SheetName.ACCESS_TYPE_ROLES);
            createErrorMessage(validationResult, formattedMessage);
            throw new ValidationException(validationResult);
        }

    }

    private void createErrorMessage(ValidationResult validationResult, String message) {
        validationResult.addError(new ValidationError(message) {
            @Override
            public String toString() {
                return getDefaultMessage();
            }
        });
    }

}
