package uk.gov.hmcts.ccd.definition.store.excel.validation;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import uk.gov.hmcts.ccd.definition.store.domain.validation.*;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.*;
import uk.gov.hmcts.ccd.definition.store.excel.parser.*;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.*;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.*;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.*;

@Component
public class AccessTypeRolesValidator {

    private static final String ERROR_MESSAGE = "AccessTypeRoles tab Invalid ";
    private static final String NOT_VALID = " is not a valid ";

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
            String formattedMessage = String.format("Organisation Profile ID should not be null or empty in column '%s' "
                + "in the sheet '%s'", ColumnName.ORGANISATION_PROFILE_ID, SheetName.ACCESS_TYPE_ROLES);
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
