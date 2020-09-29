package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.rest.configuration.AdminWebAuthorizationProperties;
import uk.gov.hmcts.ccd.definition.store.rest.model.AdminWebAuthorization;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileClient;

@RestController
@Api(value = "/api")
@RequestMapping(value = "/api")
public class AdminWebAuthorizationController {

    private final IdamProfileClient idamProfileClient;
    private final AdminWebAuthorization.AdminWebAuthorizationBuilder adminWebAuthorizationBuilder;

    @Autowired
    public AdminWebAuthorizationController(final IdamProfileClient idamProfileClient,
                                           final AdminWebAuthorizationProperties adminWebAuthorizationProperties) {
        this.idamProfileClient = idamProfileClient;
        this.adminWebAuthorizationBuilder =
            new AdminWebAuthorization.AdminWebAuthorizationBuilder(adminWebAuthorizationProperties);
    }

    @GetMapping(value = "/idam/adminweb/authorization", produces = {"application/json"})
    @ApiOperation(value = "Gets admin web authorization from current logged in user",
        response = AdminWebAuthorization.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Admin web authorization")})
    public AdminWebAuthorization getAdminWebAuthorization() {
        final IdamProperties loggedInUserDetails = idamProfileClient.getLoggedInUserDetails();
        return adminWebAuthorizationBuilder.withIdamProperties(loggedInUserDetails).build();
    }
}
