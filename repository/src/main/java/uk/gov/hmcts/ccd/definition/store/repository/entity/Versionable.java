package uk.gov.hmcts.ccd.definition.store.repository.entity;

public interface Versionable extends DefEntity, Referencable {

    Integer getVersion();

    void setVersion(Integer version);
}
