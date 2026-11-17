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
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellCaseState;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMapping;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMappingResponse;
import uk.gov.hmcts.ccd.definition.store.repository.model.Version;

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
    public ShellMappingResponse findByOriginatingCaseTypeId(String caseTypeId,
                                                            List<String> stateCategoriesToExclude) {
        CaseType caseType = caseTypeService.findByCaseTypeId(caseTypeId)
            .orElseThrow(() -> new CaseTypeValidationException(
                new CaseTypeValidationResult("Case Type not found " + caseTypeId)
            ));

        // Use the already-loaded case type version so states and mappings stay consistent
        // if a newer definition is imported between the two lookups.
        Integer caseTypeVersion = requireCaseTypeVersion(caseType, caseTypeId);
        List<ShellMappingEntity> shellMappingEntities = shellMappingRepository
            .findByOriginatingCaseTypeIdReferenceAndVersion(caseTypeId, caseTypeVersion);
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
        List<ShellCaseState> caseStates = getCaseStatesExcludingCategories(caseType, stateCategoriesToExclude);
        return new ShellMappingResponse(shellCaseTypeID, caseStates, fieldMappings);
    }

    private Integer requireCaseTypeVersion(CaseType caseType, String caseTypeId) {
        Version version = caseType.getVersion();
        if (version == null || version.getNumber() == null) {
            throw new CaseTypeValidationException(
                new CaseTypeValidationResult("Case Type version not found " + caseTypeId)
            );
        }
        return version.getNumber();
    }

    /**
     * Returns originating case states, excluding any state whose stateCategory matches
     * one of the supplied values. Matching is by any comma-separated category on the state
     * (e.g. "Start,End" matches exclude value "End"). Null/blank state categories are kept.
     */
    private List<ShellCaseState> getCaseStatesExcludingCategories(CaseType caseType,
                                                                  List<String> stateCategoriesToExclude) {
        List<CaseState> states = caseType.getStates();
        if (states == null || states.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> excludeCategories = toExcludeCategorySet(stateCategoriesToExclude);

        return states.stream()
            .filter(state -> !hasMatchingStateCategory(state, excludeCategories))
            .map(state -> new ShellCaseState(state.getId(), state.getStateCategory()))
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
