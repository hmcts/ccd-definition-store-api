package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Api(value = "/api/base-types")
@RequestMapping(value = "/api")
public class BaseTypeController {

    private EntityToResponseDTOMapper entityToResponseDTOMapper;

    private List<FieldTypeEntity> baseTypes;

    @Autowired
    public BaseTypeController(FieldTypeRepository fieldTypeRepository,
                              EntityToResponseDTOMapper entityToResponseDTOMapper,
                              HttpServletRequest context) {
        this.baseTypes = fieldTypeRepository.findCurrentBaseTypes();
        this.entityToResponseDTOMapper = entityToResponseDTOMapper;
    }

    @RequestMapping(value = "/base-types", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Fetch all Base Types", response = FieldType.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "All valid base types")
    })
    public List<FieldType> getBaseTypes(HttpServletRequest request) {
        return baseTypes.stream().map(entityToResponseDTOMapper::map).collect(Collectors.toList());
    }
}
