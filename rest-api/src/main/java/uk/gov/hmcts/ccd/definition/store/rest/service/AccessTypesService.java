package uk.gov.hmcts.ccd.definition.store.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeField;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRoleField;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.OrganisationProfileIds;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRoleResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeJurisdictionResults;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeJurisdictionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccessTypesService {

    private static final Logger LOG = LoggerFactory.getLogger(AccessTypesService.class);

    private EntityToResponseDTOMapper entityToResponseDTOMapper;
    private AccessTypesRepository accessTypesRepository;
    private AccessTypeRolesRepository accessTypeRolesRepository;

    public AccessTypesService(EntityToResponseDTOMapper entityToResponseDTOMapper,
                              AccessTypesRepository accessTypeRepository,
                              AccessTypeRolesRepository accessTypeRolesRepository) {
        this.entityToResponseDTOMapper = entityToResponseDTOMapper;
        this.accessTypesRepository = accessTypeRepository;
        this.accessTypeRolesRepository = accessTypeRolesRepository;

    }

    public AccessTypeJurisdictionResults retrieveAccessTypeRoles(OrganisationProfileIds organisationProfileIds) {
        return getAccessTypeRolesJurisdictionResults(organisationProfileIds);
    }

    private AccessTypeJurisdictionResults getAccessTypeRolesJurisdictionResults(
        OrganisationProfileIds organisationProfileIds) {
        List<AccessTypeField> accessTypes;
        List<AccessTypeRoleField> accessTypeRoles;

        if (organisationProfileIds != null
            && organisationProfileIds.getOrganisationProfileIds() != null
            && !organisationProfileIds.getOrganisationProfileIds().isEmpty()) {

            // Get only access_types with caseTypeId's by OrganisationProfileIds
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
        List<AccessTypeJurisdictionResult> jurisdictions = buildJurisdictionJsonResult(
            accessTypes, accessTypeRoles);

        AccessTypeJurisdictionResults jurisdictionResults = new AccessTypeJurisdictionResults();
        jurisdictionResults.setJurisdictions(jurisdictions);
        return jurisdictionResults;
    }

    private List<AccessTypeJurisdictionResult> buildJurisdictionJsonResult(
        List<AccessTypeField> accessTypes,
        List<AccessTypeRoleField> accessTypeRoles) {
        List<AccessTypeJurisdictionResult> jurisdictions = new ArrayList<>();

        for (AccessTypeField accessType : accessTypes) {

            Optional<AccessTypeJurisdictionResult> existingJurisdiction = jurisdictions.stream()
                .filter(jurisdiction -> accessType.getJurisdictionId()
                    .equals(jurisdiction.getId()))
                .findAny();

            //find matching access type roles
            List<AccessTypeRoleField> matchingAccessTypeRole = accessTypeRoles.stream()
                .filter(accessTypeRole -> accessTypeRole.getAccessTypeId()
                    .equals(accessType.getAccessTypeId()))
                .filter(accessTypeRole -> accessTypeRole.getCaseTypeId()
                    .equals(accessType.getCaseTypeId()))
                .filter(accessTypeRole -> accessTypeRole.getOrganisationProfileId()
                    .equals(accessType.getOrganisationProfileId())).toList();

            if (existingJurisdiction.isPresent()) {

                existingJurisdiction.get().getAccessTypes().addAll(getAccessTypeJsonResults(
                    accessType, matchingAccessTypeRole));

            } else {
                AccessTypeJurisdictionResult jurisdictionResult = new AccessTypeJurisdictionResult();

                jurisdictionResult.setId(accessType.getJurisdictionId());
                jurisdictionResult.setName(accessType.getJurisdictionName());

                List<AccessTypeResult> accessTypesResults = getAccessTypeJsonResults(
                    accessType, matchingAccessTypeRole);
                jurisdictionResult.setAccessTypes(accessTypesResults);

                jurisdictions.add(jurisdictionResult);
            }
        }
        return jurisdictions;
    }

    private List<AccessTypeResult> getAccessTypeJsonResults(AccessTypeField accessType,
                                                            List<AccessTypeRoleField> accessTypeRoles) {

        AccessTypeResult result = new AccessTypeResult();

        result.setOrganisationProfileId(accessType.getOrganisationProfileId());
        result.setAccessTypeId(accessType.getAccessTypeId());
        result.setAccessMandatory(accessType.getAccessMandatory());
        result.setAccessDefault(accessType.getAccessDefault());
        result.setDisplay(accessType.getDisplay());
        result.setDisplayOrder(accessType.getDisplayOrder());
        result.setDescription(accessType.getDescription());
        result.setHint(accessType.getHint());
        result.setRoles(getAccessTypeRoleJsonResults(accessTypeRoles));

        List<AccessTypeResult> accessTypeResults = new ArrayList<>();
        accessTypeResults.add(result);

        return accessTypeResults;
    }

    private List<AccessTypeRoleResult> getAccessTypeRoleJsonResults(List<AccessTypeRoleField> accessTypeRoles) {

        List<AccessTypeRoleResult> accessTypeRoleResults = new ArrayList<>();

        accessTypeRoles.stream().forEach(accessTypeRole -> {
            AccessTypeRoleResult role = new AccessTypeRoleResult();
            role.setGroupRoleName(accessTypeRole.getGroupRoleName());
            role.setCaseTypeId(accessTypeRole.getCaseTypeId());
            role.setOrganisationalRoleName(accessTypeRole.getOrganisationalRoleName());
            role.setCaseGroupIdTemplate(accessTypeRole.getCaseAccessGroupIdTemplate());
            role.setGroupAccessEnabled(accessTypeRole.getGroupAccessEnabled());
            accessTypeRoleResults.add(role);
        });

        return accessTypeRoleResults;
    }
}

