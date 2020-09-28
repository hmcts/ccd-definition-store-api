package uk.gov.hmcts.ccd.definition.store.elastic.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticDefinitionImportListener;
import uk.gov.hmcts.ccd.definition.store.elastic.model.IndicesCreationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;

import static uk.gov.hmcts.ccd.definition.store.elastic.endpoint.ElasticsearchIndexController.ELASTIC_INDEX_URI;

@RestController
@Api(ELASTIC_INDEX_URI)
@Slf4j
@ConditionalOnExpression("'${elasticsearch.enabled}'=='true'")
public class ElasticsearchIndexController {

    public static final String ELASTIC_INDEX_URI = "/elastic-support/index";

    private final CaseTypeRepository caseTypeRepository;
    private final ElasticDefinitionImportListener elasticDefinitionImportListener;

    @Autowired
    public ElasticsearchIndexController(CaseTypeRepository caseTypeRepository,
                                        ElasticDefinitionImportListener elasticDefinitionImportListener) {
        this.caseTypeRepository = caseTypeRepository;
        this.elasticDefinitionImportListener = elasticDefinitionImportListener;
    }

    @PostMapping(ELASTIC_INDEX_URI)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Builds the Elasticsearch indices for all known case types, using their latest version. This API can "
        + "be used to negate the need to reimport all spreadsheet definitions as part of an Elasticsearch reindex.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Elasticsearch indices have been created successfully "
            + "for all known case types"),
        @ApiResponse(code = 400, message = "An error occurred during creation of indices"),
        @ApiResponse(code = 404, message = "Endpoint is disabled")
    })
    public IndicesCreationResult createElasticsearchIndices() {
        log.info("Creating Elasticsearch indices for latest versions of all known case types.");
        List<CaseTypeEntity> allCaseTypes = caseTypeRepository.findAllLatestVersions();
        elasticDefinitionImportListener.initialiseElasticSearch(allCaseTypes);
        return new IndicesCreationResult(allCaseTypes);
    }
}
