package uk.gov.hmcts.ccd.definition.store.domain.service.display;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.GenericLayoutRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;
import uk.gov.hmcts.ccd.definition.store.repository.model.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisplayService {

    private GenericLayoutRepository genericLayoutRepository;
    private DisplayGroupAdapterService displayGroupAdapterService;
    private DisplayGroupRepository displayGroupRepository;
    private EntityToResponseDTOMapper entityToResponseDTOMapper;

    @Autowired
    public DisplayService(GenericLayoutRepository genericLayoutRepository,
                          DisplayGroupAdapterService displayGroupAdapterService,
                          DisplayGroupRepository displayGroupRepository,
                          EntityToResponseDTOMapper entityToResponseDTOMapper) {
        this.genericLayoutRepository = genericLayoutRepository;
        this.displayGroupAdapterService = displayGroupAdapterService;
        this.displayGroupRepository = displayGroupRepository;
        this.entityToResponseDTOMapper = entityToResponseDTOMapper;
    }

    public SearchResultDefinition findSearchResultDefinitionForCaseType(String caseTypeId) {
        return mapToSearchResultDefinition(this.genericLayoutRepository.findSearchResultsByCaseTypeReference(caseTypeId), caseTypeId);
    }

    public SearchInputDefinition findSearchInputDefinitionForCaseType(String caseTypeId) {
        return mapToSearchInputDefinition(this.genericLayoutRepository.findSearchInputsByCaseTypeReference(caseTypeId), caseTypeId);
    }

    public CaseTabCollection findTabStructureForCaseType(String caseTypeId) {
        return mapToCaseTabCollection(this.displayGroupRepository.findTabsByCaseTypeReference(caseTypeId), caseTypeId);
    }

    public WorkbasketInputDefinition findWorkBasketInputDefinitionForCaseType(String caseTypeId) {
        return mapToWorkBasketInputDefinition(this.genericLayoutRepository.findWorkbasketInputByCaseTypeReference(caseTypeId), caseTypeId);
    }

    public WorkBasketResult findWorkBasketDefinitionForCaseType(String caseTypeId) {
        return mapToWorkBasketResult(this.genericLayoutRepository.findWorkbasketByCaseTypeReference(caseTypeId), caseTypeId);
    }

    public SearchCasesResult findSearchCasesResultDefinitionForCaseType(String caseTypeId) {
        return mapToSearchCasesResult(this.genericLayoutRepository.findSearchCasesResultsByCaseTypeReference(caseTypeId), caseTypeId);
    }

    public SearchCasesResult findSearchCasesResultDefinitionForCaseType(String caseTypeId, String useCase) {
        return mapToSearchCasesResult(this.genericLayoutRepository.findSearchCasesResultsByCaseTypeReference(caseTypeId, useCase), caseTypeId);
    }


    public WizardPageCollection findWizardPageForCaseType(String caseTypeId, String eventReference) {
        return displayGroupAdapterService.findWizardPagesByCaseTypeId(caseTypeId, eventReference);
    }

    private CaseTabCollection mapToCaseTabCollection(List<DisplayGroupEntity> displayGroupEntities, String caseTypeId) {

        CaseTabCollection caseTabCollection = new CaseTabCollection();
        caseTabCollection.setCaseTypeId(caseTypeId);
        caseTabCollection.setTabs(
            displayGroupEntities.stream().map(
                displayGroupEntity
                    -> entityToResponseDTOMapper.map(displayGroupEntity)
            ).collect(Collectors.toList())
        );
        caseTabCollection.setChannels(
            displayGroupEntities.stream().map(DisplayGroupEntity::getChannel).collect(Collectors.toList())
        );

        return caseTabCollection;
    }

    private SearchInputDefinition mapToSearchInputDefinition(List<SearchInputCaseFieldEntity> searchInputCaseFieldEntities,
                                                             String caseTypeId) {
        SearchInputDefinition searchInputDefinition = new SearchInputDefinition();
        searchInputDefinition.setCaseTypeId(caseTypeId);
        searchInputDefinition.setFields(
            searchInputCaseFieldEntities.stream()
                .map(searchInputCaseFieldEntity -> entityToResponseDTOMapper.map(searchInputCaseFieldEntity))
                .collect(Collectors.toList())
        );
        return searchInputDefinition;
    }

    private SearchResultDefinition mapToSearchResultDefinition(List<SearchResultCaseFieldEntity> searchResultCaseFieldEntities,
                                                               String caseTypeId) {
        SearchResultDefinition searchResultDefinition = new SearchResultDefinition();
        searchResultDefinition.setCaseTypeId(caseTypeId);
        searchResultDefinition.setFields(
            searchResultCaseFieldEntities.stream()
                .map(searchResultCaseFieldEntity -> entityToResponseDTOMapper.map(searchResultCaseFieldEntity))
                .collect(Collectors.toList())
        );
        return searchResultDefinition;
    }

    private WorkbasketInputDefinition mapToWorkBasketInputDefinition(List<WorkBasketInputCaseFieldEntity> workBasketInputCaseFieldEntities,
                                                             String caseTypeId) {
        WorkbasketInputDefinition workbasketInputDefinition = new WorkbasketInputDefinition();
        workbasketInputDefinition.setCaseTypeId(caseTypeId);
        workbasketInputDefinition.setFields(
            workBasketInputCaseFieldEntities.stream()
                .map(workBasketInputCaseFieldEntity -> entityToResponseDTOMapper.map(workBasketInputCaseFieldEntity))
                .collect(Collectors.toList())
        );
        return workbasketInputDefinition;
    }

    private WorkBasketResult mapToWorkBasketResult(List<WorkBasketCaseFieldEntity> workBasketCaseFieldEntities,
                                                   String caseTypeId) {
        WorkBasketResult workBasketResult = new WorkBasketResult();
        workBasketResult.setCaseTypeId(caseTypeId);
        workBasketResult.setFields(
            workBasketCaseFieldEntities.stream()
                .map(workBasketCaseFieldEntity -> entityToResponseDTOMapper.map(workBasketCaseFieldEntity))
                .collect(Collectors.toList())
        );
        return workBasketResult;
    }

    private SearchCasesResult mapToSearchCasesResult(List<SearchCasesResultFieldEntity> searchCasesResultFieldEntities,
                                                   String caseTypeId) {
        SearchCasesResult searchCasesResult = new SearchCasesResult();
        searchCasesResult.setCaseTypeId(caseTypeId);
        searchCasesResult.setFields(
            searchCasesResultFieldEntities.stream()
            .map(searchCasesResultFieldEntity -> entityToResponseDTOMapper.map(searchCasesResultFieldEntity))
            .collect(Collectors.toList())
        );
        return searchCasesResult;
    }


}
