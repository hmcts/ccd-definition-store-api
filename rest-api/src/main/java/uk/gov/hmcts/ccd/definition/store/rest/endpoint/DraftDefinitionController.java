package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.domain.service.DefinitionService;
import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.model.Definition;

import javax.validation.constraints.NotNull;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@Api(value = "/api/draft")
@RequestMapping(value = "/api")
public class DraftDefinitionController {

    private final DefinitionService definitionService;

    @Autowired
    public DraftDefinitionController(final DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @PostMapping("/draft")
    @ResponseStatus(CREATED)
    @ApiOperation(
        value = "Creates a draft Definition",
        notes = "Creates a draft Definition for the specified Jurisdiction, incrementing the version number each time"
    )
    @ApiResponse(code = 201, message = "Draft Definition created")
    public ResponseEntity<Definition> draftDefinitionCreate(
        @ApiParam(value = "Draft Definition", required = true)
        @RequestBody @NotNull final Definition definition) {
        final ServiceResponse<Definition> serviceResponse = definitionService.createDraftDefinition(definition);
        final ResponseEntity.BodyBuilder responseEntityBuilder = ResponseEntity.status(CREATED);
        return responseEntityBuilder.body(serviceResponse.getResponseBody());
    }
}
