package uk.gov.hmcts.ccd.definition.store.excel.validation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
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
        String accessProfiles = entity.getAccessProfiles();
        String caseTypeRef = entity.getCaseType().getReference();
        if (StringUtils.isEmpty(accessProfiles)) {
            throw new InvalidImportException("Access Profiles should not be null or empty");
        }

        Arrays.stream(accessProfiles.split(","))
            .forEach(accessProfile -> {
                Optional<UserRoleEntity> userRoleEntity = parseContext.getRole(caseTypeRef, accessProfile);
                if (userRoleEntity.isEmpty()) {
                    String message = String.format("Access Profile '%s' not found", accessProfile);
                    throw new InvalidImportException(message);
                }
            });
    }
}
