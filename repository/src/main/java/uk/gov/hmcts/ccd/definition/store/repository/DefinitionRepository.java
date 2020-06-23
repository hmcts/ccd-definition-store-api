package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public interface DefinitionRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

}
