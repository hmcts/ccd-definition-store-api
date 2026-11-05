package uk.gov.hmcts.ccd.definition.store.repository;

public final class SnapshotFormat {

    // Increment whenever mapping or serialized model changes invalidate previously cached responses.
    public static final int REVISION = 1;

    private SnapshotFormat() {
    }
}
