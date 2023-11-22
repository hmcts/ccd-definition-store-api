package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

import java.io.Serializable;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "access_type_roles")
@Entity
public class AccessTypeRolesEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "live_from")
    private LocalDateTime liveFrom;

    @Column(name = "live_to")
    private LocalDateTime liveTo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseTypeId;

    @Column(name = "access_type_id")
    private String accessTypeId;

    @Column(name = "organisation_profile_id")
    private String organisationProfileId;

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

    @Column(name = "case_access_group_id_template")
    private String caseAccessGroupIdTemplate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(LocalDateTime liveFrom) {
        this.liveFrom = liveFrom;
    }

    public LocalDateTime getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(LocalDateTime liveTo) {
        this.liveTo = liveTo;
    }

    public CaseTypeEntity getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(CaseTypeEntity caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    public String getAccessTypeId() {
        return accessTypeId;
    }

    public void setAccessTypeId(String accessTypeId) {
        this.accessTypeId = accessTypeId;
    }

    public String getOrganisationProfileId() {
        return organisationProfileId;
    }

    public void setOrganisationProfileId(String organisationProfileId) {
        this.organisationProfileId = organisationProfileId;
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

    public String getCaseAccessGroupIdTemplate() {
        return caseAccessGroupIdTemplate;
    }

    public void setCaseAccessGroupIdTemplate(String caseAccessGroupIdTemplate) {
        this.caseAccessGroupIdTemplate = caseAccessGroupIdTemplate;
    }
}
