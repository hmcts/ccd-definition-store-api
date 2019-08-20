package uk.gov.hmcts.ccd.definition.store.domain;

public interface AmPersistenceSwitch {

    AmPersistenceWriteDestination getWriteDataSourceFor(String caseType);

    AmPersistenceReadSource getReadDataSourceFor(String caseType);

}
