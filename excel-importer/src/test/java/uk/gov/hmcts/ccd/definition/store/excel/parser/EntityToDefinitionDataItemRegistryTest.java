package uk.gov.hmcts.ccd.definition.store.excel.parser;


import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class EntityToDefinitionDataItemRegistryTest {

    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @BeforeEach
    public void setUp() {
        entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
    }

    @Test
    public void addEntityToRegistry_definitionDataItemCanBeRetreived() {

        DefinitionDataItem definitionDataItem = new DefinitionDataItem("");
        Object entity = new Object();

        entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(entity, definitionDataItem);
        assertEquals(Optional.of(definitionDataItem), entityToDefinitionDataItemRegistry.getForEntity(entity));

    }

    @Test
    public void entityNotInRegistry_nullDefinitionDataItemRetreived() {

        assertEquals(Optional.empty(), entityToDefinitionDataItemRegistry.getForEntity(new Object()));

    }

}
