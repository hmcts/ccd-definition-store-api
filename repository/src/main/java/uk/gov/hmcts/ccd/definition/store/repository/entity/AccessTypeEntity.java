package uk.gov.hmcts.ccd.definition.store.repository.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "access_type")
@Entity
@Getter
@Setter
public class AccessTypeEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
    @JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
    private CaseTypeEntity caseTypeId;

    @Column(name = "access_type_id", nullable = false)
    private String accessTypeId;

    @Column(name = "organisation_profile_id", nullable = false)
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

}
