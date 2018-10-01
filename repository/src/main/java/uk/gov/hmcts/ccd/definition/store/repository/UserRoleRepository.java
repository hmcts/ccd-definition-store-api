package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Integer> {

    Optional<UserRoleEntity> findTopByReference(String reference);

    List<UserRoleEntity> findByReferenceIn(List<String> reference);
}
