package uk.gov.hmcts.ccd.definition.store.domain.service;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.exception.BadRequestException;
import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.DraftDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Definition;
import uk.gov.hmcts.ccd.definition.store.repository.model.DefinitionModelMapper;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;

import javax.persistence.OptimisticLockException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.CREATE;

@Service
public class DefinitionServiceImpl implements DefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(DefinitionServiceImpl.class);

    private final JurisdictionRepository jurisdictionRepository;
    private final DraftDefinitionRepositoryDecorator decoratedRepository;
    private final DefinitionModelMapper mapper;

    @Autowired
    public DefinitionServiceImpl(JurisdictionRepository jurisdictionRepository,
                                 DraftDefinitionRepositoryDecorator decoratedRepository,
                                 DefinitionModelMapper mapper) {
        this.jurisdictionRepository = jurisdictionRepository;
        this.decoratedRepository = decoratedRepository;
        this.mapper = mapper;
    }

    @Override
    public ServiceResponse<Definition> createDraftDefinition(final Definition definition) {
        preConditionCheck(definition);

        Jurisdiction jurisdiction = definition.getJurisdiction();
        // Retrieve the corresponding JurisdictionEntity for the Jurisdiction reference in the Definition
        return jurisdictionRepository.findFirstByReferenceOrderByVersionDesc(jurisdiction.getId())
            .map(jurisdictionEntity -> {
                LOG.info("Creating draft Definition for {} jurisdiction...", jurisdiction.getId());
                // If found, this then needs to be attached to the mapped DefinitionEntity, prior to persisting
                final DefinitionEntity definitionEntity = mapper.toEntity(definition);
                definitionEntity.setJurisdiction(jurisdictionEntity);
                return new ServiceResponse<>(mapper.toModel(
                    decoratedRepository.save(definitionEntity)), CREATE);
            })
            .orElseThrow(() -> new BadRequestException(
                "Jurisdiction " + jurisdiction.getId() + " could not be retrieved or does not exist"));
    }

    @Override
    public ServiceResponse<Definition> saveDraftDefinition(final Definition definition) {
        preConditionCheck(definition);

        final String jurisdictionId = definition.getJurisdiction().getId();
        final DefinitionEntity
            latestDraftDefinition =
            decoratedRepository.findLatestByJurisdictionId(jurisdictionId);

        if (latestDraftDefinition == null) {
            throw new BadRequestException("Jurisdiction " + jurisdictionId
                                          + " could not be retrieved or does not exist");
        }

        if (latestDraftDefinition.getVersion() != definition.getVersion()) {
            throw new OptimisticLockException("Mismatched definition version.");
        }

        return jurisdictionRepository.findFirstByReferenceOrderByVersionDesc(jurisdictionId)
                                     .map(jurisdictionEntity -> {
                                         LOG.info("Creating draft Definition for {} jurisdiction...",
                                                  jurisdictionId);
                                         final DefinitionEntity definitionEntity = mapper.toEntity(definition);
                                         definitionEntity.setJurisdiction(jurisdictionEntity);
                                         return new ServiceResponse<>(mapper.toModel(
                                             decoratedRepository.save(definitionEntity)), CREATE);
                                     })
                                     .orElseThrow(() -> new BadRequestException(
                                         "Jurisdiction " + jurisdictionId
                                         + " could not be retrieved or does not exist"));
    }

    @Override
    public void deleteDraftDefinition(String jurisdiction) {
        final DefinitionEntity
            latestDefinitionByJurisdictionId =
            decoratedRepository.findLatestByJurisdictionId(jurisdiction);
        if (null == latestDefinitionByJurisdictionId) {
            throw new BadRequestException("Can't find definition for jurisdiction " + jurisdiction);
        }
        latestDefinitionByJurisdictionId.setDeleted(true);
        decoratedRepository.save(latestDefinitionByJurisdictionId);
    }

    @VisibleForTesting
    protected void preConditionCheck(final Definition definition) {
        if (definition.getDescription() == null) {
            throw new BadRequestException("Definition description cannot be null");
        }

        if (definition.getAuthor() == null) {
            throw new BadRequestException("Definition author cannot be null");
        }

        if (definition.getJurisdiction() == null) {
            throw new BadRequestException("No Jurisdiction present in Definition");
        }
    }

    @Override
    public List<Definition> findByJurisdictionId(String jurisdiction) {
        return decoratedRepository.findByJurisdictionId(jurisdiction)
                                  .stream()
                                  .map(mapper::toModel)
                                  .collect(toList());
    }

    @Override
    public Definition findByJurisdictionIdAndVersion(final String jurisdiction, final Integer version) {
        final DefinitionEntity entity = decoratedRepository.findByJurisdictionIdAndVersion(jurisdiction, version);
        return null == entity ? null : mapper.toModel(entity);
    }
}
