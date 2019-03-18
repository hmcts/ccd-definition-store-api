package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;

import static javax.persistence.GenerationType.IDENTITY;

import uk.gov.hmcts.ccd.definition.store.repository.entity.Versionable;

public abstract class VersionableDefEntity implements Versionable {

    public abstract Integer getId();
}
