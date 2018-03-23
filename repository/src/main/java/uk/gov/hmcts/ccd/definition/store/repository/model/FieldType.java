package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "")
public class FieldType {

    private String id = null;
    private String type = null;
    private String min = null;
    private String max = null;
    private String regularExpression = null;
    private List<FixedListItem> fixedListItems = new ArrayList<>();
    private List<CaseField> complexFields = new ArrayList<>();
    private FieldType collectionFieldType = null;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("min")
    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    @JsonProperty("max")
    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    @JsonProperty("regular_expression")
    public String getRegularExpression() {
        return regularExpression;
    }

    public void setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression;
    }

    @JsonProperty("fixed_list_items")
    public List<FixedListItem> getFixedListItems() {
        return fixedListItems;
    }

    public void setFixedListItems(List<FixedListItem> fixedListItems) {
        this.fixedListItems = fixedListItems;
    }

    @JsonProperty("complex_fields")
    public List<CaseField> getComplexFields() {
        return complexFields;
    }

    public void setComplexFields(List<CaseField> complexFields) {
        complexFields.stream().forEach(cf -> cf.setAcls(null));
        this.complexFields = complexFields;
    }

    @JsonProperty("collection_field_type")
    public FieldType getCollectionFieldType() {
        return collectionFieldType;
    }

    public void setCollectionFieldType(FieldType collectionFieldType) {
        this.collectionFieldType = collectionFieldType;
    }
}
