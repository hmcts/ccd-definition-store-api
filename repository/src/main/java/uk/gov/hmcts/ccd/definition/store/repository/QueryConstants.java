package uk.gov.hmcts.ccd.definition.store.repository;

final class QueryConstants {
    private QueryConstants() {
        // Hide Utility Class Constructor : (squid:S1118)
    }

    public static final String SELECT_MAX_CASE_TYPE_VERSION_NUMBER =
        "select max(cm.version) from CaseTypeEntity as cm where cm.reference = :caseTypeReference";

    public static final String SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE =
        "select c from CaseTypeEntity c where c.reference=:caseTypeReference "
            + "and c.version in (" + SELECT_MAX_CASE_TYPE_VERSION_NUMBER + ")";
}
