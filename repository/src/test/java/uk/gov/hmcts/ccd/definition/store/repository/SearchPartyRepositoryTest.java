package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;

import java.util.List;

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
public class SearchPartyRepositoryTest {

    private static final String CASE_TYPE_REFERENCE = "CaseTypeA";

    private static final String SEARCH_PARTY_NAME = "Test Name";
    private static final String SEARCH_PARTY_EMAIL_ADDRESS = "email@mail.com";
    private static final String SEARCH_PARTY_ADDRESS_LINE_1 = "Address Line 1";
    private static final String SEARCH_PARTY_POST_CODE = "Post Code";
    private static final String SEARCH_PARTY_DOB = "10/10/1910";
    private static final String SEARCH_PARTY_DOD = "10/10/1910";

    private static final String SEARCH_PARTY_COLLECTION_FIELD_NAME = "Name";

    @Autowired
    private SearchPartyRepository searchPartyRepository;

    @Autowired
    private TestHelper testHelper;

    private CaseTypeEntity latestCaseType;

    @Before
    public void setUp() {
        latestCaseType = testHelper.createCaseType(CASE_TYPE_REFERENCE, CASE_TYPE_REFERENCE);

        createSearchPartyEntity(latestCaseType);
    }

    @Test
    public void shouldGetSearchPartyEntity() {
        List<SearchPartyEntity> result = searchPartyRepository
            .findSearchPartyEntityByCaseType(CASE_TYPE_REFERENCE);

        assertAll(
            () -> assertThat(result, hasSize(1)),
            () -> assertThat(result.get(0).getCaseType(), is(latestCaseType)),
            () -> assertThat(result, hasItem(hasProperty("searchPartyName", is(SEARCH_PARTY_NAME))))
        );
    }

    @Test
    public void shouldReturnNoSearchPartyResultForUnknownParameters() {
        List<SearchPartyEntity> result = searchPartyRepository
            .findSearchPartyEntityByCaseType("IncorrectCaseType");

        assertAll(
            () -> assertThat(result, hasSize(0))
        );
    }

    private SearchPartyEntity createSearchPartyEntity(CaseTypeEntity caseType) {
        SearchPartyEntity searchPartyEntity = new SearchPartyEntity();

        searchPartyEntity.setCaseType(caseType);
        searchPartyEntity.setSearchPartyName(SEARCH_PARTY_NAME);
        searchPartyEntity.setSearchPartyEmailAddress(SEARCH_PARTY_EMAIL_ADDRESS);
        searchPartyEntity.setSearchPartyAddressLine1(SEARCH_PARTY_ADDRESS_LINE_1);
        searchPartyEntity.setSearchPartyPostCode(SEARCH_PARTY_POST_CODE);
        searchPartyEntity.setSearchPartyDob(SEARCH_PARTY_DOB);
        searchPartyEntity.setSearchPartyDod(SEARCH_PARTY_DOD);
        searchPartyEntity.setSearchPartyCollectionFieldName(SEARCH_PARTY_COLLECTION_FIELD_NAME);

        return searchPartyRepository.save(searchPartyEntity);
    }
}
