package uk.gov.hmcts.ccd.definition.store.write.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefEntity;

@NoRepositoryBean
public interface WriteDefinitionRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

}
