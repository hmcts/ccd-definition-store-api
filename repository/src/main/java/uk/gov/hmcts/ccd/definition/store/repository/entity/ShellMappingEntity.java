package uk.gov.hmcts.ccd.definition.store.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Table(name = "shell_mapping")
@Entity
@Getter
@Setter
public class ShellMappingEntity {

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
    @JoinColumn(name = "shell_case_type_id", nullable = false)
    private CaseTypeLiteEntity shellCaseTypeId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "shell_case_field_name", nullable = false)
    private CaseFieldEntity shellCaseFieldName;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "originating_case_type_id", nullable = false)
    private CaseTypeLiteEntity originatingCaseTypeId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "originating_case_field_name", nullable = false)
    private CaseFieldEntity originatingCaseFieldName;
}
