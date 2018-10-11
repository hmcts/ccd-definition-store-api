package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.service.display.DisplayService;
import uk.gov.hmcts.ccd.definition.store.repository.model.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DisplayApiControllerTest {

    private DisplayApiController subject;

    private DisplayService displayService;

    @Before
    public void setup() {
        displayService = mock(DisplayService.class);
        subject = new DisplayApiController(displayService);
    }

    @Test
    public void  getSearchInputDefinitionDisplay() {
        SearchInputDefinition searchInputDefinition = new SearchInputDefinition();
        when(displayService.findSearchInputDefinitionForCaseType("XXX")).thenReturn(searchInputDefinition);
        subject.displaySearchInputDefinitionIdGet("XXX");
        verify(displayService, times(1)).findSearchInputDefinitionForCaseType("XXX");
    }

    @Test
    public void  getSearchResultDefinitionDisplay() {
        SearchResultDefinition searchResultDefinition = new SearchResultDefinition();
        when(displayService.findSearchResultDefinitionForCaseType("XXX")).thenReturn(searchResultDefinition);
        subject.displaySearchResultDefinitionIdGet("XXX");
        verify(displayService, times(1)).findSearchResultDefinitionForCaseType("XXX");
    }

    @Test
    public void  shouldReturnWorkbasketInputDefinition() {
        WorkbasketInputDefinition workbasketInputDefinition = new WorkbasketInputDefinition();
        when(displayService.findWorkBasketInputDefinitionForCaseType("XXX")).thenReturn(workbasketInputDefinition);
        subject.displayWorkBasketInputDefinitionIdGet("XXX");
        verify(displayService, times(1)).findWorkBasketInputDefinitionForCaseType("XXX");
    }

    @Test
    public void  shouldReturnTabStructure() {
        CaseTabCollection caseTabCollection = new CaseTabCollection();
        when(displayService.findTabStructureForCaseType("XXX")).thenReturn(caseTabCollection);
        subject.displayTabStructureIdGet("XXX");
        verify(displayService, times(1)).findTabStructureForCaseType("XXX");
    }

    @Test
    public void  getWorkBasketItemResultDisplay() {
        WorkBasketResult workBasketResult = new WorkBasketResult();
        when(displayService.findWorkBasketDefinitionForCaseType("XXX")).thenReturn(workBasketResult);
        subject.displayWorkBasketDefinitionIdGet("XXX");
        verify(displayService, times(1)).findWorkBasketDefinitionForCaseType("XXX");
    }

    @Test
    public void getWizardPageDisplay() {
        WizardPageCollection wizardPageCollection = new WizardPageCollection("TestAddressBookCase", "createCase");
        when(displayService.findWizardPageForCaseType(any(), any())).thenReturn(wizardPageCollection);
        subject.displayWizardPageStructureIdGet("TestAddressBookCase", "createCase");
        verify(displayService).findWizardPageForCaseType("TestAddressBookCase", "createCase");
    }
}
