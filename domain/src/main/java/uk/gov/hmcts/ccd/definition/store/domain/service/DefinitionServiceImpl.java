package uk.gov.hmcts.ccd.definition.store.domain.service;

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

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.CREATE;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.UPDATE;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus.PUBLISHED;

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
    public List<Definition> findByJurisdictionId(String jurisdiction) {
        return decoratedRepository.findByJurisdictionId(jurisdiction)
            .stream()
            .map(mapper::toModel)
            .collect(toList());
    }

    @Override
    public ServiceResponse<Definition> saveDraftDefinition(final Definition definition) {
        preConditionCheck(definition);

        final DefinitionEntity
            definitionEntity =
            decoratedRepository.findByJurisdictionIdAndVersion(definition.getJurisdiction().getId(),
                definition.getVersion());

        if (null == definitionEntity) {
            return createDraftDefinition(definition);
        }

        if (PUBLISHED == definitionEntity.getStatus()) {
            throw new BadRequestException("Definition is live");
        }

        if (definitionEntity.isDeleted()) {
            throw new BadRequestException("Definition is deleted");
        }

        mapper.toEntity(definition, definitionEntity);

        return new ServiceResponse<>(mapper.toModel(
            decoratedRepository.simpleSave(definitionEntity)), UPDATE);
    }

    @Override
    public void deleteDraftDefinition(final String jurisdiction, final Integer version) {
        final DefinitionEntity
            definitionEntity =
            decoratedRepository.findByJurisdictionIdAndVersion(jurisdiction, version);

        if (null == definitionEntity) {
            throw new BadRequestException("Can't find draft definition");
        }

        if (definitionEntity.isDeleted()) {
            throw new BadRequestException("Draft definition is deleted");
        }

        if (PUBLISHED == definitionEntity.getStatus()) {
            throw new BadRequestException("Definition is live");
        }

        definitionEntity.setDeleted(true);
        decoratedRepository.simpleSave(definitionEntity);
    }

    @Override
    public Definition findByJurisdictionIdAndVersion(final String jurisdiction, final Integer version) {
        final DefinitionEntity entity = decoratedRepository.findByJurisdictionIdAndVersion(jurisdiction, version);
        return null == entity ? null : mapper.toModel(entity);
    }

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

}
