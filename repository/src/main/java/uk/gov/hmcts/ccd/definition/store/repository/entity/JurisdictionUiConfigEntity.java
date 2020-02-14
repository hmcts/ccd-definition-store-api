package uk.gov.hmcts.ccd.definition.store.repository.entity;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
        return "JurisdictionUiConfigEntity{" +
                "id=" + id +
                ", shuttered=" + shuttered +
                ", jurisdiction='" + jurisdiction + '\'' +
                '}';
    }
}
