package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionUiConfigService;
import uk.gov.hmcts.ccd.definition.store.domain.service.banner.BannerService;
import uk.gov.hmcts.ccd.definition.store.domain.service.display.DisplayService;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;
import uk.gov.hmcts.ccd.definition.store.repository.model.BannersResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTabCollection;
import uk.gov.hmcts.ccd.definition.store.repository.model.JurisdictionUiConfig;
import uk.gov.hmcts.ccd.definition.store.repository.model.JurisdictionUiConfigResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchCasesResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchInputDefinition;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchResultDefinition;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageCollection;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketResult;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkbasketInputDefinition;

public class DisplayApiControllerTest {

    private DisplayApiController subject;

    private DisplayService displayService;

    private BannerService bannerService;

    private JurisdictionUiConfigService jurisdictionUiConfigService;

    @Before
    public void setup() {
        displayService = mock(DisplayService.class);
        bannerService = mock(BannerService.class);
        jurisdictionUiConfigService = mock(JurisdictionUiConfigService.class);
        subject = new DisplayApiController(displayService, bannerService, jurisdictionUiConfigService);
    }

    @Test
    public void getSearchInputDefinitionDisplay() {
        SearchInputDefinition searchInputDefinition = new SearchInputDefinition();
        when(displayService.findSearchInputDefinitionForCaseType("XXX")).thenReturn(searchInputDefinition);
        subject.displaySearchInputDefinitionIdGet("XXX");
        verify(displayService, times(1)).findSearchInputDefinitionForCaseType("XXX");
    }

    @Test
    public void getSearchResultDefinitionDisplay() {
        SearchResultDefinition searchResultDefinition = new SearchResultDefinition();
        when(displayService.findSearchResultDefinitionForCaseType("XXX")).thenReturn(searchResultDefinition);
        subject.displaySearchResultDefinitionIdGet("XXX");
        verify(displayService, times(1)).findSearchResultDefinitionForCaseType("XXX");
    }

    @Test
    public void shouldReturnWorkbasketInputDefinition() {
        WorkbasketInputDefinition workbasketInputDefinition = new WorkbasketInputDefinition();
        when(displayService.findWorkBasketInputDefinitionForCaseType("XXX")).thenReturn(workbasketInputDefinition);
        subject.displayWorkBasketInputDefinitionIdGet("XXX");
        verify(displayService, times(1)).findWorkBasketInputDefinitionForCaseType("XXX");
    }

    @Test
    public void shouldReturnTabStructure() {
        CaseTabCollection caseTabCollection = new CaseTabCollection();
        when(displayService.findTabStructureForCaseType("XXX")).thenReturn(caseTabCollection);
        subject.displayTabStructureIdGet("XXX");
        verify(displayService, times(1)).findTabStructureForCaseType("XXX");
    }

    @Test
    public void getWorkBasketItemResultDisplay() {
        WorkBasketResult workBasketResult = new WorkBasketResult();
        when(displayService.findWorkBasketDefinitionForCaseType("XXX")).thenReturn(workBasketResult);
        subject.displayWorkBasketDefinitionIdGet("XXX");
        verify(displayService, times(1)).findWorkBasketDefinitionForCaseType("XXX");
    }

    @Test
    public void getSearchCasesResultDisplay() {
        SearchCasesResult searchCasesResult = new SearchCasesResult();
        when(displayService.findSearchCasesResultDefinitionForCaseType("XXX", "useCase")).thenReturn(searchCasesResult);
        subject.displaySearchCasesResultIdGet("XXX", "useCase");
        verify(displayService, times(1)).findSearchCasesResultDefinitionForCaseType("XXX", "useCase");
    }

    @Test
    public void getWizardPageDisplay() {
        WizardPageCollection wizardPageCollection = new WizardPageCollection("TestAddressBookCase", "createCase");
        when(displayService.findWizardPageForCaseType(any(), any())).thenReturn(wizardPageCollection);
        subject.displayWizardPageStructureIdGet("TestAddressBookCase", "createCase");
        verify(displayService).findWizardPageForCaseType("TestAddressBookCase", "createCase");
    }

    @Test
    public void getBannerResults() {
        List<String> references = Collections.singletonList("AUTOTEST1");
        Banner banner = new Banner();
        List<Banner> banners = Collections.singletonList(banner);
        BannersResult bannersResult = new BannersResult(banners);
        when(bannerService.getAll(references)).thenReturn(banners);
        subject.getBanners(Optional.of(references));
        verify(bannerService).getAll(references);
    }

    @Test
    public void getJurisdictionUiConfigs() {
        List<String> references = Collections.singletonList("AUTOTEST1");
        JurisdictionUiConfig jurisdictionUiConfig = new JurisdictionUiConfig();
        when(jurisdictionUiConfigService.getAll(any())).thenReturn(Collections.singletonList(jurisdictionUiConfig));
        JurisdictionUiConfigResult result = subject.getJurisdictionUiConfigs(Optional.of(references));
        verify(jurisdictionUiConfigService).getAll(references);
        assertAll(() -> assertEquals(1, result.getConfigs().size()),
            () -> assertEquals(jurisdictionUiConfig, result.getConfigs().get(0)));
    }
}
