package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.excel.service.RoleToAccessProfileMappingService;

@RestController
@Api(value = RoleToAccessProfileMappingController.ACCESS_PROFILE_URI)
public class RoleToAccessProfileMappingController {

    public static final String ACCESS_PROFILE_URI = "/access-profile/mapping";

    private RoleToAccessProfileMappingService roleToAccessProfileMappingService;

    @Autowired
    public RoleToAccessProfileMappingController(RoleToAccessProfileMappingService roleToAccessProfileMappingService) {
        this.roleToAccessProfileMappingService = roleToAccessProfileMappingService;
    }

    @Transactional
    @PutMapping(value = ACCESS_PROFILE_URI,  produces = {"application/json"})
    @ApiOperation(value = "Create Role to Access Profile mapping for user roles and case roles",
        response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created")
    })
    public ResponseEntity createAccessProfileMapping(@ApiParam(value = "Comma separated list of case type ID(s) "
        + "or empty if the mapping should be applied on any existing case type", required = true)
                                                         @RequestParam("ctid") List<String> caseTypeIds) {
        String message = this.roleToAccessProfileMappingService
            .createAccessProfileMapping(Sets.newHashSet(caseTypeIds));
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

}
