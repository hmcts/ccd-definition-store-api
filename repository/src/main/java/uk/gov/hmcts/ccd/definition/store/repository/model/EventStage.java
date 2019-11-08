package uk.gov.hmcts.ccd.definition.store.repository.model;

/**
 * The different stages at which webhooks can fire for an
 * {@link uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity}
 */
public enum EventStage {
    START,
    PRE_SUBMIT,
    POST_SUBMIT
}
