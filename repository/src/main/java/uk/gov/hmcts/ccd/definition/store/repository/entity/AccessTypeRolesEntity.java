package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;

@Table(name = "access_type_roles")
@Entity
public class AccessTypeRolesEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = SEQUENCE, generator = "access_type_roles_id_seq")
    private Integer id;

    @Column(name = "live_from")
    private Date liveFrom;

    @Column(name = "live_to")
    private Date liveTo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @Column(name = "access_type_id", nullable = false)
    private String accessTypeID;

    @Column(name = "organisation_profile_id", nullable = false)
    private String organisationProfileID;

    @Column(name = "access_mandatory")
    private Boolean accessMandatory;

    @Column(name = "access_default")
    private Boolean accessDefault;
    @Column(name = "display")
    private Boolean display;

    @Column(name = "description")
    private String description;

    @Column(name = "hint")
    private String hint;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "organisational_role_name")
    private String organisationalRoleName;

    @Column(name = "group_role_name")
    private String groupRoleName;

    @Column(name = "organisation_policy_field")
    private String organisationPolicyField;

    @Column(name = "group_access_enabled")
    private Boolean groupAccessEnabled;

    @Column(name = "case_group_id_template")
    private String caseGroupIdTemplate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(Date liveFrom) {
        this.liveFrom = liveFrom;
    }

    public Date getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(Date liveTo) {
        this.liveTo = liveTo;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public String getAccessTypeID() {
        return accessTypeID;
    }

    public void setAccessTypeID(String accessTypeID) {
        this.accessTypeID = accessTypeID;
    }

    public String getOrganisationProfileID() {
        return organisationProfileID;
    }

    public void setOrganisationProfileID(String organisationProfileID) {
        this.organisationProfileID = organisationProfileID;
    }

    public Boolean getDisplay() {
        return display;
    }

    public void setDisplay(Boolean display) {
        this.display = display;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getOrganisationalRoleName() {
        return organisationalRoleName;
    }

    public void setOrganisationalRoleName(String organisationalRoleName) {
        this.organisationalRoleName = organisationalRoleName;
    }

    public String getGroupRoleName() {
        return groupRoleName;
    }

    public void setGroupRoleName(String groupRoleName) {
        this.groupRoleName = groupRoleName;
    }

    public String getOrganisationPolicyField() {
        return organisationPolicyField;
    }

    public void setOrganisationPolicyField(String organisationPolicyField) {
        this.organisationPolicyField = organisationPolicyField;
    }

    public Boolean getGroupAccessEnabled() {
        return groupAccessEnabled;
    }

    public void setGroupAccessEnabled(Boolean groupAccessEnabled) {
        this.groupAccessEnabled = groupAccessEnabled;
    }

    public String getCaseGroupIdTemplate() {
        return caseGroupIdTemplate;
    }

    public void setCaseGroupIdTemplate(String caseGroupIdTemplate) {
        this.caseGroupIdTemplate = caseGroupIdTemplate;
    }

    public Boolean getAccessMandatory() {
        return accessMandatory;
    }

    public void setAccessMandatory(Boolean accessMandatory) {
        this.accessMandatory = accessMandatory;
    }

    public Boolean getAccessDefault() {
        return accessDefault;
    }

    public void setAccessDefault(Boolean accessDefault) {
        this.accessDefault = accessDefault;
    }

}
