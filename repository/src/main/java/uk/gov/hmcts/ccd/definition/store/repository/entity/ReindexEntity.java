package uk.gov.hmcts.ccd.definition.store.repository.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "reindex")
@Entity
@Getter
@Setter
public class ReindexEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

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
