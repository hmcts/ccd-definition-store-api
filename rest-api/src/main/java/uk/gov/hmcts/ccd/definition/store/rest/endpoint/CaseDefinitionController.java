package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.CaseRoleService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeVersionInformation;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseRole;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;

import java.util.List;
import java.util.Optional;

@RestController
@Api(value = "/api/data")
@RequestMapping(value = "/api")
public class CaseDefinitionController {

    private CaseTypeService caseTypeService;
    private JurisdictionService jurisdictionService;
    private final CaseRoleService caseRoleService;

    @Autowired
    public CaseDefinitionController(CaseTypeService caseTypeService, JurisdictionService jurisdictionService,
                                    CaseRoleService caseRoleService) {
        this.caseTypeService = caseTypeService;
        this.jurisdictionService = jurisdictionService;
        this.caseRoleService = caseRoleService;
    }

    private static final Logger LOG = LoggerFactory.getLogger(CaseDefinitionController.class);

    @RequestMapping(value = "/data/case-type/{id}", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Fetch a Case Type Schema", notes = "Returns the schema of a single case type.\n", response = CaseType.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Case Type Schema"),
        @ApiResponse(code = 200, message = "Unexpected error")
    })
    public CaseType dataCaseTypeIdGet(
        @ApiParam(value = "Case Type ID", required = true) @PathVariable("id") String id) {
        return caseTypeService.findByCaseTypeId(id).orElseThrow(() -> new NotFoundException(id));
    }

    @RequestMapping(value = "/data/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Fetch a Case Type Schema", notes = "Returns the schema of a single case type.\n", response = CaseType.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Case Type Schema"),
        @ApiResponse(code = 200, message = "Unexpected error")
    })
    public CaseType dataCaseworkerIdAndJurisdictionIdCaseTypeGet(
        @ApiParam(value = "ID for a Caseworker", required = true) @PathVariable("uid") String caseworkerId,
        @ApiParam(value = "ID for a Jurisdiction", required = true) @PathVariable("jid") String jurisdictionId,
        @ApiParam(value = "ID for Case Type", required = true) @PathVariable("ctid") String caseTypeId) {
        return caseTypeService.findByCaseTypeId(caseTypeId).orElseThrow(() -> new NotFoundException(caseTypeId));
    }

    @RequestMapping(value = "/data/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/roles", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Get Case Roles for a case type", notes = "Returns list of case roles of a single case type.\n", response = CaseRole.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of Case Roles")
    })
    public List<CaseRole> getCaseRoles(
        @ApiParam(value = "ID for a Caseworker", required = true) @PathVariable("uid") String caseworkerId,
        @ApiParam(value = "ID for a Jurisdiction", required = true) @PathVariable("jid") String jurisdictionId,
        @ApiParam(value = "ID for Case Type", required = true) @PathVariable("ctid") String caseTypeId) {
        return caseRoleService.findByCaseTypeId(caseTypeId);
    }

    @RequestMapping(value = "/data/jurisdictions/{jurisdiction_id}/case-type", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Get case types", notes = "Get the case types as a list with optional jurisdiction filter", response = CaseType.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of Case Types")
    })
    public List<CaseType> dataJurisdictionsJurisdictionIdCaseTypeGet(
        @ApiParam(value = "ID for a Jurisdiction", required = true) @PathVariable("jurisdiction_id") String jurisdictionId) {
        return caseTypeService.findByJurisdictionId(jurisdictionId);
    }

    @RequestMapping(value = "/data/jurisdictions", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Get jurisdiction details", response = Jurisdiction.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of jurisdictions")
    })
    public List<Jurisdiction> findJurisdictions(
            @ApiParam(value = "list of jurisdiction references") @RequestParam("ids") Optional<List<String>> idsOptional) {

        LOG.debug("received find jurisdictions request with ids: {}", idsOptional);

        return idsOptional.map(ids -> jurisdictionService.getAll(ids)).orElseGet(jurisdictionService::getAll);
    }

    @RequestMapping(value = "/data/case-type/{ctid}/version",
                    method = RequestMethod.GET,
                    produces = {"application/json"})
    @ApiOperation(value = "Gets the current version of a Case Type Schema",
                  response = CaseTypeVersionInformation.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "version information"),
                           @ApiResponse(code = 404, message = "Not Found")})
    public CaseTypeVersionInformation dataCaseTypeVersionGet(@ApiParam(value = "Case Type ID",
                                                                       required = true) @PathVariable("ctid")
                                                                 String id) {
        return caseTypeService.findVersionInfoByCaseTypeId(id).orElseThrow(() -> new NotFoundException(id));
    }
}
