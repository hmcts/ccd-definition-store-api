package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Date;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class SearchCriteria {

    private String caseTypeId;
    private Date liveFrom;
    private Date liveTo;
    private String otherCaseReference;
}
