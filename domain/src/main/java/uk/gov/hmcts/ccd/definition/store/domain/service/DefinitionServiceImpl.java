package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.DraftDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.model.Definition;
import uk.gov.hmcts.ccd.definition.store.repository.model.DefinitionModelMapper;

import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.CREATE;

@Service
public class DefinitionServiceImpl implements DefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(DefinitionServiceImpl.class);

    private final DraftDefinitionRepositoryDecorator decoratedRepository;
    private final DefinitionModelMapper mapper;

    @Autowired
    public DefinitionServiceImpl(DraftDefinitionRepositoryDecorator decoratedRepository,
                                 DefinitionModelMapper mapper) {
        this.decoratedRepository = decoratedRepository;
        this.mapper = mapper;
    }

    @Override
    public ServiceResponse<Definition> createDraftDefinition(final Definition definition) {
        LOG.info("Creating draft Definition for " + definition.getJurisdiction().getId() + " jurisdiction...");
        return new ServiceResponse<>(mapper.toModel(decoratedRepository.save(mapper.toEntity(definition))), CREATE);
    }
}
