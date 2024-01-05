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
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesField;
import uk.gov.hmcts.ccd.definition.store.repository.model.OrganisationProfileIds;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResults;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesRoleResult;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Api(value = "/api")
@RequestMapping(value = "/api")
public class AccessTypeRolesController {

    private final EntityToResponseDTOMapper entityToResponseDTOMapper;
    private final AccessTypeRolesRepository accessTypeRolesRepository;

    @Autowired
    public AccessTypeRolesController(EntityToResponseDTOMapper entityToResponseDTOMapper,
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
        @Valid @RequestBody @NotNull  OrganisationProfileIds organisationProfileIds) {
        List<AccessTypeRolesField> accessTypeRoles = null;

        List<AccessTypeRolesEntity>  accessTypeRolesEntities = accessTypeRolesRepository
            .findByOrganisationProfileIds(organisationProfileIds.getOrganisationProfileIds());
        accessTypeRoles =  accessTypeRolesEntities.stream().map(entityToResponseDTOMapper::map)
            .collect(Collectors.toList());

        // Build the json output from the data
        List<AccessTypeRolesJurisdictionResult> jurisdictions = buildJuristionJsonResult(accessTypeRoles);

        AccessTypeRolesJurisdictionResults jurisdictionResults = new AccessTypeRolesJurisdictionResults();
        jurisdictionResults.setJurisdictions(jurisdictions);
        return  jurisdictionResults;
    }

    private List<AccessTypeRolesJurisdictionResult> buildJuristionJsonResult(
        List<AccessTypeRolesField> accessTypeRoles) {
        List<AccessTypeRolesJurisdictionResult>  jurisdictions = new ArrayList<AccessTypeRolesJurisdictionResult>();

        for (AccessTypeRolesField accessTypeRole : accessTypeRoles) {
            AccessTypeRolesJurisdictionResult jurisdictionResult = new AccessTypeRolesJurisdictionResult();

            CaseTypeEntity caseTypeEntity = accessTypeRole.getCaseTypeId();
            JurisdictionEntity jurisdictionEntity = caseTypeEntity.getJurisdiction();
            jurisdictionResult.setId(jurisdictionEntity.getId().toString());
            jurisdictionResult.setName(jurisdictionEntity.getName());

            List<AccessTypeRolesResult> accessTypeRolesResults = getRoleJsonResults(accessTypeRole);
            jurisdictionResult.setAccessTypeRoles(accessTypeRolesResults);

            jurisdictions.add(jurisdictionResult);
        }
        return jurisdictions;
    }

    private List<AccessTypeRolesResult>  getRoleJsonResults(AccessTypeRolesField accessTypeRole) {

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
        /***** Set with getIdOfCaseType Saved previously from casetypeId before it is copied and is = null when
         * case is copied the id is null as (Property "id") has no write accessor
         * ******/
        role.setCaseTypeId(accessTypeRole.getIdOfCaseType().toString());
        role.setOrganisationalRoleName(accessTypeRole.getOrganisationalRoleName());
        role.setCaseGroupIdTemplate(accessTypeRole.getCaseAccessGroupIdTemplate());

        List<AccessTypeRolesRoleResult> accessTypeRolesRoleResults = new ArrayList<AccessTypeRolesRoleResult>();

        accessTypeRolesRoleResults.add(role);

        result.setRoles(accessTypeRolesRoleResults);

        List<AccessTypeRolesResult> accessTypeRolesResults = new ArrayList<AccessTypeRolesResult>();

        accessTypeRolesResults.add(result);

        return accessTypeRolesResults;
    }
}
