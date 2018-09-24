package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.repository.CaseRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseRole;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class CaseRoleServiceImpl implements CaseRoleService {
    private final CaseRoleRepository caseRoleRepository;
    private final CaseTypeRepository caseTypeRepository;

    private final EntityToResponseDTOMapper dtoMapper;

    @Autowired
    public CaseRoleServiceImpl(CaseRoleRepository caseRoleRepository,
                               CaseTypeRepository caseTypeRepository, EntityToResponseDTOMapper dtoMapper) {
        this.caseRoleRepository = caseRoleRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<CaseRole> findByCaseTypeId(String caseType) {
        caseTypeRepository.findLastVersion(caseType).orElseThrow(() -> new NotFoundException(caseType));

        List<CaseRoleEntity> caseRoleEntities = caseRoleRepository.findCaseRoleEntitiesByCaseType(caseType);

        return caseRoleEntities
            .stream()
            .map(dtoMapper::map)
            .collect(toList());
    }
}
