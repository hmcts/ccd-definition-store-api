package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static javax.persistence.FetchType.LAZY;

@Table(name = "role_to_access_profiles")
@Entity
public class RoleToAccessProfilesEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @Column(name = "live_from")
    private Date liveFrom;

    @Column(name = "live_to")
    private Date liveTo;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Column(name = "authorisation")
    private String authorisation;

    @Column(name = "access_profiles", nullable = false)
    private String accessProfiles;

    @Column(name = "read_only")
    private Boolean readOnly;

    @Column(name = "disabled")
    private Boolean disabled;

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

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getAuthorisation() {
        return authorisation;
    }

    public void setAuthorisation(String authorisation) {
        this.authorisation = authorisation;
    }

    public String getAccessProfiles() {
        return accessProfiles;
    }

    public void setAccessProfiles(String accessProfiles) {
        this.accessProfiles = accessProfiles;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
}
