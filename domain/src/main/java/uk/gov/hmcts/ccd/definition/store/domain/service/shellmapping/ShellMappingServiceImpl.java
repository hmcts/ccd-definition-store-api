package uk.gov.hmcts.ccd.definition.store.domain.service.shellmapping;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules.CaseTypeValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.ShellMappingRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellCaseFieldMapping;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMapping;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMappingResponse;

import java.util.List;

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
    public ShellMappingResponse findByOriginatingCaseTypeId(String caseTypeId) {
        caseTypeService.findByCaseTypeId(caseTypeId)
            .orElseThrow(() -> new CaseTypeValidationException(
                new CaseTypeValidationResult("Case Type not found " + caseTypeId)
            ));

        List<ShellMappingEntity> shellMappingEntities = shellMappingRepository
            .findByOriginatingCaseTypeIdReference(caseTypeId);
        if (shellMappingEntities.isEmpty()) {
            throw new NotFoundException("No Shell case found for case type id " + caseTypeId);
        }

        // Create field mappings list
        List<ShellCaseFieldMapping> fieldMappings = shellMappingEntities.stream()
            .map(entity -> new ShellCaseFieldMapping(
                entity.getOriginatingCaseFieldName().getReference(),
                entity.getShellCaseFieldName().getReference()
            ))
            .toList();

        // Get shell case type ID from the first mapping
        String shellCaseTypeID = shellMappingEntities.getFirst().getShellCaseTypeId().getReference();
        return new ShellMappingResponse(shellCaseTypeID, fieldMappings);
    }
}
