package uk.gov.hmcts.ccd.definition.store.repository;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Version;
import java.util.Date;

@MappedSuperclass
@Getter
@Setter
public class ModelObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long reference;

    @Version
    @Temporal(TemporalType.TIMESTAMP)
    private Date version;
}
