package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.ccd.definition.store.repository.model.OrganisationProfileIds;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesJurisdictionResults;
import uk.gov.hmcts.ccd.definition.store.rest.service.AccessTypeRolesService;

import javax.validation.Valid;

@RestController
@Api(value = "/api")
@RequestMapping(value = "/api")
public class AccessTypesController {

    private static final Logger LOG = LoggerFactory.getLogger(AccessTypesController.class);

    private final AccessTypeRolesService accessTypeRolesService;

    @Autowired
    public AccessTypesController(AccessTypeRolesService accessTypeRolesService) {
        this.accessTypeRolesService = accessTypeRolesService;
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

        return accessTypeRolesService.retrieveAccessTypeRoles(organisationProfileIds);

    }

}
