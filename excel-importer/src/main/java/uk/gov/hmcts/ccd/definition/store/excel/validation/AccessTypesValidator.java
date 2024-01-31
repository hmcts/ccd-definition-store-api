package uk.gov.hmcts.ccd.definition.store.excel.validation;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Component
public class AccessTypesValidator {

    public ValidationResult validate(final List<AccessTypeEntity> accessTypeEntities) {

        ValidationResult validationResult = new ValidationResult();

        accessTypeEntities
            .forEach(entity -> {
                validateRequiredFields(validationResult, entity, accessTypeEntities);
                validateDisplay(validationResult, entity, accessTypeEntities);
            });

        return validationResult;
    }

    private void validateRequiredFields(ValidationResult validationResult, AccessTypeEntity entity,
                                        List<AccessTypeEntity> accessTypeEntities) {

        if (!StringUtils.hasLength(entity.getAccessTypeId())) {
            validationResult.addError(new ValidationError(
                String.format("Access Type ID should not be null or empty in column '%s' in the sheet '%s'",
                    ColumnName.ACCESS_TYPE_ID, SheetName.ACCESS_TYPE)) {
            });
        }

        if (!StringUtils.hasLength(entity.getOrganisationProfileId())) {
            validationResult.addError(new ValidationError(
                String.format("Organisation Profile ID should not be null or empty in column '%s' "
                    + "in the sheet '%s'", ColumnName.ORGANISATION_PROFILE_ID, SheetName.ACCESS_TYPE)) {
            });
        }

        if (StringUtils.hasLength(entity.getAccessTypeId())
            && StringUtils.hasLength(entity.getOrganisationProfileId())) {
            validateAccessTypeIdAndOrgProfileIdIsUniqueForCaseTypeAndJurisdiction(validationResult, accessTypeEntities);
        }
    }

    private void validateAccessTypeIdAndOrgProfileIdIsUniqueForCaseTypeAndJurisdiction(
        ValidationResult validationResult, List<AccessTypeEntity> accessTypeEntities) {

        Map<Object, List<AccessTypeEntity>> uniqueRecords = accessTypeEntities.stream()
            .collect(groupingBy(accessTypeEntity -> new AccessTypeEntity.uniqueIdentifier(
            accessTypeEntity.getCaseTypeId().getReference(),
            accessTypeEntity.getCaseTypeId().getJurisdiction().getReference(),
            accessTypeEntity.getAccessTypeId(),
            accessTypeEntity.getOrganisationProfileId()
        )));

        if (uniqueRecords.size() != accessTypeEntities.size()) {
            String errorMessage = String.format(
                "'%s' in combination with the '%s' and '%s', must be unique within the Jurisdiction.  "
                    + "Therefore, if a service requires the same Access Type and Organisation Profile to "
                    + "apply for several Case Types in the same Jursidiction, the configuration needs to be "
                    + "repeated for each reqired case type. in the sheet '%s'",
                ColumnName.ACCESS_TYPE_ID, ColumnName.CASE_TYPE_ID, ColumnName.ORGANISATION_PROFILE_ID,
                SheetName.ACCESS_TYPE_ROLE);

            if (!alreadyReportedError(validationResult, errorMessage)) {
                validationResult.addError(new ValidationError(errorMessage) {});
            }
        }
    }

    private void validateDisplay(ValidationResult validationResult, AccessTypeEntity entity,
                                 List<AccessTypeEntity> accessTypeEntities) {

        if (entity.getDisplay()) {
            if (!entity.getAccessMandatory() || !entity.getAccessDefault()) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' and '%s' must be set to true for '%s' to be used in the sheet '%s'",
                        ColumnName.ACCESS_MANDATORY, ColumnName.ACCESS_DEFAULT,
                        ColumnName.DISPLAY, SheetName.ACCESS_TYPE)) {
                });
            }

            if (!StringUtils.hasLength(entity.getDescription())) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' must be set for '%s' to be used in the sheet '%s'",
                        ColumnName.DESCRIPTION, ColumnName.DISPLAY, SheetName.ACCESS_TYPE)) {
                });
            }

            if (!StringUtils.hasLength(entity.getHint())) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' must be set for '%s' to be used in the sheet '%s'",
                        ColumnName.HINT_TEXT, ColumnName.DISPLAY, SheetName.ACCESS_TYPE)) {
                });
            }

            if (entity.getDisplayOrder() == null) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' should not be null or empty for '%s' to be used in column '%s' "
                            + "in the sheet '%s'", ColumnName.DISPLAY_ORDER, ColumnName.DISPLAY,
                        ColumnName.DISPLAY_ORDER, SheetName.ACCESS_TYPE)) {
                });

            } else if (entity.getDisplayOrder() < 1) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' must be greater than 0 in column '%s' in the sheet '%s'",
                        ColumnName.DISPLAY_ORDER, ColumnName.DISPLAY_ORDER, SheetName.ACCESS_TYPE)) {
                });
            } else {
                validateDisplayOrder(validationResult, accessTypeEntities);
            }
        }
    }

    private void validateDisplayOrder(ValidationResult validationResult,
                                      List<AccessTypeEntity> accessTypeEntities) {

        Map<Triple<CaseTypeEntity, Integer, Integer>, List<AccessTypeEntity>> accessTypeRolesDisplayOrder =
            accessTypeEntities
                .stream()
                .collect(groupingBy(p ->
                    Triple.of(p.getCaseTypeId(),
                        p.getCaseTypeId().getJurisdiction().getId(),
                        p.getDisplayOrder())));

        accessTypeRolesDisplayOrder.keySet()
            .forEach(triple -> {
                if (accessTypeRolesDisplayOrder.get(triple).size() > 1) {
                    String errorMessage = String.format("'%s' must be unique across all Case Types for "
                            + "a given Jurisdiction in the sheet '%s'",
                        ColumnName.DISPLAY_ORDER, SheetName.ACCESS_TYPE);

                    if (!alreadyReportedError(validationResult, errorMessage)) {
                        validationResult.addError(new ValidationError(errorMessage) {});
                    }
                }
            });
    }

    private boolean alreadyReportedError(ValidationResult validationResult, String errorMessage) {
        return validationResult.getValidationErrors().stream()
            .map(ValidationError::getDefaultMessage).anyMatch(errorMessage::contains);
    }
}
