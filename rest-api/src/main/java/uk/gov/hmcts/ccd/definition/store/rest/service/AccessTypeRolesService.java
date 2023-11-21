package uk.gov.hmcts.ccd.definition.store.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.OrganisationProfileIds;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesRoleResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResults;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesField;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccessTypeRolesService {

    private static final Logger LOG = LoggerFactory.getLogger(AccessTypeRolesService.class);

    private EntityToResponseDTOMapper entityToResponseDTOMapper;
    private AccessTypeRolesRepository accessTypeRolesRepository;

    public AccessTypeRolesService(EntityToResponseDTOMapper entityToResponseDTOMapper,
                                  AccessTypeRolesRepository accessTypeRolesRepository) {
        this.entityToResponseDTOMapper = entityToResponseDTOMapper;
        this.accessTypeRolesRepository = accessTypeRolesRepository;

    }

    public AccessTypeRolesJurisdictionResults retrieveAccessTypeRoles(OrganisationProfileIds organisationProfileIds) {
        return getAccessTypeRolesJurisdictionResults(organisationProfileIds);
    }

    private AccessTypeRolesJurisdictionResults getAccessTypeRolesJurisdictionResults(
        OrganisationProfileIds organisationProfileIds) {
        List<AccessTypeRolesField> accessTypeRoles;

        if (organisationProfileIds != null
            && organisationProfileIds.getOrganisationProfileIds() != null
            && !organisationProfileIds.getOrganisationProfileIds().isEmpty()) {
            //Get only access_types with caseTypeId's by OrganisationProfileIds
            List<AccessTypeRolesEntity> accessTypeRolesEntities = accessTypeRolesRepository
                .findByOrganisationProfileIds(organisationProfileIds.getOrganisationProfileIds());
            accessTypeRoles = accessTypeRolesEntities.stream().map(entityToResponseDTOMapper::map)
                .collect(Collectors.toList());
        } else {
            // get all access Types
            List<AccessTypeRolesEntity> accessTypeRolesEntities = accessTypeRolesRepository
                .findAllWithCaseTypeIds();
            accessTypeRoles = accessTypeRolesEntities.stream().map(entityToResponseDTOMapper::map)
                .collect(Collectors.toList());
        }

        // Build the json output from the data
        List<AccessTypeRolesJurisdictionResult> jurisdictions = buildJurisdictionJsonResult(accessTypeRoles);

        AccessTypeRolesJurisdictionResults jurisdictionResults = new AccessTypeRolesJurisdictionResults();
        jurisdictionResults.setJurisdictions(jurisdictions);
        return jurisdictionResults;
    }


    private List<AccessTypeRolesJurisdictionResult> buildJurisdictionJsonResult(
        List<AccessTypeRolesField> accessTypeRoles) {
        List<AccessTypeRolesJurisdictionResult> jurisdictions = new ArrayList<>();

        for (AccessTypeRolesField accessTypeRole : accessTypeRoles) {

            Optional<AccessTypeRolesJurisdictionResult> existingJurisdiction = jurisdictions.stream()
                .filter(jurisdiction -> accessTypeRole.getCaseTypeId().getJurisdiction().getReference()
                    .equals(jurisdiction.getId()))
                .findAny();

            if (existingJurisdiction.isPresent()) {
                existingJurisdiction.get().getAccessTypeRoles().addAll(getRoleJsonResults(accessTypeRole));

            } else {
                AccessTypeRolesJurisdictionResult jurisdictionResult = new AccessTypeRolesJurisdictionResult();

                CaseTypeEntity caseTypeEntity = accessTypeRole.getCaseTypeId();
                JurisdictionEntity jurisdictionEntity = caseTypeEntity.getJurisdiction();
                jurisdictionResult.setId(jurisdictionEntity.getReference());
                jurisdictionResult.setName(jurisdictionEntity.getName());

                List<AccessTypeRolesResult> accessTypeRolesResults = getRoleJsonResults(accessTypeRole);
                jurisdictionResult.setAccessTypeRoles(accessTypeRolesResults);

                jurisdictions.add(jurisdictionResult);
            }
        }
        return jurisdictions;
    }

    private List<AccessTypeRolesResult> getRoleJsonResults(AccessTypeRolesField accessTypeRole) {

        AccessTypeRolesResult result = new AccessTypeRolesResult();

        // for each jurisdiction build access type Roles
        result.setOrganisationProfileId(accessTypeRole.getOrganisationProfileId());
        result.setAccessTypeId(accessTypeRole.getAccessTypeId());
        result.setAccessMandatory(accessTypeRole.getAccessMandatory());
        result.setAccessDefault(accessTypeRole.getAccessDefault());
        result.setDisplay(accessTypeRole.getDisplay());
        result.setDisplayOrder(accessTypeRole.getDisplayOrder());
        result.setDescription(accessTypeRole.getDescription());
        result.setHint(accessTypeRole.getHint());

        AccessTypeRolesRoleResult role = new AccessTypeRolesRoleResult();

        role.setGroupRoleName(accessTypeRole.getGroupRoleName());
        role.setCaseTypeId(accessTypeRole.getCaseTypeId().getReference());
        role.setOrganisationalRoleName(accessTypeRole.getOrganisationalRoleName());
        role.setCaseGroupIdTemplate(accessTypeRole.getCaseAccessGroupIdTemplate());

        List<AccessTypeRolesRoleResult> accessTypeRolesRoleResults = new ArrayList<>();

        accessTypeRolesRoleResults.add(role);

        result.setRoles(accessTypeRolesRoleResults);

        List<AccessTypeRolesResult> accessTypeRolesResults = new ArrayList<>();

        accessTypeRolesResults.add(result);

        return accessTypeRolesResults;
    }
}

