package uk.gov.hmcts.ccd.definition.store.elastic.listener;

public interface ReindexListener {

    void onSuccess(String reindexResponse);

    void onFailure(Exception ex);
}
