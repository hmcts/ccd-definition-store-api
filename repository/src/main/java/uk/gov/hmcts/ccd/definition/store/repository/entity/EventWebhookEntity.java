package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.model.WebhookType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * This is a joining type for modelling Event Webhooks.
 * An event can have multiple webhooks which fire at different stages during the
 * event (Start, PreSubmit and PostSubmit at time of writing).
 * See {@link EventEntity#webhooks}
 */
@Entity(name = "EventWebhookEntity")
@Table(name = "event_webhook")
public class EventWebhookEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_webhook_id_seq")
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "webhook_id")
    private WebhookEntity webhook;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private EventEntity event;

    @Column(name = "webhook_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private WebhookType type;

    public EventWebhookEntity(EventEntity event, WebhookEntity webhook, WebhookType type) {
        this.event = event;
        this.webhook = webhook;
        this.type = type;
    }

    public WebhookEntity getWebhook() {
        return webhook;
    }
}

