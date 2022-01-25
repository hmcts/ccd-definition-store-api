package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RestController
@Api(value = "/api/base-types")
@RequestMapping(value = "/api")
public class BaseTypeController {

    private EntityToResponseDTOMapper entityToResponseDTOMapper;
    private FieldTypeRepository fieldTypeRepository;
    private AtomicReference<List<FieldTypeEntity>> baseTypes = new AtomicReference<>();

    @Autowired
    public BaseTypeController(FieldTypeRepository fieldTypeRepository,
                              EntityToResponseDTOMapper entityToResponseDTOMapper) {
        this.fieldTypeRepository = fieldTypeRepository;
        this.entityToResponseDTOMapper = entityToResponseDTOMapper;
    }

    @RequestMapping(value = "/base-types", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Fetch all Base Types", response = FieldType.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "All valid base types")
    })
    public List<FieldType> getBaseTypes() {
        if (CollectionUtils.isEmpty(baseTypes.get())) {
            baseTypes.set(fieldTypeRepository.findCurrentBaseTypes());
        }
        return baseTypes.get().stream().map(entityToResponseDTOMapper::map).collect(Collectors.toList());
    }
}
