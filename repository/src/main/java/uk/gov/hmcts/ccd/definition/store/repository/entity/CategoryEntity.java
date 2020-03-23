package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "category")
@Entity
public class CategoryEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "label")
    private String label;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "parent_category_id")
    private String parentCategoryId;

    public Integer getId() {
        return id;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer order) {
        this.displayOrder = order;
    }

    public String getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
}
