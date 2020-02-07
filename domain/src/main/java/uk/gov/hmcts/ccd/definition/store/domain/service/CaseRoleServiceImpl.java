package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(CaseRoleServiceImpl.class);

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
        Integer caseTypeVersion = caseTypeRepository.findLastVersion(caseType).orElseThrow(() -> new NotFoundException(caseType));
        LOG.debug("CaseType version {} found. for caseType {}...", caseTypeVersion, caseType);

        List<CaseRoleEntity> caseRoleEntities = caseRoleRepository.findCaseRoleEntitiesByCaseType(caseType);

        return caseRoleEntities
            .stream()
            .map(dtoMapper::map)
            .collect(toList());
    }

    public static boolean isCaseRole(final String reference) {
        return reference != null
            && reference.length() > 2
            && reference.trim().startsWith("[")
            && reference.trim().endsWith("]");
    }
}
