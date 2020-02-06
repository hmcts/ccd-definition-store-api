package uk.gov.hmcts.ccd.definition.store.repository;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionUiConfigEntity;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class JurisdictionUiConfigRepositoryTest {

    @Autowired
    private JurisdictionUiConfigRepository jurisdictionUiConfigRepository;

    @Autowired
    private TestHelper testHelper;
    
    List<JurisdictionEntity> jurisdictions;

    @Before
    public void setup() {
    	jurisdictions = new ArrayList<>();
    	jurisdictions.add(testHelper.createJurisdiction("ref1", "name1", "desc1"));
    	jurisdictions.add(testHelper.createJurisdiction("ref2", "name2", "desc2"));
    	jurisdictions.add(testHelper.createJurisdiction("ref3", "name3", "desc3"));
    	createJurisdictionUiConfig(true, jurisdictions.get(0));
    	createJurisdictionUiConfig(false, jurisdictions.get(1));
    	createJurisdictionUiConfig(false, jurisdictions.get(2));
    }

    @Test
    public void getSpecificJurisdictionUiConfig() {

        JurisdictionUiConfigEntity result = jurisdictionUiConfigRepository.findByJurisdictionId("ref1");
        
        assertAll(
            () -> assertEquals(true, result.getShuttered()),
            () -> assertThat(result.getJurisdiction(), hasProperty("reference", is("ref1")))
        );
    }

    @Test
    public void getJurisdictionUiConfigsByReferences() {
    	List<String> references = new ArrayList<>();
    	references.add("ref1");
    	references.add("ref3");
    	
    	List<JurisdictionUiConfigEntity> result = jurisdictionUiConfigRepository.findAllByReference(references);

        assertAll(
                () -> assertThat(result, hasSize(2)),
                () -> assertThat(result, hasItem(hasProperty("jurisdiction", hasProperty("reference", is("ref1"))))),
                () -> assertThat(result, hasItem(hasProperty("jurisdiction", hasProperty("reference", is("ref3"))))),
                () -> assertThat(result, hasItem(hasProperty("shuttered", is(true)))),
                () -> assertThat(result, hasItem(hasProperty("shuttered", is(false))))
        );
    }
    
    private JurisdictionUiConfigEntity createJurisdictionUiConfig(Boolean shuttered, JurisdictionEntity jurisdiction) {
        final JurisdictionUiConfigEntity jurisdictionUiConfig = new JurisdictionUiConfigEntity();
        jurisdictionUiConfig.setShuttered(shuttered);
        jurisdictionUiConfig.setJurisdiction(jurisdiction);
        return jurisdictionUiConfigRepository.save(jurisdictionUiConfig);
    }

}
