package uk.gov.hmcts.ccd.definition.store.repository.model;

/**
 * The different webhooks which can occur for an EventEntity.
 * {@link uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity}
 */
public enum WebhookType {
    START,
    PRE_SUBMIT,
    POST_SUBMIT
}
