package uk.gov.hmcts.ccd.definition.store.repository.entity;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CASELINK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORDER_SUMMARY;

public interface FieldEntity extends Serializable {

    String getReference();

    FieldTypeEntity getFieldType();

    boolean isSearchable();

    default String getBaseTypeString() {
        FieldTypeEntity baseFieldType = this.getFieldType().getBaseFieldType();
        if (baseFieldType != null) {
            return baseFieldType.getReference();
        } else {
            return getFieldType().getReference();
        }
    }

    default FieldTypeEntity getBaseType() {
        return ofNullable(this.getFieldType().getBaseFieldType()).orElse(this.getFieldType());
    }

    default boolean isCollectionFieldType() {
        return this.getFieldType().getCollectionFieldType() != null;
    }

    default boolean isCollectionOfComplex() {
        FieldTypeEntity collectionFieldType = this.getFieldType().getCollectionFieldType();
        return collectionFieldType != null && !collectionFieldType.getComplexFields().isEmpty();
    }

    default boolean isComplexFieldType() {
        return !isMetadataField() && this.getBaseTypeString().equalsIgnoreCase(BASE_COMPLEX);
    }

    default boolean isPredefinedComplexType() {
        return isComplexFieldType()
            && (this.getFieldType().getReference().equalsIgnoreCase(PREDEFINED_COMPLEX_ADDRESS_GLOBAL)
            || this.getFieldType().getReference().equalsIgnoreCase(PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK)
            || this.getFieldType().getReference().equalsIgnoreCase(PREDEFINED_COMPLEX_ADDRESS_UK)
            || this.getFieldType().getReference().equalsIgnoreCase(PREDEFINED_COMPLEX_CASELINK)
            || this.getFieldType().getReference().equalsIgnoreCase(PREDEFINED_COMPLEX_ORDER_SUMMARY));
    }

    boolean isMetadataField();

    default boolean isCompound() {
        return isCollectionFieldType() || isComplexFieldType();
    }

    default FieldTypeEntity getCollectionFieldType() {
        return getFieldType().getCollectionFieldType();
    }

    /**
     * Determine whether a nested field is searchable. A nested field is considered searchable if every field in the
     * path is searchable too. For example, for a path of 'LevelOne.LevelTwo.LevelThree', the fields at 'LevelOne',
     * 'LevelOne.LevelTwo' and 'LevelOne.LevelTwo.LevelThree' must all have the boolean searchable property set to
     * true. If no path is provided, the searchable value of the top level field is considered.
     * @return Whether the nested field is searchable.
     */
    @Transient
    default boolean isNestedFieldSearchable(String path) {
        return Strings.isNullOrEmpty(path) ? isSearchable() : isNestedFieldSearchable(getPathElements(path));
    }

    private boolean isNestedFieldSearchable(List<String> pathElements) {
        if (pathElements.isEmpty()) {
            return isSearchable();
        }

        return isSearchable() && findNestedElementByPath(pathElements.get(0)).orElseThrow(() ->
            new NullPointerException(String.format(
                "Unable to find nested field '%s' within field '%s'.", pathElements.get(0), getReference())))
            .isNestedFieldSearchable(getPathElementsTail(pathElements));
    }

    @Transient
    default Optional<FieldEntity> findNestedElementByPath(String path) {
        if (StringUtils.isBlank(path)) {
            return Optional.of(this);
        }
        if (this.getFieldType().getChildren().isEmpty()) {
            Optional.empty();
        }

        return reduce(this.getFieldType().getChildren(), getPathElements(path));
    }

    private Optional<FieldEntity> reduce(List<ComplexFieldEntity> caseFields, List<String> pathElements) {
        String firstPathElement = pathElements.get(0);

        Optional<FieldEntity> caseField = caseFields.stream()
            .filter(e -> e.getReference().equals(firstPathElement))
            .map(e -> (FieldEntity)e)
            .findFirst();

        if (!caseField.isPresent()) {
            return Optional.empty();
        }

        if (pathElements.size() == 1) {
            return caseField;
        } else {
            List<ComplexFieldEntity> complexFieldEntities = caseField.get().getFieldType().getChildren();

            return reduce(complexFieldEntities, getPathElementsTail(pathElements));
        }
    }

    private List<String> getPathElements(String path) {
        return Arrays.stream(path.trim().split("\\.")).collect(Collectors.toList());
    }

    private List<String> getPathElementsTail(List<String> pathElements) {
        return pathElements.subList(1, pathElements.size());
    }
}
