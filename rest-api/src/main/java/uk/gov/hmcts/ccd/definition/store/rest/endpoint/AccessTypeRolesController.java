package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRolesField;

import uk.gov.hmcts.ccd.definition.store.repository.model.OrganisationProfileIds;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
    @ApiOperation(value = "Get all accessTypes for all caseTypes", response = AccessTypeRolesEntity.class,
        responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of valid access types request"),
        @ApiResponse(code = 401, message = "Unauthorised request"),
        @ApiResponse(code = 403, message = "Bad request")
    })
    public List<AccessTypeRolesField> retrieveAccessTypeRoles(
        @Valid @RequestBody() @NotNull  OrganisationProfileIds organisationProfileIds) {
        List<AccessTypeRolesEntity>  accessTypeRolesEntities = accessTypeRolesRepository
            .findByOrganisationProfileIds(organisationProfileIds.getOrganisationProfileIds());
        return accessTypeRolesEntities.stream().map(entityToResponseDTOMapper::map)
            .collect(Collectors.toList());
    }

}
