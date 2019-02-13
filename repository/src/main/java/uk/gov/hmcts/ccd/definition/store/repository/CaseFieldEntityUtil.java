package uk.gov.hmcts.ccd.definition.store.repository;

import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class CaseFieldEntityUtil {

    private CaseFieldEntityUtil() { }

    public static List<String> buildDottedComplexFieldPossibilities(List<? extends FieldEntity> caseFieldEntities) {
        List<String> allSubTypePossibilities = new ArrayList<>();

        //.<FieldEntity> added to fix a weird build error with some versions of the jdk
        prepare(allSubTypePossibilities, "",
            caseFieldEntities.stream().filter(Objects::nonNull).collect(Collectors.<FieldEntity>toList()));
        return removeElementsThatAreNotTreeLeafs(allSubTypePossibilities);
    }

    private static List<String> removeElementsThatAreNotTreeLeafs(List<String> allSubTypePossibilities) {
        return allSubTypePossibilities.stream()
            .filter(e -> allSubTypePossibilities.stream().noneMatch(el -> el.startsWith(e + ".")))
            .collect(Collectors.toList());
    }

    private static void prepare(List<String> allSubTypePossibilities,
                         String startingString,
                         List<FieldEntity> caseFieldEntities) {

        String concatenationCharacter = isBlank(startingString) ? "" : ".";
        caseFieldEntities.forEach(caseFieldEntity -> {
            allSubTypePossibilities.add(startingString + concatenationCharacter + caseFieldEntity.getReference());
            List<ComplexFieldEntity> complexFields = caseFieldEntity
                .getFieldType()
                .getComplexFields();
            prepare(allSubTypePossibilities,
                startingString + concatenationCharacter + caseFieldEntity.getReference(),
                complexFields.stream().map(FieldEntity.class::cast).collect(Collectors.toList()));
        });
    }

}
