package uk.gov.hmcts.ccd.definition.store.write.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.VersionableDefEntity;

@Repository
public class CustomDefEntityRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public VersionableDefEntity save(VersionableDefEntity entity) {

        if (entity.getId() == null) {
            em.persist(entity);
            return entity;
        } else {
            return em.merge(entity);
        }
    }
}
