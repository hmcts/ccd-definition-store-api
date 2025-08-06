package uk.gov.hmcts.ccd.definition.store.repository;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
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
