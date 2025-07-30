package uk.gov.hmcts.ccd.definition.store.repository.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serial;
import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Table(name = "access_type")
@Entity
@Getter
@Setter
public class AccessTypeEntity {

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
    private CaseTypeLiteEntity caseType;

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

    public record UniqueIdentifier(String caseTypeReference, String jurisdictionReference, String accessTypeId,
                                   String organisationProfileId) {
    }

}
