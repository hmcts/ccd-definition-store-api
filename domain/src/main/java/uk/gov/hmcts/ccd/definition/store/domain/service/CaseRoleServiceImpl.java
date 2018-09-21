package uk.gov.hmcts.ccd.definition.store.domain.service;

import java.util.List;

import static java.util.stream.Collectors.toList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.CaseRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseRole;

@Component
public class CaseRoleServiceImpl implements CaseRoleService {
    private final CaseRoleRepository caseRoleRepository;
    private final EntityToResponseDTOMapper dtoMapper;

    @Autowired
    public CaseRoleServiceImpl(CaseRoleRepository caseRoleRepository,
                               EntityToResponseDTOMapper dtoMapper) {
        this.caseRoleRepository = caseRoleRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<CaseRole> findByCaseTypeId(String caseType) {
        List<CaseRoleEntity> caseRoleEntities = caseRoleRepository.findCaseRoleEntitiesByCaseType(caseType);

        return caseRoleEntities
            .stream()
            .map(dtoMapper::map)
            .collect(toList());
    }
}
