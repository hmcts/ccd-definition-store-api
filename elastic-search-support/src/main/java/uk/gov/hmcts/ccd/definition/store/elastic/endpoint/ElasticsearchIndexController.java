package uk.gov.hmcts.ccd.definition.store.elastic.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticDefinitionImportListener;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticGlobalSearchListener;
import uk.gov.hmcts.ccd.definition.store.elastic.model.IndicesCreationResult;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
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
    public static final String GS_ELASTIC_INDEX_URI = "/elastic-support/global-search/index";

    private final CaseTypeRepository caseTypeRepository;
    private final ElasticDefinitionImportListener elasticDefinitionImportListener;
    private final ElasticGlobalSearchListener elasticGlobalSearchListener;

    @Autowired
    public ElasticsearchIndexController(CaseTypeRepository caseTypeRepository,
                                        ElasticDefinitionImportListener elasticDefinitionImportListener,
                                        ElasticGlobalSearchListener elasticGlobalSearchListener) {
        this.caseTypeRepository = caseTypeRepository;
        this.elasticDefinitionImportListener = elasticDefinitionImportListener;
        this.elasticGlobalSearchListener = elasticGlobalSearchListener;
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
    public IndicesCreationResult createElasticsearchIndices(@ApiParam("Comma separated list of case types for which "
                                                            + " Elastic indices should be created. If no values are "
                                                            + "provided, all case types' indices are created.")
                                                            @RequestParam(value = "ctid", required = false)
                                                            List<String> caseTypeIds) {
        log.info("Creating Elasticsearch indices for latest versions of case types.");
        List<CaseTypeEntity> caseTypesToIndex = CollectionUtils.isEmpty(caseTypeIds)
            ? caseTypeRepository.findAllLatestVersions()
            : caseTypeRepository.findAllLatestVersions(caseTypeIds);
        DefinitionImportedEvent event = new DefinitionImportedEvent(caseTypesToIndex, false, true);
        elasticDefinitionImportListener.initialiseElasticSearch(event);
        return new IndicesCreationResult(caseTypesToIndex);
    }

    @PostMapping(GS_ELASTIC_INDEX_URI)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Builds the Elasticsearch index for Global Search.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Global Search Elasticsearch index has been created successfully."),
        @ApiResponse(code = 400, message = "An error occurred during creation of index"),
        @ApiResponse(code = 404, message = "Endpoint is disabled")
    })
    public void createGlobalSearchElasticsearchIndex() {
        log.info("Creating Elasticsearch index for Global search.");
        elasticGlobalSearchListener.initialiseElasticSearchForGlobalSearch();
    }

    @GetMapping("/elastic-support/case-types")
    @ApiOperation("Returns the list of unique case type references across all jurisdictions.")
    public List<String> getAllCaseTypeReferences() {
        return caseTypeRepository.findAllCaseTypeIds();
    }

}
