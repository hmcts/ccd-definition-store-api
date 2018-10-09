package uk.gov.hmcts.ccd.definition.store.domain.service.display;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.GenericLayoutRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;
import uk.gov.hmcts.ccd.definition.store.repository.model.*;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DisplayServiceTest {

    @Mock
    private GenericLayoutRepository genericLayoutRepository;

    @Mock
    private DisplayGroupAdapterService displayGroupAdapterService;

    @Mock
    private DisplayGroupRepository displayGroupRepository;

    @Mock
    private EntityToResponseDTOMapper entityToResponseDTOMapper;

    @InjectMocks
    private DisplayService classUnderTest;

    @BeforeEach
    public void setUpMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Nested
    class FindTabStructureForCaseTypeTests {

        @Test
        public void shouldReturnCaseTabCollectionWithEmptyTabsAndChannelsList_whenNoDisplayGroupEntitiesForCaseType() {

            String caseTypeId = "CaseTypeId";

            when(displayGroupRepository.findTabsByCaseTypeReference(any())).thenReturn(
                Collections.emptyList()
            );

            CaseTabCollection caseTabCollection = classUnderTest.findTabStructureForCaseType(caseTypeId);

            assertTrue(caseTabCollection.getTabs().isEmpty());
            assertTrue(caseTabCollection.getChannels().isEmpty());

        }

        @Test
        public void shouldReturnCaseTabCollectionWithPopulatedChannelsAndTabsLists_whenDisplayGroupEntitiesExistForCaseType() {

            String caseTypeId = "CaseTypeId";

            String channel1 = "Channel1";
            String channel2 = "Channel2";
            String channel3 = "Channel3";

            DisplayGroupEntity displayGroupEntity1 = displayGroupEntity(channel1);
            DisplayGroupEntity displayGroupEntity2 = displayGroupEntity(channel2);
            DisplayGroupEntity displayGroupEntity3 = displayGroupEntity(channel3);

            when(displayGroupRepository.findTabsByCaseTypeReference(any())).thenReturn(
                Arrays.asList(displayGroupEntity1, displayGroupEntity2, displayGroupEntity3)
            );

            CaseTypeTab caseTypeTab1 = new CaseTypeTab();
            CaseTypeTab caseTypeTab2 = new CaseTypeTab();
            CaseTypeTab caseTypeTab3 = new CaseTypeTab();

            when(entityToResponseDTOMapper.map(displayGroupEntity1)).thenReturn(caseTypeTab1);
            when(entityToResponseDTOMapper.map(displayGroupEntity2)).thenReturn(caseTypeTab2);
            when(entityToResponseDTOMapper.map(displayGroupEntity3)).thenReturn(caseTypeTab3);

            CaseTabCollection caseTabCollection = classUnderTest.findTabStructureForCaseType(caseTypeId);

            assertEquals(caseTypeId, caseTabCollection.getCaseTypeId());
            assertEquals(3, caseTabCollection.getTabs().size());
            assertThat(caseTabCollection.getTabs(), hasItems(caseTypeTab1, caseTypeTab2, caseTypeTab3));

            verify(displayGroupRepository).findTabsByCaseTypeReference(eq(caseTypeId));


        }

        private DisplayGroupEntity displayGroupEntity(String channel) {
            DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
            displayGroupEntity.setChannel(channel);
            return displayGroupEntity;
        }

    }

    @Nested
    class FindSearchInputDefinitionForCaseType {

        @Test
        public void shouldReturnEquivalentSearchInputDefinition_whenSearchInputCaseFieldEntitiesExist() {

            String caseTypeId = "CaseTypeId";

            SearchInputCaseFieldEntity searchInputCaseFieldEntity1 = new SearchInputCaseFieldEntity();
            SearchInputCaseFieldEntity searchInputCaseFieldEntity2 = new SearchInputCaseFieldEntity();
            SearchInputCaseFieldEntity searchInputCaseFieldEntity3 = new SearchInputCaseFieldEntity();

            when(genericLayoutRepository.findSearchInputsByCaseTypeReference(any())).thenReturn(
                Arrays.asList(
                    searchInputCaseFieldEntity1, searchInputCaseFieldEntity2, searchInputCaseFieldEntity3
                )
            );

            SearchInputField searchInputField1 = new SearchInputField();
            SearchInputField searchInputField2 = new SearchInputField();
            SearchInputField searchInputField3 = new SearchInputField();

            when(entityToResponseDTOMapper.map(searchInputCaseFieldEntity1)).thenReturn(searchInputField1);
            when(entityToResponseDTOMapper.map(searchInputCaseFieldEntity2)).thenReturn(searchInputField2);
            when(entityToResponseDTOMapper.map(searchInputCaseFieldEntity3)).thenReturn(searchInputField3);

            SearchInputDefinition searchInputDefinition = classUnderTest.findSearchInputDefinitionForCaseType(caseTypeId);

            assertEquals(caseTypeId, searchInputDefinition.getCaseTypeId());
            assertEquals(3, searchInputDefinition.getFields().size());
            assertThat(searchInputDefinition.getFields(), hasItems(searchInputField1, searchInputField2, searchInputField3));

        }

        @Test
        public void shouldReturnSearchInputDefinitionWithEmptySearchInputFieldList_whenNoSearchInputCaseFieldEntitiesForCaseType() {

            String caseTypeId = "CaseTypeId";

            when(genericLayoutRepository.findSearchInputsByCaseTypeReference(any())).thenReturn(
                Collections.emptyList()
            );

            SearchInputDefinition searchInputDefinition = classUnderTest.findSearchInputDefinitionForCaseType(caseTypeId);

            assertTrue(searchInputDefinition.getFields().isEmpty());

        }

    }

    @Nested
    class FindSearchResultDefinitionForCaseType {

        @Test
        public void shouldReturnEquivalentSearchResultDefinition_whenSearchResultCaseFieldEntitiesExist() {

            String caseTypeId = "CaseTypeId";

            SearchResultCaseFieldEntity searchInputCaseFieldEntity1 = new SearchResultCaseFieldEntity();
            SearchResultCaseFieldEntity searchInputCaseFieldEntity2 = new SearchResultCaseFieldEntity();
            SearchResultCaseFieldEntity searchInputCaseFieldEntity3 = new SearchResultCaseFieldEntity();

            when(genericLayoutRepository.findSearchResultsByCaseTypeReference(any())).thenReturn(
                Arrays.asList(
                    searchInputCaseFieldEntity1, searchInputCaseFieldEntity2, searchInputCaseFieldEntity3
                )
            );

            SearchResultsField searchResultsField1 = new SearchResultsField();
            SearchResultsField searchResultsField2 = new SearchResultsField();
            SearchResultsField searchResultsField3 = new SearchResultsField();

            when(entityToResponseDTOMapper.map(searchInputCaseFieldEntity1)).thenReturn(searchResultsField1);
            when(entityToResponseDTOMapper.map(searchInputCaseFieldEntity2)).thenReturn(searchResultsField2);
            when(entityToResponseDTOMapper.map(searchInputCaseFieldEntity3)).thenReturn(searchResultsField3);

            SearchResultDefinition searchResultDefinition = classUnderTest.findSearchResultDefinitionForCaseType(caseTypeId);

            assertEquals(caseTypeId, searchResultDefinition.getCaseTypeId());
            assertEquals(3, searchResultDefinition.getFields().size());
            assertThat(searchResultDefinition.getFields(), hasItems(searchResultsField1, searchResultsField2, searchResultsField3));

        }

        @Test
        public void shouldReturnSearchResultDefinitionWithEmptySearchResultFieldList_whenNoSearchResultCaseFieldEntitiesForCaseType() {

            String caseTypeId = "CaseTypeId";

            when(genericLayoutRepository.findSearchResultsByCaseTypeReference(any())).thenReturn(
                Collections.emptyList()
            );

            SearchResultDefinition searchInputDefinition = classUnderTest.findSearchResultDefinitionForCaseType(caseTypeId);

            assertTrue(searchInputDefinition.getFields().isEmpty());

        }

    }

    @Nested
    class FindWorkbasketInputDefinitionForCaseType {

        @Test
        void shouldReturnEquivalentWorkbasketInputDefinition_whenWorkbasketInputCaseFieldEntitiesExist() {

            String caseTypeId = "CaseTypeId";

            WorkBasketInputCaseFieldEntity workbasketInputCaseFieldEntity1 = new WorkBasketInputCaseFieldEntity();
            WorkBasketInputCaseFieldEntity workbasketInputCaseFieldEntity2 = new WorkBasketInputCaseFieldEntity();
            WorkBasketInputCaseFieldEntity workbasketInputCaseFieldEntity3 = new WorkBasketInputCaseFieldEntity();

            when(genericLayoutRepository.findWorkbasketInputByCaseTypeReference(any())).thenReturn(
                Arrays.asList(
                    workbasketInputCaseFieldEntity1, workbasketInputCaseFieldEntity2, workbasketInputCaseFieldEntity3
                )
            );

            WorkbasketInputField workbasketInputField1 = new WorkbasketInputField();
            WorkbasketInputField workbasketInputField2 = new WorkbasketInputField();
            WorkbasketInputField workbasketInputField3 = new WorkbasketInputField();

            when(entityToResponseDTOMapper.map(workbasketInputCaseFieldEntity1)).thenReturn(workbasketInputField1);
            when(entityToResponseDTOMapper.map(workbasketInputCaseFieldEntity2)).thenReturn(workbasketInputField2);
            when(entityToResponseDTOMapper.map(workbasketInputCaseFieldEntity3)).thenReturn(workbasketInputField3);

            WorkbasketInputDefinition workbasketInputDefinition = classUnderTest.findWorkBasketInputDefinitionForCaseType(caseTypeId);

            assertEquals(caseTypeId, workbasketInputDefinition.getCaseTypeId());
            assertEquals(3, workbasketInputDefinition.getFields().size());
            assertThat(workbasketInputDefinition.getFields(), hasItems(workbasketInputField1, workbasketInputField2, workbasketInputField3));

        }

        @Test
        void shouldReturnWorkbasketInputDefinitionWithEmptyWorkbasketInputFieldList_whenNoWorkbasketInputCaseFieldEntitiesForCaseType() {

            String caseTypeId = "CaseTypeId";

            when(genericLayoutRepository.findWorkbasketInputByCaseTypeReference(any())).thenReturn(
                Collections.emptyList()
            );

            WorkbasketInputDefinition workbasketInputDefinition = classUnderTest.findWorkBasketInputDefinitionForCaseType(caseTypeId);

            assertTrue(workbasketInputDefinition.getFields().isEmpty());
        }
    }

    @Nested
    class FindWorkBasketResultDefinitionForCaseType {

        @Test
        public void shouldReturnEquivalentWorkBasketResult_whenWorkBasketCaseFieldEntitiesExist() {

            String caseTypeId = "CaseTypeId";

            WorkBasketCaseFieldEntity workBasketCaseFieldEntity1 = new WorkBasketCaseFieldEntity();
            WorkBasketCaseFieldEntity workBasketCaseFieldEntity2 = new WorkBasketCaseFieldEntity();
            WorkBasketCaseFieldEntity workBasketCaseFieldEntity3 = new WorkBasketCaseFieldEntity();

            when(genericLayoutRepository.findWorkbasketByCaseTypeReference(any())).thenReturn(
                Arrays.asList(
                    workBasketCaseFieldEntity1, workBasketCaseFieldEntity2, workBasketCaseFieldEntity3
                )
            );

            WorkBasketResultField workBasketResultField1 = new WorkBasketResultField();
            WorkBasketResultField workBasketResultField2 = new WorkBasketResultField();
            WorkBasketResultField workBasketResultField3 = new WorkBasketResultField();

            when(entityToResponseDTOMapper.map(workBasketCaseFieldEntity1)).thenReturn(workBasketResultField1);
            when(entityToResponseDTOMapper.map(workBasketCaseFieldEntity2)).thenReturn(workBasketResultField2);
            when(entityToResponseDTOMapper.map(workBasketCaseFieldEntity3)).thenReturn(workBasketResultField3);

            WorkBasketResult workBasketResult = classUnderTest.findWorkBasketDefinitionForCaseType(caseTypeId);

            assertEquals(caseTypeId, workBasketResult.getCaseTypeId());
            assertEquals(3, workBasketResult.getFields().size());
            assertThat(workBasketResult.getFields(), hasItems(workBasketResultField1, workBasketResultField2, workBasketResultField3));

        }

        @Test
        public void shouldReturnWorkBasketResultWithEmptySearchResultFieldList_whenNoWorkBasketCaseFieldEntitiesForCaseType() {

            String caseTypeId = "CaseTypeId";

            when(genericLayoutRepository.findSearchResultsByCaseTypeReference(any())).thenReturn(
                Collections.emptyList()
            );

            WorkBasketResult searchInputDefinition = classUnderTest.findWorkBasketDefinitionForCaseType(caseTypeId);

            assertTrue(searchInputDefinition.getFields().isEmpty());

        }

    }

    @Nested
    class FindWizardPageForCaseType {

        @Test
        public void delegatesToDisplayGroupAdapterService() {
            String caseTypeId = "CaseTypeId";
            String createCaseEvent = "createCaseEvent";

            classUnderTest.findWizardPageForCaseType(caseTypeId, createCaseEvent);

            verify(displayGroupAdapterService).findWizardPagesByCaseTypeId(caseTypeId, createCaseEvent);
        }
    }
}
