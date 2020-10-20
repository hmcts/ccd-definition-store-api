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

import javax.persistence.EntityManager;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class DraftDefinitionRepositoryTest {

    @Autowired
    private DraftDefinitionRepository classUnderTest;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    private JurisdictionEntity testJurisdiction;
    private DefinitionEntity definitionEntity;

    @Before
    public void setUp() throws IOException {
        testJurisdiction = testHelper.createJurisdiction();

        definitionEntity = testHelper.buildDefinition(testJurisdiction, "Test definition");
        definitionEntity.setCaseTypes("CaseType1,CaseType2");
        definitionEntity.setVersion(1);
        definitionEntity.setStatus(DefinitionStatus.DRAFT);
    }

    @Test
    public void shouldCreateNewDefinition() {
        classUnderTest.save(definitionEntity);

        entityManager.flush();
        entityManager.clear();

        DefinitionEntity savedDefinitionEntity =
            classUnderTest.findLatestByJurisdictionId(testJurisdiction.getReference());

        assertNotNull(savedDefinitionEntity.getId());
        assertThat(savedDefinitionEntity.getJurisdiction().getReference(), is(testJurisdiction.getReference()));
        assertThat(savedDefinitionEntity.getJurisdiction().getName(), is(testJurisdiction.getName()));
        assertThat(savedDefinitionEntity.getJurisdiction().getVersion(), is(testJurisdiction.getVersion()));
        assertThat(savedDefinitionEntity.getCaseTypes(), is(definitionEntity.getCaseTypes()));
        assertThat(savedDefinitionEntity.getDescription(), is(definitionEntity.getDescription()));
        assertThat(savedDefinitionEntity.getVersion(), is(definitionEntity.getVersion()));
        assertThat(savedDefinitionEntity.getStatus(), is(definitionEntity.getStatus()));
        assertThat(savedDefinitionEntity.getData(), is(definitionEntity.getData()));
        assertThat(savedDefinitionEntity.getAuthor(), is(definitionEntity.getAuthor()));
        assertNotNull(savedDefinitionEntity.getCreatedAt());
        assertNotNull(savedDefinitionEntity.getLastModified());
        assertThat(savedDefinitionEntity.isDeleted(), is(definitionEntity.isDeleted()));
    }
}
