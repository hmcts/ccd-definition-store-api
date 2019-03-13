package uk.gov.hmcts.ccd.definition.store.repository.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "")
public class Version implements Serializable {

    private static final long serialVersionUID = 1886051203323303930L;

    private Integer number = null;
    private Date liveFrom = null;
    private Date liveUntil = null;

    /**
     * Sequantial version number
     **/
    @ApiModelProperty(required = true, value = "Sequantial version number")
    @JsonProperty("number")
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     * Date and time from when this version is valid from
     **/
    @ApiModelProperty(required = true, value = "Date and time from when this version is valid from")
    @JsonProperty("live_from")
    public Date getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(Date liveFrom) {
        this.liveFrom = liveFrom;
    }

    /**
     * Date and time this version is to be retired
     **/
    @ApiModelProperty(value = "Date and time this version is to be retired")
    @JsonProperty("live_until")
    public Date getLiveUntil() {
        return liveUntil;
    }

    public void setLiveUntil(Date liveUntil) {
        this.liveUntil = liveUntil;
    }
}
