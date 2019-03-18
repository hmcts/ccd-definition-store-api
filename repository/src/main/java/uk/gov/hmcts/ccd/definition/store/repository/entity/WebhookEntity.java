package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.EAGER;

@Table(name = "webhook")
@Entity
public class WebhookEntity extends DefEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "url")
    private String url;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(name = "webhook_timeout", joinColumns = @JoinColumn(name = "webhook_id"))
    @OrderColumn(name = "index")
    @Column(name = "timeout")
    private final List<Integer> timeouts = new ArrayList<>();

    @Override
    public Integer getId() {
        return this.id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void addTimeout(@NotNull final Integer timeout) {
        timeouts.add(timeout);
    }

    public List<Integer> getTimeouts() {
        return timeouts;
    }
}
