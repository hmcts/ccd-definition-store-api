package uk.gov.hmcts.ccd.definition.store.repository;

final class QueryConstants {

    public static final String SELECT_MAX_CASE_TYPE_VERSION_NUMBER =
        "select max(cm.version) from CaseTypeEntity as cm where cm.reference = :caseTypeReference";

    public static final String SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE =
        "select c from CaseTypeEntity c where c.reference=:caseTypeReference " +
            "and c.version in (" + SELECT_MAX_CASE_TYPE_VERSION_NUMBER + ")";

    public static final String SELECT_LATEST_CASE_TYPES_REFERENCES =
        "select c.reference from CaseTypeEntity c where c.version in (select max(cm.version) from CaseTypeEntity as cm where cm.reference = c.reference)";


}
