package uk.gov.hmcts.ccd.definition.store.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
@Table(name = "case_type_snapshot")
public class CaseTypeSnapshotEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "case_type_reference", nullable = false, length = 70, unique = true)
    private String caseTypeReference;

    @Column(name = "version_id", nullable = false)
    private Integer versionId;

    @Column(name = "precomputed_response", nullable = false, columnDefinition = "jsonb")
    private String precomputedResponse;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_modified", nullable = false, insertable = false)
    @UpdateTimestamp
    private LocalDateTime lastModified;

}
