package uk.gov.hmcts.ccd.definition.store.excel.validation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

@Component
public class AccessProfileValidator {

    public void validate(final List<RoleToAccessProfileEntity> roleToAccessProfileEntities,
                         final ParseContext parseContext) {
        roleToAccessProfileEntities
            .stream()
            .forEach(entity -> validate(entity, parseContext));
    }

    private void validate(RoleToAccessProfileEntity entity,
                         ParseContext parseContext) {
        ValidationResult validationResult = new ValidationResult();
        String accessProfiles = entity.getAccessProfiles();
        String caseTypeRef = entity.getCaseType().getReference();
        if (StringUtils.isEmpty(accessProfiles)) {
            String message = String.format("Access Profiles should not be null or empty in column '%s' "
                    + "in the sheet '%s'", ColumnName.ACCESS_PROFILES, SheetName.ROLE_TO_ACCESS_PROFILES);
            createErrorMessage(validationResult, message);
            throw new ValidationException(validationResult);
        }

        Arrays.stream(accessProfiles.split(","))
            .forEach(accessProfile -> {
                Optional<UserRoleEntity> userRoleEntity = parseContext.getRole(caseTypeRef, accessProfile);
                if (userRoleEntity.isEmpty()) {
                    String message = String.format("Access Profile '%s' not found in column '%s' "
                        + "in the sheet '%s'",
                        accessProfile, ColumnName.ACCESS_PROFILES, SheetName.ROLE_TO_ACCESS_PROFILES);
                    createErrorMessage(validationResult, message);
                    throw new ValidationException(validationResult);
                }
            });
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
