package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

@ApiModel(description = "")
public class BannersResult {

    private List<Banner> banners;

    public BannersResult(List<Banner> banners) {
        this.banners = banners;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("banners")
    public List<Banner> getBanners() {
        return banners;
    }

    public void setBanners(List<Banner> banners) {
        this.banners = banners;
    }
}
