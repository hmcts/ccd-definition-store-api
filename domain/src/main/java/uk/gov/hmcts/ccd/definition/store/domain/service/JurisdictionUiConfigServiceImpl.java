package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionUiConfigRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionUiConfigEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.JurisdictionUiConfig;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class JurisdictionUiConfigServiceImpl implements JurisdictionUiConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(JurisdictionUiConfigServiceImpl.class);

    private final JurisdictionUiConfigRepository repository;

    private final EntityToResponseDTOMapper dtoMapper;

    @Autowired
    public JurisdictionUiConfigServiceImpl(JurisdictionUiConfigRepository repository,
                                           EntityToResponseDTOMapper dtoMapper) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public void save(JurisdictionUiConfigEntity jurisdictionUiConfigEntity) {
        LOG.debug("Create Jurisdiction UI Config Entity {}", jurisdictionUiConfigEntity);
        String reference = jurisdictionUiConfigEntity.getJurisdiction().getReference();
        Optional<JurisdictionUiConfigEntity> entityObj = Optional.ofNullable(
            repository.findByJurisdictionId(reference));
        JurisdictionUiConfigEntity entityDB = jurisdictionUiConfigEntity;
        if (entityObj.isPresent()) {
            entityDB = entityObj.get();
            entityDB.copy(jurisdictionUiConfigEntity);
        }
        this.repository.save(entityDB);
    }

    @Transactional
    @Override
    public List<JurisdictionUiConfig> getAll(List<String> references) {
        List<JurisdictionUiConfigEntity> entities = repository.findAllByReference(references);
        return entities.stream()
            .map(dtoMapper::map)
            .collect(toList());
    }
}
