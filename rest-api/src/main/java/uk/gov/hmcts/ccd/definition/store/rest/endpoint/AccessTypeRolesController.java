package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    private EntityToResponseDTOMapper entityToResponseDTOMapper;
    private AccessTypeRolesRepository accessTypeRolesRepository;

    @Autowired
    public AccessTypeRolesController(EntityToResponseDTOMapper entityToResponseDTOMapper,
                                     AccessTypeRolesRepository accessTypeRolesRepository) {
        this.entityToResponseDTOMapper = entityToResponseDTOMapper;
        this.accessTypeRolesRepository = accessTypeRolesRepository;
    }


    @PostMapping(value = "/retrieve-access-types",consumes = {"application/json"}, produces = {"application/json"})
    @ApiOperation(value = "Get all accessTypes for all caseTypes", response = AccessTypeRolesEntity.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of valid access types request"),
        @ApiResponse(code = 401, message = "Unauthorised request"),
        @ApiResponse(code = 403, message = "Bad request")
    })
    public List<AccessTypeRolesField> retrieveAccessTypeRoles (
        @Valid @RequestBody() @NotNull  OrganisationProfileIds organisationProfileIds){
        List<AccessTypeRolesEntity>  accessTypeRolesEntities = accessTypeRolesRepository.findByOrganisationProfileIds(organisationProfileIds.getOrganisationProfileIds());
        return accessTypeRolesEntities.stream().map(entityToResponseDTOMapper::map).collect(Collectors.toList());
    }

}
