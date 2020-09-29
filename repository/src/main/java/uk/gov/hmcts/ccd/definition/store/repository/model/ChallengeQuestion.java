package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class ChallengeQuestion {

    private String caseTypeId;
    private Integer order;
    private String questionText;
    private FieldType answerFieldType;
    private String displayContextParameter;
    private String challengeQuestionId;
    private String answerField;
    private String questionId;
}
