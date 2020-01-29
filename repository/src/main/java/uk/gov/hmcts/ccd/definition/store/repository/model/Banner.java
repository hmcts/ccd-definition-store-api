package uk.gov.hmcts.ccd.definition.store.repository.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "")
public class Banner {

    private String id = null;
    private Boolean bannerEnabled = null;
    private String bannerDescription = null;
    private String bannerUrl = null;
    private String bannerUrlText = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getBannerUrlText() {
        return bannerUrlText;
    }

    public void setBannerUrlText(String bannerUrlText) {
        this.bannerUrlText = bannerUrlText;
    }
}
