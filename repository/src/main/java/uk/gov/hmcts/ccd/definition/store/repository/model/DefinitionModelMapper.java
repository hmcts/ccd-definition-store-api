package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Component
public class DefinitionModelMapper {

    private final ObjectMapper mapper = new ObjectMapper();
    private final TypeReference stringJsonMapType = new TypeReference<Map<String, JsonNode>>() {};

    /**
     * Maps a Definition model object to its corresponding entity class. Note: The Jurisdiction object within a
     * Definition is deliberately not mapped. The Jurisdiction is always expected to exist already, so the actual
     * JurisdictionEntity needs to be retrieved then set on the resultant DefinitionEntity, post mapping.
     *
     * @param definition The Definition to be mapped
     * @return A DefinitionEntity with the mappings. Note: The JurisdictionEntity will be null and needs to be set post
     *         mapping with the proper, retrieved entity
     */
    public DefinitionEntity toEntity(@NotNull final Definition definition) {
        final DefinitionEntity definitionEntity = new DefinitionEntity();
        definitionEntity.setCaseTypes(definition.getCaseTypes());
        definitionEntity.setDescription(definition.getDescription());
        definitionEntity.setVersion(definition.getVersion());
        definitionEntity.setStatus(definition.getStatus());
        if (definition.getData() == null) {
            definitionEntity.setData(mapper.createObjectNode());
        } else {
            definitionEntity.setData(mapper.convertValue(definition.getData(), JsonNode.class));
        }
        definitionEntity.setAuthor(definition.getAuthor());
        definitionEntity.setLastModified(definition.getLastModified());
        definitionEntity.setDeleted(definition.isDeleted());

        return definitionEntity;
    }

    /**
     * Maps a DefinitionEntity to its corresponding model class.
     *
     * @param definitionEntity The DefinitionEntity to be mapped
     * @return A Definition model object with the mappings
     */
    public Definition toModel(@NotNull final DefinitionEntity definitionEntity) {
        final Definition definition = new Definition();
        if (definitionEntity.getJurisdiction() != null) {
            final Jurisdiction jurisdiction = new Jurisdiction();
            final JurisdictionEntity jurisdictionEntity = definitionEntity.getJurisdiction();
            jurisdiction.setId(jurisdictionEntity.getReference());
            jurisdiction.setName(jurisdictionEntity.getName());
            jurisdiction.setDescription(jurisdictionEntity.getDescription());
            jurisdiction.setLiveFrom(jurisdictionEntity.getLiveFrom());
            jurisdiction.setLiveUntil(jurisdictionEntity.getLiveTo());
            definition.setJurisdiction(jurisdiction);
        }
        definition.setCaseTypes(definitionEntity.getCaseTypes());
        definition.setDescription(definitionEntity.getDescription());
        definition.setVersion(definitionEntity.getVersion());
        definition.setStatus(definitionEntity.getStatus());
        definition.setData(mapper.convertValue(definitionEntity.getData(), stringJsonMapType));
        definition.setAuthor(definitionEntity.getAuthor());
        definition.setCreatedAt(definitionEntity.getCreatedAt());
        definition.setLastModified(definitionEntity.getLastModified());
        definition.setDeleted(definitionEntity.isDeleted());

        return definition;
    }
}
