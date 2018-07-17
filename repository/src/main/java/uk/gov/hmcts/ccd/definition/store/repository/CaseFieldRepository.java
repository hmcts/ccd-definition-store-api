package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;

import java.util.List;

public interface CaseFieldRepository extends JpaRepository<CaseFieldEntity, Integer> {

    List<CaseFieldEntity> findByDataFieldTypeAndCaseTypeNull(DataFieldType dataFieldType);

}
