package uk.gov.hmcts.ccd.definition.store.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;

@Table(name = "jurisdiction_ui_config")
@Entity
public class JurisdictionUiConfigEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "shuttered")
    private Boolean shuttered;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "jurisdiction_id", nullable = false)
    private JurisdictionEntity jurisdiction;

    public Integer getId() {
        return id;
    }

    public Boolean getShuttered() {
        return shuttered;
    }

    public void setShuttered(Boolean shuttered) {
        this.shuttered = shuttered;
    }

    public JurisdictionEntity getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(JurisdictionEntity jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public void copy(JurisdictionUiConfigEntity entity) {
        this.setShuttered(entity.getShuttered());
    }

    @Override
    public String toString() {
        return "JurisdictionUiConfigEntity{"
            + "id=" + id
            + ", shuttered=" + shuttered
            + ", jurisdiction='" + jurisdiction + '\''
            + '}';
    }
}
