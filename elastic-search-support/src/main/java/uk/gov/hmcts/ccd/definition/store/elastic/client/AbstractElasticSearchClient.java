package uk.gov.hmcts.ccd.definition.store.elastic.client;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public class AbstractElasticSearchClient {

    @Autowired
    protected CcdElasticSearchProperties config;
}
