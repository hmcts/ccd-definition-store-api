package uk.gov.hmcts.ccd.definition.store.repository;


import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class JurisdictionRepositoryTest {

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private TestHelper testHelper;

    @BeforeEach
    void setup() {
        testHelper.createJurisdiction("ref1", "name1", "desc1");
        testHelper.createJurisdiction("ref1", "name1.2", "desc1.2");
        testHelper.createJurisdiction("ref2", "name2", "desc2");
        testHelper.createJurisdiction("ref3", "name3", "desc3");
        testHelper.createJurisdiction("ref4", "name4", "desc4");
        testHelper.createJurisdiction("ref4", "name4.2", "desc4.2");
        testHelper.createJurisdiction("ref4", "name4.3", "desc4.3");
    }

    @Test
    void getSpecificJurisdictionDefinitions() {

        List<JurisdictionEntity> result = jurisdictionRepository.findAllLatestVersionByReference(
            newArrayList("ref1", "ref2", "ref4"));

        assertAll(
            () -> assertThat(result, hasSize(3)),
            () -> assertThat(result, hasItem(hasProperty("name", is("name1.2")))),
            () -> assertThat(result, hasItem(hasProperty("name", is("name2")))),
            () -> assertThat(result, hasItem(hasProperty("name", is("name4.3"))))
        );
    }

    @Test
    void getAllJurisdictionDefinitinon() {

        List<JurisdictionEntity> result = jurisdictionRepository.findAllLatestVersion();

        assertAll(
            () -> assertThat(result, hasSize(4)),
            () -> assertThat(result, hasItem(hasProperty("name", is("name1.2")))),
            () -> assertThat(result, hasItem(hasProperty("name", is("name2")))),
            () -> assertThat(result, hasItem(hasProperty("name", is("name3")))),
            () -> assertThat(result, hasItem(hasProperty("name", is("name4.3"))))
        );
    }


}
