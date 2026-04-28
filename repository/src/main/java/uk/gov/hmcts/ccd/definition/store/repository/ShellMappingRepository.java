package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;

public interface ShellMappingRepository extends JpaRepository<ShellMappingEntity, Integer> {

}
