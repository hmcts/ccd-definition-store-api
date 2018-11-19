package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.rest.model.IDAMProperties;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileService;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@Api(value = "/api/data")
@RequestMapping(value = "/api")
class IdamProfileController {

    private final IdamProfileService idamProfileService;

    @Autowired
    IdamProfileController(final IdamProfileService idamProfileService) {
        this.idamProfileService = idamProfileService;
    }

    @RequestMapping(value = "/data/idam/profile", method = GET, produces = {"application/json"})
    @ApiOperation(value = "Gets idam profile from current logged in user", response = IDAMProperties.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Case Type Schema"),
        @ApiResponse(code = 200, message = "Unexpected error")
    })
    IDAMProperties getIdamProfile() {
        return idamProfileService.getLoggedInUserDetails();
    }

}
