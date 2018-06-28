package uk.gov.hmcts.ccd.definition.store;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractElasticSearchSupport {

    @Autowired
    protected RestHighLevelClient elasticClient;

}
