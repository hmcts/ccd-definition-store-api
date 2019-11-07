package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.model.EventState;

import org.hibernate.annotations.Parameter;
import javax.persistence.*;

/**
 * This is a joining type for modelling Event Webhooks.
 * An event can have multiple webhooks which fire at different states during the
 * event (Start, PreSubmit and PostSubmit at time of writing).
 * See {@link EventEntity#webhooks}
 */
@TypeDef(
    name = "event_state_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @Parameter(name = "type", value = "uk.gov.hmcts.ccd.definition.store.repository.model.EventState")
)
@Entity(name = "EventWebhook")
@Table(name = "event_webhook")
public class EventWebhook {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "webhook_id")
    private WebhookEntity webhook;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private EventEntity event;

    @Column(name = "state")
    @Type(type = "event_state_enum")
    private EventState state;

    private EventWebhook() {
    }

    public EventWebhook(EventEntity event, WebhookEntity webhook,  EventState state) {
        this.event = event;
        this.webhook = webhook;
        this.state = state;
    }

    public WebhookEntity getWebhook() {
        return webhook;
    }

    public EventState getState() {
        return state;
    }
}

