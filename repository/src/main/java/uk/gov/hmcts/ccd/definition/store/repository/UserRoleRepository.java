package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<AccessProfileEntity, Integer> {

    Optional<AccessProfileEntity> findTopByReference(String reference);

    List<AccessProfileEntity> findByReferenceIn(List<String> reference);

    @Override
    @Query(
        value = "SELECT * FROM role r WHERE r.dtype='USERROLE'",
        nativeQuery = true)
    List<AccessProfileEntity> findAll();
}
