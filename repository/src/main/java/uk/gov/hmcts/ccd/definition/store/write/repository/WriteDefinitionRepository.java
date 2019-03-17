package uk.gov.hmcts.ccd.definition.store.write.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface WriteDefinitionRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

}
