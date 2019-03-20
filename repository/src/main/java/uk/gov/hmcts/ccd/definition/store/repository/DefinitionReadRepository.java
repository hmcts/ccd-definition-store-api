package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface DefinitionReadRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    @Override
    default <S extends T> List<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException("writes not allowed");
    }

    @Override
    default <S extends T> S save(S entity)
    {
        throw new UnsupportedOperationException("writes not allowed");
    }
}
