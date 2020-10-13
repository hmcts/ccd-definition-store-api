package uk.gov.hmcts.ccd.definition.store.elastic.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticDefinitionImportListener;
import uk.gov.hmcts.ccd.definition.store.elastic.model.IndicesCreationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
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

    private List<CaseTypeEntity> caseTypes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        caseTypes = new ArrayList<>();
        caseTypes.add(createCaseType("CT1", "J1"));
        caseTypes.add(createCaseType("CT2", "J1"));
        caseTypes.add(createCaseType("CT3", "J2"));
        when(caseTypeRepository.findAllLatestVersions()).thenReturn(caseTypes);
    }

    @Test
    void shouldTriggerElasticsearchIndicesCreation() {
        IndicesCreationResult result = controller.createElasticsearchIndices();

        assertAll(
            () -> verify(elasticDefinitionImportListener).initialiseElasticSearch(caseTypes),
            () -> assertThat(result.getTotal(), is(3)),
            () -> assertThat(result.getCaseTypesByJurisdiction().keySet().size(), is(2)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J1").size(), is(2)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J2").size(), is(1)),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J1"), hasItem("CT1")),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J1"), hasItem("CT2")),
            () -> assertThat(result.getCaseTypesByJurisdiction().get("J2"), hasItem("CT3"))
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
