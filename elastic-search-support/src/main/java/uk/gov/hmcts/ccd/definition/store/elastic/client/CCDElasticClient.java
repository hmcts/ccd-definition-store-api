package uk.gov.hmcts.ccd.definition.store.elastic.client;

import java.io.IOException;

public interface CCDElasticClient {

    boolean createIndex(String indexName, String alias) throws IOException;

    boolean upsertMapping(String aliasName, String caseTypeMapping) throws IOException;

    boolean aliasExists(String alias) throws IOException;

    /**
     * Ensures {@code baseIndexName} points at the highest-numbered {@code baseIndexName-00000N} index.
     *
     * @return {@code true} if versioned indices exist (alias created, restored, or already correct),
     *         {@code false} if none exist and a first index must be created
     */
    boolean restoreAliasFromLatestVersionedIndex(String baseIndexName) throws IOException;

    void close();
}
