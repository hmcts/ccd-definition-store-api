package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "case_type_acl")
@Entity
public class CaseTypeLiteACLEntity extends Authorisation implements Serializable {

}
