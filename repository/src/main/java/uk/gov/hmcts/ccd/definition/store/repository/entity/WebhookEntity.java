package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Table(name = "webhook")
@Entity
public class WebhookEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "webhook_id_seq")
    private Integer id;

    @Column(name = "url")
    private String url;

    @Column(
        name = "timeouts",
        columnDefinition = "integer[]"
    )
    private Integer[] timeouts = new Integer[0];

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTimeouts(@NotNull final List<Integer> ints) {
        this.timeouts = ints.toArray(new Integer[0]);
    }

    public List<Integer> getTimeouts() {
        return Lists.newArrayList(timeouts);
    }

}
