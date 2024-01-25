package uk.gov.hmcts.ccd.definition.store.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeField;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRoleField;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRoleResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.OrganisationProfileIds;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesRoleResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResults;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccessTypeRolesService {

    private static final Logger LOG = LoggerFactory.getLogger(AccessTypeRolesService.class);

    private EntityToResponseDTOMapper entityToResponseDTOMapper;
    private AccessTypesRepository accessTypesRepository;
    private AccessTypeRolesRepository accessTypeRolesRepository;

    public AccessTypeRolesService(EntityToResponseDTOMapper entityToResponseDTOMapper,
                                  AccessTypesRepository accessTypesRepository,
                                  AccessTypeRolesRepository accessTypeRolesRepository) {
        this.entityToResponseDTOMapper = entityToResponseDTOMapper;
        this.accessTypesRepository = accessTypesRepository;
        this.accessTypeRolesRepository = accessTypeRolesRepository;

    }

    public AccessTypeRolesJurisdictionResults retrieveAccessTypeRoles(OrganisationProfileIds organisationProfileIds) {
        return getAccessTypeRolesJurisdictionResults(organisationProfileIds);
    }

    private AccessTypeRolesJurisdictionResults getAccessTypeRolesJurisdictionResults(
        OrganisationProfileIds organisationProfileIds) {
        List<AccessTypeField> accessTypes;
        List<AccessTypeRoleField> accessTypeRoles;

        if (organisationProfileIds != null
            && organisationProfileIds.getOrganisationProfileIds() != null
            && !organisationProfileIds.getOrganisationProfileIds().isEmpty()) {

            //Get only access_types with caseTypeId's by OrganisationProfileIds
            List<AccessTypeEntity> accessTypeEntities = accessTypesRepository
                .findByOrganisationProfileIds(organisationProfileIds.getOrganisationProfileIds());
            accessTypes = accessTypeEntities.stream().map(entityToResponseDTOMapper::map)
                .collect(Collectors.toList());

            List<AccessTypeRoleEntity> accessTypeRoleEntities = accessTypeRolesRepository
                .findByOrganisationProfileIds(organisationProfileIds.getOrganisationProfileIds());
            accessTypeRoles = accessTypeRoleEntities.stream().map(entityToResponseDTOMapper::map)
                .collect(Collectors.toList());
        } else {
            // get all access types
            List<AccessTypeEntity> accessTypeEntities = accessTypesRepository
                .findAllWithCaseTypeIds();
            accessTypes = accessTypeEntities.stream().map(entityToResponseDTOMapper::map)
                .collect(Collectors.toList());

            //get all access type roles
            List<AccessTypeRoleEntity> accessTypeRolesEntities = accessTypeRolesRepository
                .findAllWithCaseTypeIds();
            accessTypeRoles = accessTypeRolesEntities.stream().map(entityToResponseDTOMapper::map)
                .collect(Collectors.toList());
        }

        // Build the json output from the data
        List<AccessTypeRolesJurisdictionResult> jurisdictions = buildJurisdictionJsonResult(
            accessTypes, accessTypeRoles);

        AccessTypeRolesJurisdictionResults jurisdictionResults = new AccessTypeRolesJurisdictionResults();
        jurisdictionResults.setJurisdictions(jurisdictions);
        return jurisdictionResults;
    }


    private List<AccessTypeRolesJurisdictionResult> buildJurisdictionJsonResult(
        List<AccessTypeField> accessTypes,
        List<AccessTypeRoleField> accessTypeRoles) {
        List<AccessTypeRolesJurisdictionResult> jurisdictions = new ArrayList<>();

        for (AccessTypeRoleField accessTypeRole : accessTypeRoles) {

            Optional<AccessTypeRolesJurisdictionResult> existingJurisdiction = jurisdictions.stream()
                .filter(jurisdiction -> accessTypeRole.getCaseTypeId().getJurisdiction().getReference()
                    .equals(jurisdiction.getId()))
                .findAny();

            Optional<AccessTypeField> matchingAccessType = accessTypes.stream()
                .filter(accessType -> accessType.getAccessTypeId()
                    .equals(accessTypeRole.getAccessTypeId()))
                .findAny();

            if (existingJurisdiction.isPresent()) {
                existingJurisdiction.get().getAccessTypeRoles().addAll(getRoleJsonResults(
                    accessTypeRole, matchingAccessType));

            } else {
                AccessTypeRolesJurisdictionResult jurisdictionResult = new AccessTypeRolesJurisdictionResult();

                CaseTypeEntity caseTypeEntity = accessTypeRole.getCaseTypeId();
                JurisdictionEntity jurisdictionEntity = caseTypeEntity.getJurisdiction();
                jurisdictionResult.setId(jurisdictionEntity.getReference());
                jurisdictionResult.setName(jurisdictionEntity.getName());

                List<AccessTypeRoleResult> accessTypeRolesResults = getRoleJsonResults(
                    accessTypeRole, matchingAccessType);
                jurisdictionResult.setAccessTypeRoles(accessTypeRolesResults);

                jurisdictions.add(jurisdictionResult);
            }
        }
        return jurisdictions;
    }

    private List<AccessTypeRoleResult> getRoleJsonResults(AccessTypeRoleField accessTypeRole,
                                                          Optional<AccessTypeField> optionalAccessType) {

        AccessTypeRoleResult result = new AccessTypeRoleResult();

        // for each jurisdiction build access type Roles
        result.setOrganisationProfileId(accessTypeRole.getOrganisationProfileId());
        result.setAccessTypeId(accessTypeRole.getAccessTypeId());
        if (optionalAccessType.isPresent()) {
            AccessTypeField accessType = optionalAccessType.get();
            result.setAccessMandatory(accessType.getAccessMandatory());
            result.setAccessDefault(accessType.getAccessDefault());
            result.setDisplay(accessType.getDisplay());
            result.setDisplayOrder(accessType.getDisplayOrder());
            result.setDescription(accessType.getDescription());
            result.setHint(accessType.getHint());
        }

        AccessTypeRolesRoleResult role = new AccessTypeRolesRoleResult();

        role.setGroupRoleName(accessTypeRole.getGroupRoleName());
        role.setCaseTypeId(accessTypeRole.getCaseTypeId().getReference());
        role.setOrganisationalRoleName(accessTypeRole.getOrganisationalRoleName());
        role.setCaseGroupIdTemplate(accessTypeRole.getCaseAccessGroupIdTemplate());

        List<AccessTypeRolesRoleResult> accessTypeRolesRoleResults = new ArrayList<>();

        accessTypeRolesRoleResults.add(role);

        result.setRoles(accessTypeRolesRoleResults);

        List<AccessTypeRoleResult> accessTypeRoleResults = new ArrayList<>();

        accessTypeRoleResults.add(result);

        return accessTypeRoleResults;
    }
}

