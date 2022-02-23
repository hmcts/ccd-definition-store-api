package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class Category {

    private String categoryId;
    private String categoryLabel;
    private String parentCategoryId;
    private LocalDate liveFrom;
    private LocalDate liveTo;
    private Integer displayOrder;
    private String caseTypeId;

}
