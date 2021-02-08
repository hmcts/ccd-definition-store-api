package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.excel.service.RoleToAccessProfileService;

@RestController
@Api(value = "/access-profile")
public class RoleToAccessProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(RoleToAccessProfileController.class);

    private RoleToAccessProfileService roleToAccessProfileService;

    @Autowired
    public RoleToAccessProfileController(RoleToAccessProfileService roleToAccessProfileService) {
        this.roleToAccessProfileService = roleToAccessProfileService;
    }

    @Transactional
    @PutMapping(value = "/mapping/{caseTypeId}", produces = {"application/json"})
    @ApiOperation(value = "Create Role to Access Profile mapping for user roles and case roles",
        response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 404, message = "Case Type Reference not found")
    })
    public ResponseEntity createAccessProfileMapping(@PathVariable("caseTypeId") String caseTypeReference) {
        LOG.info("Creating Access Profile mapping for {}", caseTypeReference);
        this.roleToAccessProfileService.createAccessProfileMapping(caseTypeReference);
        return ResponseEntity.status(HttpStatus.CREATED).body("Mapping Created Successfully");
    }

}
