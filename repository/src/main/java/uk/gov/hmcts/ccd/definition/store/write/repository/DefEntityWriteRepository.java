package uk.gov.hmcts.ccd.definition.store.write.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefEntity;

@Repository
public class DefEntityWriteRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public <T extends DefEntity > T save(T entity) {

        if (entity.getId() == null) {
            em.persist(entity);
            return entity;
        } else {
            return em.merge(entity);
        }
    }

    @Transactional
    public <T extends DefEntity > List<T> save(List<T> entities) {
        List<T> result = new ArrayList<>();
        entities.forEach(e -> {
            T save = save(e);
            result.add(save);
        });
        return result;
    }

}
