package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.repository.LayoutSheetType;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@MappedSuperclass
public abstract class GenericLayoutEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "live_from")
    private LocalDate liveFrom;

    @Column(name = "live_to")
    private LocalDate liveTo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "case_field_id", nullable = false)
    private CaseFieldEntity caseField;

    @Column(name = "case_field_element_path")
    private String caseFieldElementPath;

    @Column(name = "label")
    private String label;

    @Column(name = "display_order")
    private Integer order;

    @ManyToOne(cascade = ALL)
    @JoinColumn(name = "role_id", nullable = false)
    private UserRoleEntity userRole;

    @Column(name = "display_context_parameter")
    private String displayContextParameter;

    public abstract String getSheetName();

    public abstract LayoutSheetType getLayoutSheetType();

    public Integer getId() {
        return id;
    }

    public LocalDate getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(LocalDate liveFrom) {
        this.liveFrom = liveFrom;
    }

    public LocalDate getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(LocalDate liveTo) {
        this.liveTo = liveTo;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(final CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public CaseFieldEntity getCaseField() {
        return caseField;
    }

    public void setCaseField(final CaseFieldEntity caseField) {
        this.caseField = caseField;
    }

    public String getCaseFieldElementPath() {
        return caseFieldElementPath;
    }

    public void setCaseFieldElementPath(final String caseFieldElementPath) {
        this.caseFieldElementPath = caseFieldElementPath;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    public UserRoleEntity getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRoleEntity userRole) {
        this.userRole = userRole;
    }

    public Optional<String> showCondition() {
        return Optional.empty();
    }

    public String getDisplayContextParameter() {
        return displayContextParameter;
    }

    public void setDisplayContextParameter(String displayContextParameter) {
        this.displayContextParameter = displayContextParameter;
    }

    /**
     * Determine whether the field that the layout entity represents is searchable. All fields (top level and nested)
     * have a searchable property. In the case of nested fields, a field is considered searchable if its parent (and
     * its parent's parent etc.) are all searchable too.
     * @return Whether the entity represents a searchable field.
     */
    @Transient
    public boolean isSearchable() {
        return getCaseField().isNestedFieldSearchable(getCaseFieldElementPath());
    }

    @Transient
    public String buildFieldPath() {
        if (StringUtils.isNotBlank(getCaseFieldElementPath())) {
            return getCaseField().getReference() + '.' + getCaseFieldElementPath();
        }
        return getCaseField().getReference();
    }
}
