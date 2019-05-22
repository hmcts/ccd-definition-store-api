package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

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

    public List<String> buildDottedComplexFieldPossibilities(List<? extends FieldEntity> caseFieldEntities) {
        return removeElementsThoseAreNotTreeLeafs(buildAllDottedComplexFieldPossibilities(caseFieldEntities));
    }

    public List<String> buildDottedComplexFieldPossibilitiesIncludingParentComplexFields(List<? extends FieldEntity> caseFieldEntities) {
        return removeElementsThatAreCaseFields(buildAllDottedComplexFieldPossibilities(caseFieldEntities), caseFieldEntities);
    }

    private List<String> buildAllDottedComplexFieldPossibilities(List<? extends FieldEntity> caseFieldEntities) {
        List<String> allSubTypePossibilities = new ArrayList<>();
        List<? extends FieldEntity> fieldEntities = caseFieldEntities.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.<FieldEntity>toList());
        prepare(allSubTypePossibilities, "", fieldEntities);
        return allSubTypePossibilities;
    }

    private List<String> removeElementsThoseAreNotTreeLeafs(List<String> allSubTypePossibilities) {
        return allSubTypePossibilities.stream()
            .filter(e -> allSubTypePossibilities.stream().noneMatch(el -> el.startsWith(e + ".")))
            .collect(Collectors.toList());
    }

    private List<String> removeElementsThatAreCaseFields(List<String> allSubTypePossibilities, List<? extends FieldEntity> caseFieldEntities) {
        return allSubTypePossibilities.stream()
            .filter(e -> caseFieldEntities.stream().noneMatch(element -> element.getReference().equalsIgnoreCase(e)))
            .collect(Collectors.toList());
    }

    private void prepare(List<String> allSubTypePossibilities,
                                String startingString,
                                List<? extends FieldEntity> caseFieldEntities) {

        String concatenationCharacter = isBlank(startingString) ? "" : ".";
        caseFieldEntities.forEach(caseFieldEntity -> {
            allSubTypePossibilities.add(startingString + concatenationCharacter + caseFieldEntity.getReference());

            List<ComplexFieldEntity> complexFields;
            if (caseFieldEntity.getFieldType() == null) {
                complexFields = Collections.emptyList();
            } else if (isCollection(caseFieldEntity)) {
                complexFields = caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields();
            } else {
                complexFields = caseFieldEntity.getFieldType().getComplexFields();
            }

            prepare(allSubTypePossibilities,
                startingString + concatenationCharacter + caseFieldEntity.getReference(),
                complexFields.stream().map(FieldEntity.class::cast).collect(Collectors.toList()));
        });
    }

    private boolean isCollection(FieldEntity caseFieldEntity) {
        return caseFieldEntity.getFieldType().getCollectionFieldType() != null
            && caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields() != null
            && !caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields().isEmpty();
    }
}
