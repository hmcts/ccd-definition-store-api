package uk.gov.hmcts.ccd.definition.store.elastic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;

@Data
public class IndicesCreationResult {

    @JsonProperty("case_types")
    @ApiModelProperty("Case types for which indices have been created for, grouped by jurisdiction")
    private Map<String, Set<String>> groupedCaseTypes;

    private int total;

    public IndicesCreationResult(List<CaseTypeEntity> caseTypes) {
        this.groupedCaseTypes = getGroupedCaseTypes(caseTypes);
        this.total = caseTypes.size();
    }

    // Group the case types by jurisdiction, with both keys and values sorted
    private Map<String, Set<String>> getGroupedCaseTypes(List<CaseTypeEntity> caseTypes) {
        return caseTypes.stream()
            .collect(groupingBy(ct -> ct.getJurisdiction().getReference(),
                TreeMap::new,
                mapping(CaseTypeEntity::getReference, toCollection(TreeSet::new))));
    }
}
