package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class CaseFieldEntityUtil {

    public static List<String> parseParentCodes(String listElementCode) {
        List<String> result = new ArrayList<>();
        String codes = listElementCode;
        while (codes.lastIndexOf('.') > 0) {
            codes = codes.substring(0, codes.lastIndexOf('.'));
            result.add(codes);
        }
        return result;
    }

    public Set<String> buildDottedComplexFieldPossibilities(Set<? extends FieldEntity> caseFieldEntities) {
        return buildAllDottedComplexFieldPossibilities(caseFieldEntities, true);
    }

    public Set<String> buildDottedComplexFieldPossibilitiesIncludingParentComplexFields(
        Set<? extends FieldEntity> caseFieldEntities) {
        return removeElementsThatAreCaseFields(buildAllDottedComplexFieldPossibilities(
            caseFieldEntities, false), caseFieldEntities);
    }

    private Set<String> buildAllDottedComplexFieldPossibilities(Set<? extends FieldEntity> caseFieldEntities,
                                                                 boolean leavesOnly) {
        Set<String> allSubTypePossibilities = new HashSet<>();
        Set<? extends FieldEntity> fieldEntities = caseFieldEntities.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.<FieldEntity>toSet());
        prepare(allSubTypePossibilities, "", fieldEntities, leavesOnly);
        return allSubTypePossibilities;
    }

    private Set<String> removeElementsThatAreCaseFields(
        Set<String> allSubTypePossibilities, Set<? extends FieldEntity> caseFieldEntities) {
        return allSubTypePossibilities.stream()
            .filter(e -> caseFieldEntities.stream().noneMatch(element -> element.getReference().equalsIgnoreCase(e)))
            .collect(Collectors.toSet());
    }

    private void prepare(Set<String> allSubTypePossibilities,
                         String startingString,
                         Set<? extends FieldEntity> caseFieldEntities, boolean leavesOnly) {

        String concatenationCharacter = isBlank(startingString) ? "" : ".";
        caseFieldEntities.forEach(caseFieldEntity -> {

            Set<ComplexFieldEntity> complexFields;
            if (caseFieldEntity.getFieldType() == null) {
                complexFields = Collections.emptySet();
            } else if (isCollection(caseFieldEntity)) {
                complexFields = caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields();
            } else {
                complexFields = caseFieldEntity.getFieldType().getComplexFields();
            }

            // If only looking for leaves, only add if this field has no children.
            if (!leavesOnly || (complexFields.isEmpty())) {
                allSubTypePossibilities.add(startingString + concatenationCharacter + caseFieldEntity.getReference());
            }

            prepare(allSubTypePossibilities,
                startingString + concatenationCharacter + caseFieldEntity.getReference(),
                complexFields.stream().map(FieldEntity.class::cast).collect(Collectors.toSet()), leavesOnly);
        });
    }

    private boolean isCollection(FieldEntity caseFieldEntity) {
        return caseFieldEntity.getFieldType().getCollectionFieldType() != null
            && caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields() != null
            && !caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields().isEmpty();
    }
}
