package uk.gov.hmcts.ccd.definition.store.repository.am;

public interface AmPersistenceSwitch {

    AmPersistenceWriteDestination getWriteDataSourceFor(String caseType);

    AmPersistenceReadSource getReadDataSourceFor(String caseType);

}
