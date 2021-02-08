package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Integer> {

    Optional<UserRoleEntity> findTopByReference(String reference);

    List<UserRoleEntity> findByReferenceIn(List<String> reference);

    @Override
    @Query(
        value = "SELECT * FROM role r WHERE r.dtype='USERROLE'",
        nativeQuery = true)
    List<UserRoleEntity> findAll();

    @Query(
        value = "SELECT r.reference FROM role r WHERE r.id IN :ids",
        nativeQuery = true)
    Set<String> findAllReferenceById(Iterable<Integer> ids);
}
