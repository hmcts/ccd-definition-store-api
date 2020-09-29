package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class EntityToDefinitionDataItemRegistryTest {

    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @Before
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
