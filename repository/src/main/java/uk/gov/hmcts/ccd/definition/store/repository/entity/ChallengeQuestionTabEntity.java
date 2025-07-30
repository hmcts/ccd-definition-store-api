package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.io.Serializable;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

@Table(name = "challenge_question")
@Entity
public class ChallengeQuestionTabEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeLiteEntity caseType;

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

    @Column(name = "ignore_null_fields", columnDefinition = "boolean default false")
    private Boolean ignoreNullFields = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CaseTypeLiteEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseTypeLiteEntity caseType) {
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

    public Boolean isIgnoreNullFields() {
        return ignoreNullFields;
    }

    public void setIgnoreNullFields(boolean ignoreNullFields) {
        this.ignoreNullFields = ignoreNullFields;
    }
}
