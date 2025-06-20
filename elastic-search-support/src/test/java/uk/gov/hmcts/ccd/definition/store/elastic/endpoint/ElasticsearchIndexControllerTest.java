package uk.gov.hmcts.ccd.definition.store.elastic.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticDefinitionImportListener;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticGlobalSearchListener;
import uk.gov.hmcts.ccd.definition.store.elastic.model.IndicesCreationResult;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;

class ElasticsearchIndexControllerTest {

    @InjectMocks
    private ElasticsearchIndexController controller;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @Mock
    private ElasticDefinitionImportListener elasticDefinitionImportListener;

    @Mock
    private ElasticGlobalSearchListener elasticGlobalSearchListener;

    @Captor
    private ArgumentCaptor<DefinitionImportedEvent> eventCaptor;

    private static final String JURISDICTION_1 = "J1";
    private static final String JURISDICTION_2 = "J2";
    private static final String CASE_TYPE_1 = "CT1";
    private static final String CASE_TYPE_2 = "CT2";
    private static final String CASE_TYPE_3 = "CT3";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ArrayList<CaseTypeEntity> caseTypes = new ArrayList<>();
        caseTypes.add(createCaseType(CASE_TYPE_1, JURISDICTION_1));
        caseTypes.add(createCaseType(CASE_TYPE_2, JURISDICTION_1));
        caseTypes.add(createCaseType(CASE_TYPE_3, JURISDICTION_2));
        when(caseTypeRepository.findAllLatestVersions()).thenReturn(caseTypes);
        when(caseTypeRepository.findAllLatestVersions(any())).thenReturn(List.of(caseTypes.get(0), caseTypes.get(2)));
        when(caseTypeRepository.findAllCaseTypeIds()).thenReturn(List.of(CASE_TYPE_1, CASE_TYPE_2, CASE_TYPE_3));
    }

    @Test
    void shouldTriggerElasticsearchIndicesCreationWithEmptyList() {
        IndicesCreationResult result = controller.createElasticsearchIndices(Collections.emptyList());
        verify(elasticDefinitionImportListener).initialiseElasticSearch(eventCaptor.capture());

        assertAll(
            () -> verify(caseTypeRepository).findAllLatestVersions(),
            () -> assertThat(result.getTotal(), is(3)),
            () -> assertThat(result.getCaseTypesByJurisdiction().keySet().size(), is(2)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_1).size(), is(2)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_2).size(), is(1)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_1), hasItem(CASE_TYPE_1)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_1), hasItem(CASE_TYPE_2)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_2), hasItem(CASE_TYPE_3))
        );
    }

    @Test
    void shouldTriggerElasticsearchIndicesCreationWithNullList() {
        IndicesCreationResult result = controller.createElasticsearchIndices(null);
        verify(elasticDefinitionImportListener).initialiseElasticSearch(eventCaptor.capture());

        assertAll(
            () -> verify(caseTypeRepository).findAllLatestVersions(),
            () -> assertThat(result.getTotal(), is(3)),
            () -> assertThat(result.getCaseTypesByJurisdiction().keySet().size(), is(2)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_1).size(), is(2)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_2).size(), is(1)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_1), hasItem(CASE_TYPE_1)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_1), hasItem(CASE_TYPE_2)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_2), hasItem(CASE_TYPE_3))
        );
    }

    @Test
    void shouldTriggerElasticsearchIndicesCreationWithProvidedCaseTypes() {
        IndicesCreationResult result = controller.createElasticsearchIndices(List.of(CASE_TYPE_1, CASE_TYPE_3));
        verify(elasticDefinitionImportListener).initialiseElasticSearch(eventCaptor.capture());
        DefinitionImportedEvent capturedEvent = eventCaptor.getValue();

        assertAll(
            () -> assertThat(capturedEvent.getContent().size(), is(2)),
            () -> assertThat(capturedEvent.getContent().getFirst().getReference(), is(CASE_TYPE_1)),
            () -> assertThat(capturedEvent.getContent().get(1).getReference(), is(CASE_TYPE_3)),
            () -> assertThat(result.getTotal(), is(2)),
            () -> assertThat(result.getCaseTypesByJurisdiction().keySet().size(), is(2)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_1).size(), is(1)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_2).size(), is(1)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_1), hasItem(CASE_TYPE_1)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get(JURISDICTION_2), hasItem(CASE_TYPE_3))
        );
    }

    @Test
    void shouldTriggerGSElasticsearchIndicesCreationWithEmptyList() {
        controller.createGlobalSearchElasticsearchIndex();

        assertAll(
            () -> verify(elasticGlobalSearchListener).initialiseElasticSearchForGlobalSearch()
        );
    }

    @Test
    void shouldGetAllCaseTypeIds() {
        List<String> result = controller.getAllCaseTypeReferences();

        assertAll(
            () -> verify(caseTypeRepository).findAllCaseTypeIds(),
            () -> assertThat(result.size(), is(3)),
            () -> assertThat(result, hasItem(CASE_TYPE_1)),
            () -> assertThat(result, hasItem(CASE_TYPE_2)),
            () -> assertThat(result, hasItem(CASE_TYPE_3))
        );
    }

    private CaseTypeEntity createCaseType(String reference, String jurisdictionReference) {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(reference);
        caseType.setJurisdiction(createJurisdiction(jurisdictionReference));
        caseType.setSecurityClassification(PUBLIC);
        return caseType;
    }

    private JurisdictionEntity createJurisdiction(String reference) {
        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference(reference);
        return jurisdiction;
    }
}
