package uk.gov.hmcts.ccd.definition.store.repository.entity;

import com.google.common.collect.Lists;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Table(name = "webhook")
@Entity
@TypeDefs({
    @TypeDef(
        name = "int-array",
        typeClass = IntArrayType.class
    )
})
public class WebhookEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "webhook_id_seq")
    private Integer id;

    @Column(name = "url")
    private String url;

    @Type(type = "int-array")
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
