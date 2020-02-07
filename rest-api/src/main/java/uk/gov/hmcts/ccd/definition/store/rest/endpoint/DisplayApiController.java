package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionUiConfigService;
import uk.gov.hmcts.ccd.definition.store.domain.service.banner.BannerService;
import uk.gov.hmcts.ccd.definition.store.domain.service.display.DisplayService;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;
import uk.gov.hmcts.ccd.definition.store.repository.model.BannersResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTabCollection;
import uk.gov.hmcts.ccd.definition.store.repository.model.JurisdictionUiConfig;
import uk.gov.hmcts.ccd.definition.store.repository.model.JurisdictionUiConfigResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchInputDefinition;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchResultDefinition;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageCollection;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkbasketInputDefinition;

@Api(value = "/api/display")
@RequestMapping(value = "/api")
@RestController
public class DisplayApiController {

    private DisplayService displayService;

    private final BannerService bannerService;
    
    private final JurisdictionUiConfigService jurisdictionUiConfigService;

    public DisplayApiController(DisplayService displayService,
    		BannerService bannerService,
    		JurisdictionUiConfigService jurisdictionUiConfigService) {
        this.displayService = displayService;
        this.bannerService = bannerService;
        this.jurisdictionUiConfigService = jurisdictionUiConfigService;
    }

    @RequestMapping(value = "/display/search-input-definition/{id}", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Fetch the UI definition for the search inputs for a given Case Type", notes = "", response = SearchInputDefinition.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Search input definition"),
        @ApiResponse(code = 200, message = "Unexpected error")
    })
    public SearchInputDefinition displaySearchInputDefinitionIdGet(
        @ApiParam(value = "Case Type ID", required = true) @PathVariable("id") String id) {
        return this.displayService.findSearchInputDefinitionForCaseType(id);
    }

    @RequestMapping(value = "/display/search-result-definition/{id}", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Fetch the UI definition for the search result fields for a given Case Type", notes = "", response = SearchResultDefinition.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Search result definition"),
        @ApiResponse(code = 200, message = "Unexpected error")
    })
    public SearchResultDefinition displaySearchResultDefinitionIdGet(
        @ApiParam(value = "Case Type ID", required = true) @PathVariable("id") String id) {
        return this.displayService.findSearchResultDefinitionForCaseType(id);
    }

    @RequestMapping(value = "/display/tab-structure/{id}", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Fetch a Case Tab Collection for a given Case Type", notes = "Returns the schema of a single case type.\n", response = CaseTabCollection.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Case Tab Collection"),
        @ApiResponse(code = 200, message = "Unexpected error")
    })
    public CaseTabCollection displayTabStructureIdGet(
        @ApiParam(value = "Case Type ID", required = true) @PathVariable("id") String id) {
        return this.displayService.findTabStructureForCaseType(id);
    }

    @RequestMapping(value = "/display/wizard-page-structure/case-types/{ctid}/event-triggers/{etid}", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Fetch a Case Wizard Page Collection for a given Case Type", notes = "Returns the schema of a single case type.\n", response = CaseTabCollection.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Case Wizard Page Collection"),
        @ApiResponse(code = 200, message = "Unexpected error")
    })
    public WizardPageCollection displayWizardPageStructureIdGet(
        @ApiParam(value = "Case Type ID", required = true) @PathVariable("ctid") String caseTypeId,
        @ApiParam(value = "Event Reference", required = true) @PathVariable("etid") String eventReference) {
        return this.displayService.findWizardPageForCaseType(caseTypeId, eventReference);
    }

    @RequestMapping(value = "/display/work-basket-input-definition/{id}", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Fetch the UI definition for the work basket inputs for a given Case Type", notes = "", response = SearchInputDefinition.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Work Basket input definition"),
        @ApiResponse(code = 200, message = "Unexpected error")
    })
    public WorkbasketInputDefinition displayWorkBasketInputDefinitionIdGet(
        @ApiParam(value = "Case Type ID", required = true) @PathVariable("id") String id) {
        return this.displayService.findWorkBasketInputDefinitionForCaseType(id);
    }

    @RequestMapping(value = "/display/work-basket-definition/{id}", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Fetch the UI definition for the work basket for a given Case Type", notes = "", response = WorkBasketResult.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Work Basket Result"),
        @ApiResponse(code = 200, message = "Unexpected error")
    })
    public WorkBasketResult displayWorkBasketDefinitionIdGet(
        @ApiParam(value = "Case Type ID", required = true) @PathVariable("id") String id) {
        return this.displayService.findWorkBasketDefinitionForCaseType(id);
    }

    @RequestMapping(value = "/display/banners", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Get Banner details for list of jurisdictions", response = BannersResult.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of Banners")
    })
    public BannersResult getBanners(
        @ApiParam(value = "list of jurisdiction references") @RequestParam("ids") Optional<List<String>> referencesOptional) {
        List<Banner> banners = referencesOptional.map(references -> bannerService.getAll(references)).orElse(Collections.emptyList());
        return new BannersResult(banners);
    }
    
    @RequestMapping(value = "/display/jurisdiction-ui-configs", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Get UI config details for list of jurisdictions", response = JurisdictionUiConfigResult.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of Jurisdiction UI Configs")
    })
    public JurisdictionUiConfigResult getJurisdictionUiConfigs(
        @ApiParam(value = "list of jurisdiction references") @RequestParam("ids") Optional<List<String>> referencesOptional) {
        List<JurisdictionUiConfig> configs = referencesOptional.map(references -> jurisdictionUiConfigService.getAll(references)).orElse(Collections.emptyList());
        return new JurisdictionUiConfigResult(configs);
    }
}
