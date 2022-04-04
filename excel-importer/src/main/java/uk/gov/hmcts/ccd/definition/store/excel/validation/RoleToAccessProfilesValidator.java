package uk.gov.hmcts.ccd.definition.store.excel.validation;

import java.util.ArrayList;
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
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;

@Component
public class RoleToAccessProfilesValidator {

    public void validate(final List<RoleToAccessProfilesEntity> roleToAccessProfileEntities,
                         final ParseContext parseContext) {

        final List<String> uniqueRoleNameCaseTypeCaseAccessCategoryList = new ArrayList<>();

        roleToAccessProfileEntities.stream()
            .forEach(entity -> {
                ValidationResult validationResult = new ValidationResult();

                validateRoleNameAndAccessProfile(validationResult, entity);

                String roleNameCaseTypeAccessCategory = entity.getRoleName() + entity.getCaseType().getReference()
                    + entity.getCaseAccessCategories();
                if (uniqueRoleNameCaseTypeCaseAccessCategoryList.contains(roleNameCaseTypeAccessCategory)) {
                    String message = String.format("RoleName and CaseAccessCategories must be unique within a case type"
                            + " in the sheet '%s'",
                        SheetName.ROLE_TO_ACCESS_PROFILES);
                    createErrorMessage(validationResult, message);
                    throw new ValidationException(validationResult);
                }
                uniqueRoleNameCaseTypeCaseAccessCategoryList.add(roleNameCaseTypeAccessCategory);
                validate(entity, parseContext, validationResult);
            });
    }

    private void validate(RoleToAccessProfilesEntity entity,
                          ParseContext parseContext,
                          ValidationResult validationResult) {

        String accessProfiles = entity.getAccessProfiles();
        String caseTypeRef = entity.getCaseType().getReference();

        Arrays.stream(accessProfiles.split(","))
            .forEach(accessProfile -> {
                Optional<AccessProfileEntity> accessProfileEntity = parseContext
                    .getAccessProfile(caseTypeRef, accessProfile);
                if (accessProfileEntity.isEmpty()) {
                    String message = String.format("Access Profile '%s' not found in column '%s' "
                            + "in the sheet '%s'",
                        accessProfile, ColumnName.ACCESS_PROFILES, SheetName.ROLE_TO_ACCESS_PROFILES);
                    createErrorMessage(validationResult, message);
                    throw new ValidationException(validationResult);
                }
            });
    }

    private void validateRoleNameAndAccessProfile(ValidationResult validationResult,
                                                  RoleToAccessProfilesEntity entity) {
        if (StringUtils.isEmpty(entity.getRoleName())) {
            String formattedMessage = String.format("Role name should not be null or empty in column '%s' "
                + "in the sheet '%s'", ColumnName.ACCESS_PROFILES, SheetName.ROLE_TO_ACCESS_PROFILES);
            createErrorMessage(validationResult, formattedMessage);
            throw new ValidationException(validationResult);
        }

        if (StringUtils.isEmpty(entity.getAccessProfiles())) {
            String formattedMessage = String.format("Access Profiles should not be null or empty in column '%s' "
                    + "in the sheet '%s'",
                ColumnName.ACCESS_PROFILES, SheetName.ROLE_TO_ACCESS_PROFILES);
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
