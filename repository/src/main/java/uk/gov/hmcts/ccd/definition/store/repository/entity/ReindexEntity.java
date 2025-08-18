package uk.gov.hmcts.ccd.definition.store.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Table(name = "reindex")
@Entity
@Getter
@Setter
public class ReindexEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "reindex")
    private Boolean reindex;

    @Column(name = "delete_old_index")
    private Boolean deleteOldIndex;

    @Column(name = "case_type")
    private String caseType;

    @Column(name = "jurisdiction")
    private String jurisdiction;

    @Column(name = "index_name")
    private String indexName;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status")
    private String status;

    @Column(name = "message")
    private String message;
}
