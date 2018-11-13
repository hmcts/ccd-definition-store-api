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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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

    @Before
    public void setUp() {
        testJurisdiction = testHelper.createJurisdiction();
    }

    @Test
    public void shouldSetVersionOnCreatingDefinition() throws IOException {
        final DefinitionEntity definitionEntity1 = testHelper.buildDefinition(testJurisdiction, "Test definition");
        final DraftDefinitionRepositoryDecorator classUnderTest = new DraftDefinitionRepositoryDecorator(repository);

        final DefinitionEntity savedDefinitionEntity = classUnderTest.save(definitionEntity1);
        final DefinitionEntity retrievedDefinitionEntity = repository.findOne(savedDefinitionEntity.getId());
        assertThat(retrievedDefinitionEntity.getVersion(), is(1));
        assertThat(retrievedDefinitionEntity.getStatus(), is(DefinitionStatus.DRAFT));

        final DefinitionEntity definitionEntity2 = testHelper.buildDefinition(testJurisdiction, "Test definition");
        final DefinitionEntity savedDefinitionEntity2 = classUnderTest.save(definitionEntity2);
        assertThat(savedDefinitionEntity2.getVersion(), is(2));
    }
}
