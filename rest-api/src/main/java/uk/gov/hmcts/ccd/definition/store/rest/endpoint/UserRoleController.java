package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.domain.service.UserRoleService;
import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;

import javax.validation.constraints.NotNull;
import java.util.Base64;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.RESET_CONTENT;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.CREATE;

@RestController
@Api(value = "/api/user-role")
@RequestMapping(value = "/api")
public class UserRoleController {

    private final UserRoleService userRoleService;
    public static final String URI_USER_ROLE = "/user-role";

    @Autowired
    UserRoleController(final UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @PutMapping(value = URI_USER_ROLE, produces = {"application/json"})
    @ResponseStatus(RESET_CONTENT)
    @ApiOperation(value = "Update a user role", response = UserRole.class,
        notes = "a user role is created if it does not exist")
    @ApiResponses(value = {
        @ApiResponse(code = 215, message = "User role is created"),
        @ApiResponse(code = 205,
            message = "User role is updated successfully and the user agent SHOULD reset the document view"),
        @ApiResponse(code = 409, message = "Bad request, for example, incorrect data")
    })
    public ResponseEntity<UserRole> userRolePut(
        @ApiParam(value = "user role", required = true) @RequestBody @NotNull UserRole userRole) {
        final ServiceResponse<UserRole> serviceResponse = userRoleService.saveRole(userRole);
        final ResponseEntity.BodyBuilder responseEntityBuilder = serviceResponse.getOperation() == CREATE
            ? ResponseEntity.status(CREATED) : ResponseEntity.status(RESET_CONTENT);
        return responseEntityBuilder.body(serviceResponse.getResponseBody());
    }

    @PostMapping(value = URI_USER_ROLE, produces = {"application/json"})
    @ResponseStatus(RESET_CONTENT)
    @ApiOperation(value = "Update a user role", response = UserRole.class,
        notes = "a user role is created if it does not exist")
    @ApiResponses(value = {
        @ApiResponse(code = 215, message = "User role is created"),
        @ApiResponse(code = 205,
            message = "User role is updated successfully and the user agent SHOULD reset the document view"),
        @ApiResponse(code = 409, message = "Bad request, for example, incorrect data")
    })
    public ResponseEntity<UserRole> userRoleCreate(
        @ApiParam(value = "user role", required = true) @RequestBody @NotNull UserRole userRole) {
        final ServiceResponse<UserRole> serviceResponse = userRoleService.createRole(userRole);
        final ResponseEntity.BodyBuilder responseEntityBuilder = ResponseEntity.status(CREATED);
        return responseEntityBuilder.body(serviceResponse.getResponseBody());
    }

    @GetMapping(value = URI_USER_ROLE, produces = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get a user profile")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User role is found"),
        @ApiResponse(code = 404, message = "Unable to find user role")
    })
    public UserRole userRoleGet(@RequestParam("role") @NotNull byte[] roleBase64EncodedBytes) {
        final String role = new String(Base64.getDecoder().decode(roleBase64EncodedBytes));
        return userRoleService.getRole(role);
    }

    @RequestMapping(value = "/user-roles/{roles}", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Get user role definitions given as comma separated values", notes = "",
        response = UserRole.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User Role Response is returned"),
    })
    public List<UserRole> getUserRoles(
        @ApiParam(value = "Roles", required = true) @PathVariable("roles") List<String> roles) {
        return this.userRoleService.getRoles(roles);
    }

    @RequestMapping(value = "/user-roles", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(
        value = "Get All user role definitions", notes = "", response = UserRole.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User Roles Response is returned"),
    })
    public List<UserRole> getAllUserRoles() {
        return this.userRoleService.getRoles();
    }
}
