package uk.gov.hmcts.ccd.definition.store.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.time.LocalDate;
import java.util.Date;

public class ImportAudit {

    @Getter
    @Setter
    @JsonProperty("date_imported")
    private String dateImported;

    @Getter
    @Setter
    @JsonProperty("who_imported")
    private String whoImported;

    @Getter
    @Setter
    @JsonProperty("case_type")
    private String caseType;

    @Getter
    @Setter
    private String filename;

    @Getter
    @Setter
    private URI uri;

    @Getter
    @Setter
    private Date order;

    public void setDateImported(LocalDate date) {
        this.dateImported = date.toString();
    }
}
