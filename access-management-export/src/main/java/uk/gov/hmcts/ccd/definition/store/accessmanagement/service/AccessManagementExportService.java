package uk.gov.hmcts.ccd.definition.store.accessmanagement.service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.event.RoleImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Authorisation;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.Pair;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.DELETE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

@Slf4j
@Component
public class AccessManagementExportService {

    private static final JsonPointer ROOT_ATTRIBUTE = JsonPointer.valueOf("");
    private static final String STATE_PREFIX = "/__state/";
    private static final String EVENT_PREFIX = "/__event/";
    private static final String FIELD_PREFIX = "/__field/";
    private static final String RESOURCE_TYPE = "CASE";
    private DefaultRoleSetupImportService defaultRoleSetupImportService;
    private Function<Authorisation, Set<Permission>> crudFunction = authorisation -> {
        Set<Permission> permissions = Sets.newHashSet();
        if (authorisation.getCreate()) {
            permissions.add(CREATE);
        }
        if (authorisation.getUpdate()) {
            permissions.add(UPDATE);
        }
        if (authorisation.getRead()) {
            permissions.add(READ);
        }
        if (authorisation.getDelete()) {
            permissions.add(DELETE);
        }
        return permissions;
    };

    @Autowired
    public AccessManagementExportService(DefaultRoleSetupImportService defaultRoleSetupImportService) {
        this.defaultRoleSetupImportService = defaultRoleSetupImportService;
    }

    public void exportToAccessManagement(DefinitionImportedEvent event) {
        event.getContent().iterator().forEachRemaining(this::addService);
    }

    private void addService(CaseTypeEntity caseTypeEntity) {
        String serviceName = caseTypeEntity.getJurisdiction().getReference();
        defaultRoleSetupImportService.addService(serviceName, caseTypeEntity.getJurisdiction().getDescription());
        String resourceName = caseTypeEntity.getReference();

        //add resource
        addResource(serviceName, RESOURCE_TYPE, resourceName);

        //add resource attributes - Case Type
        caseTypeEntity.getCaseTypeACLEntities().stream().forEach(caseTypeACLEntity -> {
            addResourceAttribute(serviceName, resourceName, ROOT_ATTRIBUTE, caseTypeACLEntity, caseTypeEntity.getSecurityClassification());
        });

        //add resource attributes - Case States
        caseTypeEntity.getStates().stream().forEach(stateEntity -> {
            JsonPointer stateAttribute = JsonPointer.valueOf(STATE_PREFIX + stateEntity.getReference());
            stateEntity.getStateACLEntities().stream().forEach(stateACLEntity -> {
                addResourceAttribute(serviceName, resourceName, stateAttribute, stateACLEntity, null);
            });
        });

        //add resource attributes - process Case Events
        caseTypeEntity.getEvents().stream().forEach(eventEntity -> {
            JsonPointer eventAttribute = JsonPointer.valueOf(EVENT_PREFIX + eventEntity.getReference());
            eventEntity.getEventACLEntities().stream().forEach(eventACLEntity -> {
                addResourceAttribute(serviceName, resourceName, eventAttribute, eventACLEntity, eventEntity.getSecurityClassification());
            });
        });

        //add resource attributes - process Case Fields
        caseTypeEntity.getCaseFields().stream().forEach(fieldEntity -> {
            JsonPointer fieldAttribute = JsonPointer.valueOf(FIELD_PREFIX + fieldEntity.getReference());
            fieldEntity.getCaseFieldACLEntities().stream().forEach(fieldACLEntity -> {
                addResourceAttribute(serviceName, resourceName, fieldAttribute, fieldACLEntity, fieldEntity.getSecurityClassification());
            });
        });

    }

    private void addResourceAttribute(String serviceName, String resourceName, JsonPointer stateAttribute, Authorisation authorisation,
                                      uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification securityClassification) {
        UserRoleEntity userRole = authorisation.getUserRole();
        addRoleFromImport(userRole);

        addClassificationAndCRUDPermissions(serviceName, RESOURCE_TYPE, resourceName,
                                            stateAttribute, authorisation, userRole, securityClassification);
    }

    private void addResource(String serviceName, String caseResourceType, String resourceName) {
        defaultRoleSetupImportService.addResourceDefinition(new ResourceDefinition(serviceName,
                                                                                   caseResourceType,
                                                                                   resourceName));
    }

    private void addClassificationAndCRUDPermissions(String serviceName, String resourceType, String resourceName, JsonPointer attributePointer,
                                                     Authorisation authorisation, UserRoleEntity userRole, uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification securityClassification) {
        defaultRoleSetupImportService.grantDefaultPermission(
            DefaultPermissionGrant.builder()
                                  .roleName(userRole.getReference())
                                  .resourceDefinition(ResourceDefinition.builder()
                                                                        .serviceName(serviceName)
                                                                        .resourceType(resourceType)
                                                                        .resourceName(resourceName)
                                                                        .build())
                                  .attributePermissions(
                                      createPermissionsForAttribute(
                                          attributePointer,
                                          crudFunction.apply(authorisation),
                                          securityClassification != null ?
                                              SecurityClassification.valueOf(securityClassification.name()) : SecurityClassification.NONE))
                                  .build());
    }

    private void addRoleFromImport(UserRoleEntity userRole) {
        defaultRoleSetupImportService.addRole(userRole.getReference(),
                                              RoleType.IDAM,
                                              SecurityClassification.valueOf(userRole.getSecurityClassification().name()),
                                              AccessType.ROLE_BASED);
    }

    private static Map<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>> createPermissionsForAttribute(JsonPointer attribute, Set<Permission> permissions, SecurityClassification securityClassification) {
        Map.Entry<Set<Permission>, SecurityClassification> pair = new Pair<>(permissions, securityClassification);

        return ImmutableMap.of(attribute, pair);
    }

    public void exportRoleToAccessManagement(RoleImportedEvent event) {
        addRoleFromEvent(event.getContent());
    }

    private void addRoleFromEvent(UserRoleEntity userRoleEntity) {
        defaultRoleSetupImportService.addRole(userRoleEntity.getName(), RoleType.RESOURCE,
                                              SecurityClassification.valueOf(userRoleEntity.getSecurityClassification().name()), AccessType.ROLE_BASED);
    }
}
