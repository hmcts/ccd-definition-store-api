package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "event_acl")
@Entity
public class EventLiteACLEntity extends Authorisation implements Serializable {

}
