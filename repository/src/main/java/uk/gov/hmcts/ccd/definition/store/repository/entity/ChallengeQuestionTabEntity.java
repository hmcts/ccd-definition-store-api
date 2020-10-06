package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.io.Serializable;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.FetchType;

@Table(name = "challenge_question")
@Entity
public class ChallengeQuestionTabEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @Column(name = "display_order")
    private Integer order;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_field_type", nullable = false)
    private FieldTypeEntity answerFieldType;

    @Column(name = "display_context_parameter")
    private String displayContextParameter;

    @Column(name = "challenge_question_id", nullable = false)
    private String challengeQuestionId;

    @Column(name = "answer_field", nullable = false)
    private String answerField;

    @Column(name = "question_id")
    private String  questionId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public FieldTypeEntity getAnswerFieldType() {
        return answerFieldType;
    }

    public void setAnswerFieldType(FieldTypeEntity answerFieldType) {
        this.answerFieldType = answerFieldType;
    }

    public String getDisplayContextParameter() {
        return displayContextParameter;
    }

    public void setDisplayContextParameter(String displayContextParameter) {
        this.displayContextParameter = displayContextParameter;
    }

    public String getChallengeQuestionId() {
        return challengeQuestionId;
    }

    public void setChallengeQuestionId(String challengeQuestionId) {
        this.challengeQuestionId = challengeQuestionId;
    }

    public String getAnswerField() {
        return answerField;
    }

    public void setAnswerField(String answerField) {
        this.answerField = answerField;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
}
