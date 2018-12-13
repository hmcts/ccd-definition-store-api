package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus.DRAFT;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus.PUBLISHED;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class DraftDefinitionRepositoryDecoratorTest {

    @Autowired
    private DraftDefinitionRepository repository;

    @Autowired
    private TestHelper testHelper;

    private JurisdictionEntity testJurisdiction;

    private DraftDefinitionRepositoryDecorator classUnderTest;

    private static final String JURISDICTION_ID = "G4TqDskxuR";

    @Before
    public void setUp() throws IOException {
        testJurisdiction = testHelper.createJurisdiction();
        final JurisdictionEntity
            jurisdictionForFindTests =
            testHelper.createJurisdiction(JURISDICTION_ID, "lo0it9cg5E", "qdXfga0buG9cIum1yns8");
        classUnderTest = new DraftDefinitionRepositoryDecorator(repository);
        classUnderTest.save(testHelper.buildDefinition(jurisdictionForFindTests, "T1", PUBLISHED));
        classUnderTest.save(testHelper.buildDefinition(jurisdictionForFindTests, "T2", DRAFT));
        classUnderTest.save(testHelper.buildDefinition(jurisdictionForFindTests, "T3", DRAFT));
    }

    @Test
    public void shouldSetVersionOnCreatingDefinition() throws IOException {
        final DefinitionEntity definitionEntity1 = testHelper.buildDefinition(testJurisdiction, "Test definition");

        final DefinitionEntity savedDefinitionEntity = classUnderTest.save(definitionEntity1);
        final DefinitionEntity retrievedDefinitionEntity = repository.findOne(savedDefinitionEntity.getId());
        assertThat(retrievedDefinitionEntity.getVersion(), is(1));
        assertThat(retrievedDefinitionEntity.getStatus(), is(DRAFT));

        final DefinitionEntity definitionEntity2 = testHelper.buildDefinition(testJurisdiction, "Test definition");
        final DefinitionEntity savedDefinitionEntity2 = classUnderTest.save(definitionEntity2);
        assertThat(savedDefinitionEntity2.getVersion(), is(2));
    }

    @Test
    public void shouldFindAListOfDefinitions() {
        final List<DefinitionEntity> entities = classUnderTest.findByJurisdictionId(JURISDICTION_ID);
        assertThat(entities.size(), is(3));
        // assert that entities are retrived in reverse order they are created
        assertDefinitionEntity(entities.get(0), "T3", DRAFT);
        assertDefinitionEntity(entities.get(1), "T2", DRAFT);
        assertDefinitionEntity(entities.get(2), "T1", PUBLISHED);
    }

    @Test
    public void shouldFindASpecificVersion() {
        final DefinitionEntity definitionEntity = classUnderTest.findByJurisdictionIdAndVersion(JURISDICTION_ID, 2);
        assertDefinitionEntity(definitionEntity, "T2", DRAFT);
    }

    @Test
    public void shouldFindLatestVersionIfVersionNotSpecified() {
        final DefinitionEntity definitionEntity = classUnderTest.findByJurisdictionIdAndVersion(JURISDICTION_ID, null);
        assertDefinitionEntity(definitionEntity, "T3", DRAFT);
    }

    @Test
    public void shouldReturnNullWhenNoMatchOnJurisdiction() {
        assertNull(classUnderTest.findByJurisdictionIdAndVersion("n", null));
    }

    @Test
    public void shouldReturnNullWhenNoMatchOnVersion() {
        assertNull(classUnderTest.findByJurisdictionIdAndVersion(JURISDICTION_ID, 667));
    }

    @Test
    public void shouldFindAnEmptyListWhenNoMatchOnJurisdiction() {
        final List<DefinitionEntity> entities = classUnderTest.findByJurisdictionId("y");
        assertTrue(entities.isEmpty());
    }

    private void assertDefinitionEntity(final DefinitionEntity definitionEntity,
                                        final String description,
                                        final DefinitionStatus status) {
        assertThat(definitionEntity.getDescription(), is(description));
        assertThat(definitionEntity.getStatus(), is(status));
    }
}
