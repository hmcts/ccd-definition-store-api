package uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Metadata to be added for Definition File uploads to Azure Storage.
 */
public class DefinitionFileUploadMetadata {

    @Setter private String jurisdiction;
    private List<String> caseTypes;
    @Setter private String userId;
    @Setter @Getter private String taskId;

    public String getJurisdiction() {
        return jurisdiction;
    }

    public List<String> getCaseTypes() {
        return caseTypes;
    }

    public String getUserId() {
        return userId;
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
