package uk.gov.hmcts.ccd.definition.store.elastic.listener;

public interface ReindexListener {

    void onSuccess();

    void onFailure(Exception ex);
}
