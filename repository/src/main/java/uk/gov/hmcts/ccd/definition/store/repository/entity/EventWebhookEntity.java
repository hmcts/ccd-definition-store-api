package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.model.WebhookType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * This is a joining type for modelling Event Webhooks.
 * An event can have multiple webhooks which fire at different stages during the
 * event (Start, PreSubmit and PostSubmit at time of writing).
 * See {@link EventEntity#webhooks}
 */
@TypeDef(
    name = "webhook_type_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @Parameter(name = "type", value = "uk.gov.hmcts.ccd.definition.store.repository.model.WebhookType")
)
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
    @Type(type = "webhook_type_enum")
    private WebhookType type;

    private EventWebhookEntity() {
    }

    public EventWebhookEntity(EventEntity event, WebhookEntity webhook, WebhookType type) {
        this.event = event;
        this.webhook = webhook;
        this.type = type;
    }

    public WebhookEntity getWebhook() {
        return webhook;
    }
}

