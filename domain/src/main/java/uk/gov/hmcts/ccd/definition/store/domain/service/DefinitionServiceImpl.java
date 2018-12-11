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
        if (definition.getDescription() == null) {
            throw new BadRequestException("Definition description cannot be null");
        }

        if (definition.getAuthor() == null) {
            throw new BadRequestException("Definition author cannot be null");
        }

        if (definition.getJurisdiction() == null) {
            throw new BadRequestException("No Jurisdiction present in Definition");
        } else {
            Jurisdiction jurisdiction = definition.getJurisdiction();
            // Retrieve the corresponding JurisdictionEntity for the Jurisdiction reference in the Definition
            return jurisdictionRepository.findFirstByReferenceOrderByVersionDesc(jurisdiction.getId())
                .map(jurisdictionEntity -> {
                    LOG.info("Creating draft Definition for " + jurisdiction.getId() + " jurisdiction...");
                    // If found, this then needs to be attached to the mapped DefinitionEntity, prior to persisting
                    final DefinitionEntity definitionEntity = mapper.toEntity(definition);
                    definitionEntity.setJurisdiction(jurisdictionEntity);
                    return new ServiceResponse<>(mapper.toModel(
                        decoratedRepository.save(definitionEntity)), CREATE);
                })
                .orElseThrow(() -> new BadRequestException(
                    "Jurisdiction " + jurisdiction.getId() + " could not be retrieved or does not exist"));
        }
    }

    @Override
    public Definition findLatestByJurisdictionId(final String jurisdiction) {
        final DefinitionEntity entity = decoratedRepository.findLatestByJurisdictionId(jurisdiction);
        return null == entity ? null : mapper.toModel(entity);
    }
}
