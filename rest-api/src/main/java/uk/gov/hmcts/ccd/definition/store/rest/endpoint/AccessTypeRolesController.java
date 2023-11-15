package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRoles;
import uk.gov.hmcts.ccd.definition.store.repository.model.OrganisationProfileIds;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RestController
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

    @PostMapping(value = "/retrieve-access-types", produces = {"application/json"})
    @ApiOperation(value = "Fetch all Access Types", response = AccessTypeRoles.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "All valid access types"),
        @ApiResponse(code = 403, message = "Bad auth request")
    })
    public List<AccessTypeRoles> retrieveAccessTypesRoles(
        @ApiParam(value = "Draft Definition", required = true)
        @RequestBody @NotNull final OrganisationProfileIds organisationProfileIds) {

        List<AccessTypeRolesEntity> accessTypeRolesEntities =
            accessTypeRolesRepository.findByOrganisationProfileIDs(
                organisationProfileIds.getOrganisationProfileIds());
        return accessTypeRolesEntities.stream().map(entityToResponseDTOMapper::map).collect(Collectors.toList());
    }
}
