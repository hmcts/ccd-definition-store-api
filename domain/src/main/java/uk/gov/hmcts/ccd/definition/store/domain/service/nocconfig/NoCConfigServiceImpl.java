package uk.gov.hmcts.ccd.definition.store.domain.service.nocconfig;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.NoCConfigRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.NoCConfigEntity;

import static com.google.common.base.Strings.isNullOrEmpty;

@Component
@Slf4j
public class NoCConfigServiceImpl implements NoCConfigService {

    private final NoCConfigRepository noCConfigRepository;

    private final EntityToResponseDTOMapper dtoMapper;

    @Autowired
    public NoCConfigServiceImpl(NoCConfigRepository noCConfigRepository,
                                EntityToResponseDTOMapper dtoMapper) {
        this.noCConfigRepository = noCConfigRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public void save(NoCConfigEntity noCConfigEntity) {
        log.debug("Create noc config entity {}", noCConfigEntity);
        String reference = noCConfigEntity.getCaseType().getReference();
        Optional<NoCConfigEntity> noCConfigEntityObj = Optional.ofNullable(
            noCConfigRepository.findByCaseTypeReference(reference));
        NoCConfigEntity noCConfigEntityDb = noCConfigEntity;
        if (noCConfigEntityObj.isPresent()) {
            noCConfigEntityDb = noCConfigEntityObj.get();
            noCConfigEntityDb.copy(noCConfigEntity);
        }
        this.noCConfigRepository.save(noCConfigEntityDb);
    }

    @Override
    public List<NoCConfigEntity> getAll(List<String> caseTypeReferences) {
        return this.noCConfigRepository.findAllByCaseTypeReferences(caseTypeReferences);
    }

    @Override
    public void deleteCaseTypeNocConfig(String caseTypeReference) {
        if (isNullOrEmpty(caseTypeReference)) {
            return;
        }
        this.noCConfigRepository.deleteByCaseTypeReference(caseTypeReference);
    }
}
