package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRoleField;
import uk.gov.hmcts.ccd.definition.store.repository.model.OrganisationProfileIds;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResults;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRoleResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesRoleResult;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Api(value = "/api")
@RequestMapping(value = "/api")
public class AccessTypesController {

    private final EntityToResponseDTOMapper entityToResponseDTOMapper;
    private final AccessTypeRolesRepository accessTypeRolesRepository;

    @Autowired
    public AccessTypesController(EntityToResponseDTOMapper entityToResponseDTOMapper,
                                 AccessTypeRolesRepository accessTypeRolesRepository) {
        this.entityToResponseDTOMapper = entityToResponseDTOMapper;
        this.accessTypeRolesRepository = accessTypeRolesRepository;
    }


    @PostMapping(value = "/retrieve-access-types",consumes = {"application/json"}, produces = {"application/json"})
    @ApiOperation(value = "Get all accessTypes for all caseTypes", response = AccessTypeRolesJurisdictionResult.class,
        responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of valid access types request"),
        @ApiResponse(code = 401, message = "Unauthorised request"),
        @ApiResponse(code = 403, message = "Bad request")
    })
    public AccessTypeRolesJurisdictionResults retrieveAccessTypeRoles(
        @RequestBody(required = false) @Valid OrganisationProfileIds organisationProfileIds) {
        List<AccessTypeRoleField> accessTypeRoles;

        if (organisationProfileIds != null
            && organisationProfileIds.getOrganisationProfileIds() != null
            && !organisationProfileIds.getOrganisationProfileIds().isEmpty()) {
            //Get only access_types with caseTypeId's by OrganisationProfileIds
            List<AccessTypeRoleEntity> accessTypeRolesEntities = accessTypeRolesRepository
                .findByOrganisationProfileIds(organisationProfileIds.getOrganisationProfileIds());
            accessTypeRoles = accessTypeRolesEntities.stream().map(entityToResponseDTOMapper::map)
                .collect(Collectors.toList());
        } else {
            // get all access Types
            List<AccessTypeRoleEntity> accessTypeRolesEntities = accessTypeRolesRepository
                .findAllWithCaseTypeIds();
            accessTypeRoles = accessTypeRolesEntities.stream().map(entityToResponseDTOMapper::map)
                .collect(Collectors.toList());
        }

        // Build the json output from the data
        List<AccessTypeRolesJurisdictionResult> jurisdictions = buildJurisdictionJsonResult(accessTypeRoles);

        AccessTypeRolesJurisdictionResults jurisdictionResults = new AccessTypeRolesJurisdictionResults();
        jurisdictionResults.setJurisdictions(jurisdictions);
        return  jurisdictionResults;
    }

    private List<AccessTypeRolesJurisdictionResult> buildJurisdictionJsonResult(
        List<AccessTypeRoleField> accessTypeRoles) {
        List<AccessTypeRolesJurisdictionResult>  jurisdictions = new ArrayList<>();

        for (AccessTypeRoleField accessTypeRole : accessTypeRoles) {

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

                List<AccessTypeRoleResult> accessTypeRoleResults = getRoleJsonResults(accessTypeRole);
                jurisdictionResult.setAccessTypeRoles(accessTypeRoleResults);

                jurisdictions.add(jurisdictionResult);
            }
        }
        return jurisdictions;
    }

    private List<AccessTypeRoleResult>  getRoleJsonResults(AccessTypeRoleField accessTypeRole) {

        AccessTypeRoleResult result = new AccessTypeRoleResult();

        // for each jurisdiction build access type Roles
        result.setOrganisationProfileId(accessTypeRole.getOrganisationProfileId());
        result.setAccessTypeId(accessTypeRole.getAccessTypeId());

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
