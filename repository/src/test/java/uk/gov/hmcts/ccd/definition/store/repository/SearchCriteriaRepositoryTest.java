package uk.gov.hmcts.ccd.definition.store.repository;


import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

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
public class SearchCriteriaRepositoryTest {

    private static final String CASE_TYPE_REFERENCE = "CaseTypeA";

    private static final String OTHER_CASE_REFERENCE = "OtherCaseReference";

    @Autowired
    private SearchCriteriaRepository searchCriteriaRepository;

    @Autowired
    private TestHelper testHelper;

    private CaseTypeEntity latestCaseType;

    @BeforeEach
    public void setUp() {
        latestCaseType = testHelper.createCaseType(CASE_TYPE_REFERENCE, CASE_TYPE_REFERENCE);

        createSearchCriteriaEntity(latestCaseType);
    }

    @Test
    public void shouldGetSearchCriteriaEntity() {
        List<SearchCriteriaEntity> result = searchCriteriaRepository
            .findSearchCriteriaEntityByCaseType(CASE_TYPE_REFERENCE);

        assertAll(
            () -> assertThat(result, hasSize(1)),
            () -> assertThat(result.get(0).getCaseType(), is(latestCaseType)),
            () -> assertThat(result, hasItem(hasProperty("otherCaseReference", is(OTHER_CASE_REFERENCE))))
        );
    }

    @Test
    public void shouldReturnNoSearchCriteriaResultForUnknownParameters() {
        List<SearchCriteriaEntity> result = searchCriteriaRepository
            .findSearchCriteriaEntityByCaseType("IncorrectCaseType");

        assertThat(result, hasSize(0));
    }



    private SearchCriteriaEntity createSearchCriteriaEntity(CaseTypeEntity caseType) {
        SearchCriteriaEntity searchCriteriaEntity = new SearchCriteriaEntity();

        searchCriteriaEntity.setCaseType(caseType);
        searchCriteriaEntity.setOtherCaseReference(OTHER_CASE_REFERENCE);

        return searchCriteriaRepository.save(searchCriteriaEntity);
    }
}
