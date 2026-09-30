package uk.gov.hmcts.ccd.definition.store.repository.entity;

// RUNNING is reserved for async/v2 imports.
// Phase 1 synchronous imports transition directly
// from PENDING -> COMPLETED/FAILED.
public enum ImportJobStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    EXPIRED
}
