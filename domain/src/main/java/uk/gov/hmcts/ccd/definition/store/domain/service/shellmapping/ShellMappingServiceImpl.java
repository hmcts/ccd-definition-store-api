package uk.gov.hmcts.ccd.definition.store.domain.service.shellmapping;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules.CaseTypeValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.ShellMappingRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseState;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellCaseFieldMapping;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMapping;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMappingResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ShellMappingServiceImpl implements ShellMappingService {

    private final ShellMappingRepository shellMappingRepository;
    private final CaseTypeService caseTypeService;

    private final EntityToResponseDTOMapper dtoMapper;

    public ShellMappingServiceImpl(ShellMappingRepository repository,
                                   CaseTypeService caseTypeService,
                                   EntityToResponseDTOMapper dtoMapper) {
        this.shellMappingRepository = repository;
        this.caseTypeService = caseTypeService;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public void saveAll(List<ShellMappingEntity> entityList) {
        shellMappingRepository.saveAll(entityList);
    }

    @Override
    public List<ShellMapping> findAll() {
        List<ShellMappingEntity> shellMappingEntities = shellMappingRepository.findAll();
        return shellMappingEntities.stream().map(dtoMapper::map).toList();
    }

    @Override
    public ShellMappingResponse findByOriginatingCaseTypeId(String caseTypeId, List<String> stateCategories) {
        CaseType caseType = caseTypeService.findByCaseTypeId(caseTypeId)
            .orElseThrow(() -> new CaseTypeValidationException(
                new CaseTypeValidationResult("Case Type not found " + caseTypeId)
            ));

        List<ShellMappingEntity> shellMappingEntities = shellMappingRepository
            .findByOriginatingCaseTypeIdReference(caseTypeId);
        if (shellMappingEntities.isEmpty()) {
            throw new NotFoundException("No Shell case found for case type id " + caseTypeId);
        }

        List<ShellCaseFieldMapping> fieldMappings = shellMappingEntities.stream()
            .map(entity -> new ShellCaseFieldMapping(
                entity.getOriginatingCaseFieldName().getReference(),
                entity.getShellCaseFieldName().getReference()
            ))
            .toList();

        String shellCaseTypeID = shellMappingEntities.getFirst().getShellCaseTypeId().getReference();
        List<String> caseStates = getFilteredCaseStateIds(caseType, stateCategories);
        return new ShellMappingResponse(shellCaseTypeID, caseStates, fieldMappings);
    }

    private List<String> getFilteredCaseStateIds(CaseType caseType, List<String> stateCategoriesToExclude) {
        List<CaseState> states = caseType.getStates();
        if (states == null || states.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> excludeCategories = toExcludeCategorySet(stateCategoriesToExclude);

        return states.stream()
            .filter(state -> !hasMatchingStateCategory(state, excludeCategories))
            .map(CaseState::getId)
            .toList();
    }

    private Set<String> toExcludeCategorySet(List<String> stateCategoriesToExclude) {
        if (stateCategoriesToExclude == null || stateCategoriesToExclude.isEmpty()) {
            return Collections.emptySet();
        }
        return stateCategoriesToExclude.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(category -> !category.isEmpty())
            .collect(Collectors.toSet());
    }

    private boolean hasMatchingStateCategory(CaseState state, Set<String> excludeCategories) {
        if (excludeCategories.isEmpty()) {
            return false;
        }
        String stateCategory = state.getStateCategory();
        if (stateCategory == null || stateCategory.isBlank()) {
            return false;
        }
        return Arrays.stream(stateCategory.split(","))
            .map(String::trim)
            .anyMatch(excludeCategories::contains);
    }
}
