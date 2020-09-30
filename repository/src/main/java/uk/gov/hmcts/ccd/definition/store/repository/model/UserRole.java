package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@ApiModel
public class UserRole {

    private static final String INVALID_ISO_DATE_FROMAT = "Invalid ISO 8601 format for date";
    private static final String REGEX_ISO_DATE = "^\\d\\d\\d\\d-\\d\\d-\\d\\d$";

    private Integer id;

    private String createdAt;

    @Pattern(regexp = REGEX_ISO_DATE, message = INVALID_ISO_DATE_FROMAT)
    private String liveFrom;

    @Pattern(regexp = REGEX_ISO_DATE, message = INVALID_ISO_DATE_FROMAT)
    private String liveTo;

    @NotNull
    private String role;

    @NotNull
    private SecurityClassification securityClassification;

    @ApiModelProperty(value = "")
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
    }

    public void setLiveFrom(final String liveFrom) {
        this.liveFrom = liveFrom;
    }

    public void setLiveTo(final String liveTo) {
        this.liveTo = liveTo;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("role")
    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("security_classification")
    public SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    public void setSecurityClassification(final SecurityClassification securityClassification) {
        this.securityClassification = securityClassification;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("live_from")
    public String getLiveFrom() {
        return liveFrom;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("live_to")
    public String getLiveTo() {
        return liveTo;
    }

}
