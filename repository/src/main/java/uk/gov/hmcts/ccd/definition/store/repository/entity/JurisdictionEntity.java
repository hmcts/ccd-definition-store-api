package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "jurisdiction")
@Entity
public class JurisdictionEntity implements Serializable, Versionable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "live_from")
    private Date liveFrom;

    @Column(name = "live_to")
    private Date liveTo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "banner_enabled")
    private Boolean bannerEnabled;

    @Column(name = "banner_description")
    private String bannerDescription;

    @Column(name = "banner_url_text")
    private String bannerUrlText;

    @Column(name = "banner_url")
    private String bannerUrl;

    public Integer getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public Date getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(final Date liveFrom) {
        this.liveFrom = liveFrom;
    }

    public Date getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(final Date liveTo) {
        this.liveTo = liveTo;
    }

    public Boolean getBannerEnabled() {
        return bannerEnabled;
    }

    public void setBannerEnabled(Boolean bannerEnabled) {
        this.bannerEnabled = bannerEnabled;
    }

    public String getBannerDescription() {
        return bannerDescription;
    }

    public void setBannerDescription(String bannerDescription) {
        this.bannerDescription = bannerDescription;
    }

    public String getBannerUrlText() {
        return bannerUrlText;
    }

    public void setBannerUrlText(String bannerUrlText) {
        this.bannerUrlText = bannerUrlText;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    @Override
    public String toString() {
        return "JurisdictionEntity{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", reference='" + reference + '\'' +
                ", version=" + version +
                ", liveFrom=" + liveFrom +
                ", liveTo=" + liveTo +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", bannerEnabled='" + bannerEnabled + '\'' +
                ", bannerDescription='" + bannerDescription + '\'' +
                ", bannerUrlText='" + bannerUrlText + '\'' +
                ", bannerUrl='" + bannerUrl + '\'' +
                '}';
    }
}
