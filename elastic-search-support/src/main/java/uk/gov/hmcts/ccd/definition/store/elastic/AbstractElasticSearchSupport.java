package uk.gov.hmcts.ccd.definition.store.elastic;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class AbstractElasticSearchSupport {

    @Autowired
    protected RestHighLevelClient elasticClient;

}
