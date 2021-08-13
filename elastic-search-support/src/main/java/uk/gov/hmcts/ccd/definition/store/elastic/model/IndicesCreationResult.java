package uk.gov.hmcts.ccd.definition.store.elastic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;
import static uk.gov.hmcts.ccd.definition.store.elastic.ElasticDefinitionImportListener.GLOBAL_SEARCH;

@Data
public class IndicesCreationResult {

    @JsonProperty("case_types")
    @ApiModelProperty(
        value = "Case types for which indices have been created for, grouped by jurisdiction",
        example = "{\n"
            + "    \"AUTOTEST1\": [\n"
            + "        \"AAT\",\n"
            + "        \"MAPPER\",\n"
            + "        \"MultiplePages\"\n"
            + "    ],\n"
            + "    \"PUBLICLAW\": [\n"
            + "        \"CARE_SUPERVISION_EPO\",\n"
            + "        \"Shared_Storage_DRAFTType\"\n"
            + "    ]\n"
            + "}"
    )
    private Map<String, Set<String>> caseTypesByJurisdiction;

    @ApiModelProperty(value = "Number of case types", example = "5")
    private int total;

    public IndicesCreationResult(List<CaseTypeEntity> caseTypes) {
        this.caseTypesByJurisdiction = getCaseTypesGroupedByJurisdiction(caseTypes);
        this.total = caseTypes.size();
    }

    public IndicesCreationResult() {
        Map<String, Set<String>> result = new HashMap<>();
        result.put(GLOBAL_SEARCH, Set.of(GLOBAL_SEARCH));
        this.caseTypesByJurisdiction = result;
        this.total = 1;
    }

    private Map<String, Set<String>> getCaseTypesGroupedByJurisdiction(List<CaseTypeEntity> caseTypes) {
        return caseTypes.stream()
            .collect(groupingBy(ct -> ct.getJurisdiction().getReference(),
                TreeMap::new,
                mapping(CaseTypeEntity::getReference, toCollection(TreeSet::new))));
    }
}
