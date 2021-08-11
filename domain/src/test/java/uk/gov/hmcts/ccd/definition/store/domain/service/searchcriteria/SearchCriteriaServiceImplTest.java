package uk.gov.hmcts.ccd.definition.store.domain.service.searchcriteria;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SearchCriteriaRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchCriteria;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SearchCriteriaServiceImplTest {

    public static final String OTHER_CASE_REFERENCE = "OtherCaseReference";

    private SearchCriteriaServiceImpl sut;

    @Mock
    private SearchCriteriaRepository repository;

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        sut = new SearchCriteriaServiceImpl(repository, dtoMapper, caseTypeRepository);
    }

    @Test
    @DisplayName(
        "Should get Search Criteria for the passed case type references")
    void shouldGetRoleToAccessProfilesForValidCaseTypeId() {
        List<SearchCriteriaEntity> searchCriteriaEntities = Lists.newArrayList();
        searchCriteriaEntities.add(createSearchCriteriaEntity());
        searchCriteriaEntities.add(createSearchCriteriaEntity());
        doReturn(searchCriteriaEntities).when(repository).findByCaseTypeReferenceIn(anyList());
        List<String> references = Lists.newArrayList("Test", "Divorce");
        List<SearchCriteria> valuesReturned = sut.findByCaseTypeReferences(references);
        Assert.assertEquals(2, valuesReturned.size());
    }

    @Test
    @DisplayName(
        "Should get empty Role To Access Profiles list for the passed empty case type references")
    void shouldGetEmptyRoleToAccessProfilesListForEmptyCaseType() {
        List<SearchCriteriaEntity> searchCriteriaEntities = Lists.newArrayList();
        doReturn(searchCriteriaEntities).when(repository).findByCaseTypeReferenceIn(anyList());
        List<SearchCriteria> valuesReturned = sut.findByCaseTypeReferences(Lists.newArrayList());
        Assert.assertEquals(0, valuesReturned.size());
    }

    @Test
    @DisplayName(
        "Should save the passed entities")
    void shouldSaveEntity() {
        SearchCriteriaEntity searchCriteriaEntity = mock(SearchCriteriaEntity.class);
        List<SearchCriteriaEntity> entitiesToSave = new ArrayList<>();
        entitiesToSave.add(searchCriteriaEntity);
        sut.saveAll(entitiesToSave);
        verify(repository, times(1)).saveAll(eq(entitiesToSave));
    }

    private SearchCriteriaEntity createSearchCriteriaEntity() {
        SearchCriteriaEntity searchCriteriaEntity = new SearchCriteriaEntity();
        searchCriteriaEntity.setCaseType(createCaseTypeEntity());
        searchCriteriaEntity.setOtherCaseReference(OTHER_CASE_REFERENCE);
        return searchCriteriaEntity;
    }

    private CaseTypeEntity createCaseTypeEntity() {
        CaseTypeEntity entity = new CaseTypeEntity();
        entity.setReference("TestCaseTypeRef");
        return entity;
    }
}
