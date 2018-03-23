package uk.gov.hmcts.ccd.definition.store.repository;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DefinitionRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

}
