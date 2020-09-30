package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeLiteRepository;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionRepository;
import uk.gov.hmcts.ccd.definition.store.repository.VersionedDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class JurisdictionServiceImpl implements JurisdictionService {

    private static final Logger LOG = LoggerFactory.getLogger(JurisdictionServiceImpl.class);

    private final JurisdictionRepository repository;
    private final CaseTypeLiteRepository caseTypeLiteRepository;
    private final EntityToResponseDTOMapper entityToResponseDTOMapper;
    private final VersionedDefinitionRepositoryDecorator<JurisdictionEntity, Integer> versionedRepository;

    @Autowired
    public JurisdictionServiceImpl(JurisdictionRepository repository,
                                   CaseTypeLiteRepository caseTypeLiteRepository,
                                   EntityToResponseDTOMapper entityToResponseDTOMapper) {
        this.repository = repository;
        this.caseTypeLiteRepository = caseTypeLiteRepository;
        this.versionedRepository = new VersionedDefinitionRepositoryDecorator<>(repository);
        this.entityToResponseDTOMapper = entityToResponseDTOMapper;
    }

    @Override
    public Optional<JurisdictionEntity> get(String reference) {
        return repository.findFirstByReferenceOrderByVersionDesc(reference);
    }

    @Override
    public List<Jurisdiction> getAll() {
        List<JurisdictionEntity> jurisdictionEntities = repository.findAllLatestVersion();
        return jurisdictionEntities.stream()
            .map(entityToResponseDTOMapper::map)
            .map(this::attachCaseTypes)
            .collect(toList());
    }

    @Override
    public List<Jurisdiction> getAll(List<String> references) {
        LOG.debug("retrieving jurisdictions {}", references);
        List<JurisdictionEntity> jurisdictionEntities = repository.findAllLatestVersionByReference(
            references.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList())
        );
        LOG.debug("retrieved jurisdictions {}", jurisdictionEntities);
        return jurisdictionEntities.stream()
            .map(entityToResponseDTOMapper::map)
            .map(this::attachCaseTypes)
            .collect(toList());
    }

    @Override
    public void create(JurisdictionEntity jurisdiction) {
        versionedRepository.save(jurisdiction);
    }

    private Jurisdiction attachCaseTypes(Jurisdiction jurisdiction) {
        List<CaseTypeLiteEntity> caseTypeLiteEntities =
            caseTypeLiteRepository.findByJurisdictionId(jurisdiction.getId());
        jurisdiction.setCaseTypes(caseTypeLiteEntities.stream().map(entityToResponseDTOMapper::map).collect(toList()));
        return jurisdiction;
    }
}
