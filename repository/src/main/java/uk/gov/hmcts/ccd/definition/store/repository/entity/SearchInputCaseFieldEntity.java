package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

@Table(name = "search_input_case_field")
@Entity
public class SearchInputCaseFieldEntity extends GenericLayoutEntity {

}
