package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Date;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class SearchParty {

    private String caseTypeId;
    private String searchPartyDob;
    private String searchPartyDod;
    private String searchPartyPostCode;
    private String searchPartyAddressLine1;
    private String searchPartyEmailAddress;
    private Date liveFrom;
    private Date liveTo;
    private String searchPartyName;
    private String searchPartyCollectionFieldName;

}
