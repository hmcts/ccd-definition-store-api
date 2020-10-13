package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.NoCConfigEntity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class NoCConfigRepositoryTest {

    @Autowired
    private NoCConfigRepository noCConfigRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    private static final String CASE_TYPE_1 = "FT_MasterCaseType";
    private static final String CASE_TYPE_2 = "FT_ComplexCollection";

    @Before
    public void setUp() {
        CaseTypeEntity caseTypeEntity = testHelper.createCaseType(CASE_TYPE_1, "Master case type 1");
        CaseTypeEntity caseTypeEntity2 = testHelper.createCaseType(CASE_TYPE_2, "Master case type 2");
        NoCConfigEntity noCConfigEntity = createNoCConfigEntity(caseTypeEntity);
        NoCConfigEntity noCConfigEntity2 = createNoCConfigEntity(caseTypeEntity2);
        saveNoCConfigAndFlushSession(noCConfigEntity, noCConfigEntity2);
    }

    @Test
    public void shouldDeleteNocConfigsForProvidedReference() {
        int deletedCount = noCConfigRepository.deleteByCaseTypeReference(CASE_TYPE_1);

        List<NoCConfigEntity> configEntities = noCConfigRepository.findAll();
        assertAll(
            () -> assertThat(deletedCount, is(1)),
            () -> assertThat(configEntities.size(), is(1)),
            () -> assertThat(configEntities.get(0).getCaseType().getReference(), is(CASE_TYPE_2))
        );
    }

    @Test
    public void shouldNotDeleteAnyNoCConfigEntriesWhenReferenceDoesNotExist() {
        int deletedCount = noCConfigRepository.deleteByCaseTypeReference("UNKNOWN_REFERENCE");

        List<NoCConfigEntity> noCConfigEntities = noCConfigRepository.findAll();
        assertAll(
            () -> assertThat(deletedCount, is(0)),
            () -> assertThat(noCConfigEntities.size(), is(2))
        );
    }

    private NoCConfigEntity createNoCConfigEntity(CaseTypeEntity caseTypeEntity) {
        NoCConfigEntity noCConfigEntity = new NoCConfigEntity();
        noCConfigEntity.setCaseType(caseTypeEntity);
        noCConfigEntity.setReasonsRequired(true);
        noCConfigEntity.setNocActionInterpretationRequired(false);
        return noCConfigEntity;
    }

    private void saveNoCConfigAndFlushSession(NoCConfigEntity... noCConfigEntities) {
        for (NoCConfigEntity entity : noCConfigEntities) {
            noCConfigRepository.save(entity);
        }
        entityManager.flush();
        entityManager.clear();
    }
}
