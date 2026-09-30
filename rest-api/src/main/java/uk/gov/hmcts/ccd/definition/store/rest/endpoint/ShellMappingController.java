package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.domain.service.shellmapping.ShellMappingService;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMappingResponse;

@RestController
@Validated
@Api(value = "/api/retrieve-shell-mappings")
@RequestMapping(value = "/api")
public class ShellMappingController {

    private final ShellMappingService shellMappingService;

    public static final String RETRIEVE_SHELL_MAPPINGS = "/retrieve-shell-mappings/{originalCaseTypeId}";

    @Autowired
    public ShellMappingController(ShellMappingService shellMappingService) {
        this.shellMappingService = shellMappingService;
    }

    @GetMapping(value = RETRIEVE_SHELL_MAPPINGS, produces = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Retrieve Shell Mappings for case type")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns list of shell mappings for case type"),
        @ApiResponse(code = 404, message = "No Shell case found"),
        @ApiResponse(code = 400, message = "Invalid caseTypeId"),
        @ApiResponse(code = 401, message = "Unauthorised"),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    public ShellMappingResponse shellMappings(@PathVariable("originalCaseTypeId") @NotBlank String originalCaseTypeId) {
        return shellMappingService.findByOriginatingCaseTypeId(originalCaseTypeId);
    }
}
