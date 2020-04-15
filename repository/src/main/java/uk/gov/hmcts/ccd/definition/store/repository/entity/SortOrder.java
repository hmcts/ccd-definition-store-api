package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SortOrder {

    @Column(name = "sort_order_direction")
    private String direction;

    @Column(name = "sort_order_priority")
    private Integer priority;

    public SortOrder() { }

    public SortOrder(Integer priority, String direction) {
        this.direction = direction;
        this.priority = priority;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
