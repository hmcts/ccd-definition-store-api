package uk.gov.hmcts.ccd.definition.store.domain.service.searchcriteria;

import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SearchCriteriaRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SearchCriteriaServiceImplTest {

    private SearchCriteriaServiceImpl sut;

    @Mock
    private SearchCriteriaRepository repository;

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sut = new SearchCriteriaServiceImpl(repository, dtoMapper, caseTypeRepository);
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

}
