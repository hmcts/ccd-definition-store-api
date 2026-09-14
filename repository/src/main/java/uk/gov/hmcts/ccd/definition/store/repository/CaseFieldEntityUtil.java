package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    public boolean isDottedComplexFieldPossibility(String path,
                                                    Collection<? extends FieldEntity> caseFieldEntities) {
        if (path == null) {
            return false;
        }

        Collection<? extends FieldEntity> fields = caseFieldEntities;
        FieldEntity matchedField = null;
        for (String reference : path.split("\\.", -1)) {
            matchedField = null;
            for (FieldEntity field : fields) {
                if (field != null && field.getReference().equals(reference)) {
                    matchedField = field;
                    break;
                }
            }
            if (matchedField == null) {
                return false;
            }
            fields = getComplexFields(matchedField);
        }
        return matchedField != null && fields.isEmpty();
    }

    public Set<String> buildDottedComplexFieldPossibilitiesIncludingParentComplexFields(
        Set<? extends FieldEntity> caseFieldEntities) {
        return removeElementsThatAreCaseFields(buildAllDottedComplexFieldPossibilities(
            caseFieldEntities, false), caseFieldEntities);
    }

    private Set<String> buildAllDottedComplexFieldPossibilities(Set<? extends FieldEntity> caseFieldEntities,
                                                                 boolean leavesOnly) {
        Set<String> allSubTypePossibilities = new HashSet<>();
        prepare(allSubTypePossibilities, "", caseFieldEntities, leavesOnly);
        return allSubTypePossibilities;
    }

    private Set<String> removeElementsThatAreCaseFields(
        Set<String> allSubTypePossibilities, Set<? extends FieldEntity> caseFieldEntities) {
        Set<String> caseFieldReferences = new HashSet<>();
        for (FieldEntity caseFieldEntity : caseFieldEntities) {
            if (caseFieldEntity != null) {
                caseFieldReferences.add(caseFieldEntity.getReference().toLowerCase(Locale.ROOT));
            }
        }
        allSubTypePossibilities.removeIf(
            path -> caseFieldReferences.contains(path.toLowerCase(Locale.ROOT))
        );
        return allSubTypePossibilities;
    }

    private void prepare(Set<String> allSubTypePossibilities,
                         String startingString,
                         Collection<? extends FieldEntity> caseFieldEntities,
                         boolean leavesOnly) {
        for (FieldEntity caseFieldEntity : caseFieldEntities) {
            if (caseFieldEntity == null) {
                continue;
            }

            Set<ComplexFieldEntity> complexFields = getComplexFields(caseFieldEntity);
            String path = startingString.isEmpty()
                ? caseFieldEntity.getReference()
                : startingString + "." + caseFieldEntity.getReference();
            if (!leavesOnly || complexFields.isEmpty()) {
                allSubTypePossibilities.add(path);
            }

            prepare(allSubTypePossibilities, path, complexFields, leavesOnly);
        }
    }

    private Set<ComplexFieldEntity> getComplexFields(FieldEntity caseFieldEntity) {
        if (caseFieldEntity.getFieldType() == null) {
            return Set.of();
        }
        if (isCollection(caseFieldEntity)) {
            return caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields();
        }
        return caseFieldEntity.getFieldType().getComplexFields();
    }

    private boolean isCollection(FieldEntity caseFieldEntity) {
        return caseFieldEntity.getFieldType().getCollectionFieldType() != null
            && caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields() != null
            && !caseFieldEntity.getFieldType().getCollectionFieldType().getComplexFields().isEmpty();
    }
}
