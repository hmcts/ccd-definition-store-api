package uk.gov.hmcts.ccd.definition.store.domain.service.searchparty;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.SearchPartyRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchParty;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SearchPartyServiceImplTest {

    private SearchPartyServiceImpl sut;

    @Mock
    private SearchPartyRepository repository;

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        sut = new SearchPartyServiceImpl(repository, dtoMapper);
    }

    @Test
    @DisplayName(
        "Should get Search Party for the passed case type references")
    void shouldSearchPartyForValidCaseTypeId() {
        List<SearchPartyEntity> searchPartyEntities = Lists.newArrayList();
        searchPartyEntities.add(createSearchParty());
        searchPartyEntities.add(createSearchParty());
        doReturn(searchPartyEntities).when(repository).findByCaseTypeReferenceIn(anyList());
        List<SearchParty> valuesReturned = sut.findByCaseTypeReferences(Lists.newArrayList());
        Assert.assertEquals(2, valuesReturned.size());
    }

    @Test
    @DisplayName(
        "Should get Search Party for the passed search party name")
    void shouldSearchPartyForValidSearchPartyName() {
        List<SearchPartyEntity> searchPartyEntities = Lists.newArrayList();
        searchPartyEntities.add(createSearchParty());
        searchPartyEntities.add(createSearchParty());
        doReturn(searchPartyEntities).when(repository).findBySearchPartyName("name");
        List<SearchParty> valuesReturned = sut.findSearchPartyName("name");
        Assert.assertEquals(2, valuesReturned.size());
    }

    @Test
    @DisplayName(
        "Should get empty Role To Search Party list for the passed empty case type references")
    void shouldGetEmptySearchPartyListForEmptyCaseType() {
        List<SearchPartyEntity> searchPartyEntities = Lists.newArrayList();
        doReturn(searchPartyEntities).when(repository).findByCaseTypeReferenceIn(anyList());
        List<SearchParty> valuesReturned = sut.findByCaseTypeReferences(Lists.newArrayList("Test"));
        Assert.assertEquals(0, valuesReturned.size());
    }

    @Test
    @DisplayName(
        "Should save single Search Party Entity")
    void shouldSaveSingleSearchPartyEntity() {
        SearchPartyEntity mockedEntity = mock(SearchPartyEntity.class);
        List<SearchPartyEntity> searchPartyEntities = Lists.newArrayList(mockedEntity);
        sut.saveAll(searchPartyEntities);

        verify(repository, times(1)).saveAll(eq(searchPartyEntities));
    }

    @Test
    @DisplayName(
        "Should save multiple Search Party Entity")
    void shouldSaveMultipleSearchPartyEntities() {
        SearchPartyEntity mockedEntity1 = mock(SearchPartyEntity.class);
        SearchPartyEntity mockedEntity2 = mock(SearchPartyEntity.class);
        SearchPartyEntity mockedEntity3 = mock(SearchPartyEntity.class);
        List<SearchPartyEntity> searchPartyEntities = Lists.newArrayList(mockedEntity1, mockedEntity2, mockedEntity3);
        sut.saveAll(searchPartyEntities);

        verify(repository, times(1)).saveAll(eq(searchPartyEntities));
    }

    private SearchPartyEntity createSearchParty() {
        SearchPartyEntity searchPartyEntity = new SearchPartyEntity();
        searchPartyEntity.setCaseType(createCaseTypeEntity());
        searchPartyEntity.setSearchPartyName("test name");
        searchPartyEntity.setSearchPartyEmailAddress("testEmail@mail.com");
        searchPartyEntity.setSearchPartyAddressLine1("Test Address");
        searchPartyEntity.setSearchPartyPostCode("J21 2HS");
        searchPartyEntity.setSearchPartyDob("date of birth");

        return searchPartyEntity;
    }

    private CaseTypeEntity createCaseTypeEntity() {
        CaseTypeEntity entity = new CaseTypeEntity();
        entity.setReference("TestCaseTypeRef");
        return entity;
    }

}
