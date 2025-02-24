package uk.gov.hmcts.ccd.definition.store.elastic.client;

import java.io.IOException;

public interface CCDElasticClient {

    boolean createIndex(String indexName, String alias) throws IOException;

    boolean upsertMapping(String aliasName, String caseTypeMapping) throws IOException;

    boolean indexAndMapping(String indexName, String aliasName, String caseTypeMapping) throws IOException;

    boolean updateAlias(String alias, String oldIndex, String newIndex) throws IOException;

    boolean aliasExists(String alias) throws IOException;

    void close();
}
