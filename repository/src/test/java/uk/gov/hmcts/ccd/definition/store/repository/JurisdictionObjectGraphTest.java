package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
/**
 * Tests that the object graph for Jurisdiction entity is correctly saved and fetched.
 */
public class JurisdictionObjectGraphTest {

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private EntityManager entityManager;

    private JurisdictionEntity jurisdiction;
    private CaseTypeEntity caseType1;

    @Before
    public void setUp() {
        jurisdiction = testHelper.createJurisdiction("TEST", "Test", "Dummy jurisdiction");
        caseType1 = testHelper.createCaseTypeEntity("TestCase", "1st Test case", jurisdiction,
            SecurityClassification.PUBLIC);
    }

    @Test
    public void retrieveJurisdiction() {
        // Clear down the entity manager to ensure we are actually reading from the DB
        entityManager.flush();
        entityManager.clear();

        final JurisdictionEntity fetchedJurisdiction = jurisdictionRepository.findOne(jurisdiction.getId());
        assertThat(fetchedJurisdiction.getCreatedAt(), is(notNullValue()));
        assertThat(fetchedJurisdiction.getCreatedAt().isBefore(LocalDateTime.now()), is(true));
        assertThat(fetchedJurisdiction.getReference(), equalTo(jurisdiction.getReference()));

        // Check case types
        assertThat(fetchedJurisdiction.getCaseTypes(), hasSize(2));
        final CaseTypeEntity fetchedCaseType1 = fetchedJurisdiction.getCaseTypes().get(0);
        assertThat(fetchedCaseType1.getReference(), equalTo(caseType1.getReference()));
        assertThat(fetchedCaseType1.getName(), equalTo(caseType1.getName()));
        assertThat(fetchedCaseType1.getSecurityClassification(), equalTo(caseType1.getSecurityClassification()));
    }
}
