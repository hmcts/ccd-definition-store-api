package uk.gov.hmcts.ccd.definition.store.elastic;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public class AbstractElasticSearchSupport {

    @Autowired
    protected RestHighLevelClient elasticClient;

    @Autowired
    protected CcdElasticSearchProperties config;

    protected String indexName(CaseTypeEntity caseType) {
        String jurisdiction = caseType.getJurisdiction().getName();
        String caseTypeId = caseType.getReference();
        return String.format(config.getIndexCasesNameFormat(), jurisdiction.toLowerCase(), caseTypeId.toLowerCase());
    }
}
