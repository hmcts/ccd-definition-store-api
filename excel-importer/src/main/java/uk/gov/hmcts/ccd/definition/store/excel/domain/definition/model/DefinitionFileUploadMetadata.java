package uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Metadata to be added for Definition File uploads to Azure Storage.
 */
public class DefinitionFileUploadMetadata {

    private String jurisdiction;
    private List<String> caseTypes;
    private String userId;

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(final String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public List<String> getCaseTypes() {
        return caseTypes;
    }

    public void setCaseTypes(final List<String> caseTypes) {
        this.caseTypes = caseTypes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public void addCaseType(final String caseType) {
        if (caseTypes == null) {
            caseTypes = new ArrayList<>();
        }
        caseTypes.add(caseType);
    }

    /**
     * Return the list of Case Types as a comma-delimited string.
     *
     * @return A string with comma-separated Case Types
     */
    public String getCaseTypesAsString() {
        if (caseTypes != null) {
            return caseTypes.stream().collect(Collectors.joining(","));
        }

        return null;
    }
}
