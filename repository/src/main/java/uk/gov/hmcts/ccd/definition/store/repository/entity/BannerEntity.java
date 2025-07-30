package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;

@Table(name = "banner")
@Entity
public class BannerEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "banner_enabled")
    private Boolean bannerEnabled;

    @Column(name = "banner_description")
    private String bannerDescription;

    @Column(name = "banner_url_text")
    private String bannerUrlText;

    @Column(name = "banner_url")
    private String bannerUrl;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "jurisdiction_id", nullable = false)
    private JurisdictionEntity jurisdiction;

    public Integer getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
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

    public JurisdictionEntity getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(JurisdictionEntity jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public void copy(BannerEntity bannerEntity) {
        this.setBannerDescription(bannerEntity.getBannerDescription());
        this.setBannerUrl(bannerEntity.getBannerUrl());
        this.setBannerEnabled(bannerEntity.getBannerEnabled());
        this.setBannerUrlText(bannerEntity.getBannerUrlText());
    }

    @Override
    public String toString() {
        return "BannerEntity{"
            + "id=" + id
            + ", createdAt=" + createdAt
            + ", bannerEnabled='" + bannerEnabled + '\''
            + ", bannerDescription='" + bannerDescription + '\''
            + ", bannerUrlText='" + bannerUrlText + '\''
            + ", bannerUrl='" + bannerUrl + '\''
            + ", jurisdiction='" + jurisdiction + '\''
            + '}';
    }
}
