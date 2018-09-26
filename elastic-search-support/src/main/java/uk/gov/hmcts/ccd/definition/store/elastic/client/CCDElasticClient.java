package uk.gov.hmcts.ccd.definition.store.elastic.client;

import java.io.IOException;

public interface CCDElasticClient {

    boolean indexExists(String indexName) throws IOException;

    boolean createIndex(String indexName, String alias) throws IOException;

    boolean upsertMapping(String indexName, String caseTypeMapping) throws IOException;
}
