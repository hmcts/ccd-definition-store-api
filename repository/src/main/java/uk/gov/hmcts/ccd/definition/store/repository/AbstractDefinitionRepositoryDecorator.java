package uk.gov.hmcts.ccd.definition.store.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
abstract class AbstractDefinitionRepositoryDecorator<T, ID extends Serializable, R extends DefinitionRepository<T, ID>> implements DefinitionRepository<T, ID> {

    protected R repository;

    protected AbstractDefinitionRepositoryDecorator(R repository) {
        this.repository = repository;
    }

    /*FORWARDING METHODS*/
    @Override
    public <S extends T> S save(S s) {
        return repository.save(s);
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        return repository.findAll(example);
    }

    @Override
    public List<T> findAll(Sort sort) {
        return repository.findAll(sort);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        return repository.findAll(example, sort);
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        return repository.findAll(example, pageable);
    }

    @Override
    public List<T> findAllById(Iterable<ID> iterable) {
        return repository.findAllById(iterable);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        return repository.count(example);
    }

    @Override
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    @Override
    public void delete(T t) {
        repository.delete(t);
    }

    @Override
    public void deleteAll(Iterable<? extends T> iterable) {
        repository.deleteAll(iterable);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> iterable) {
        return repository.saveAll(iterable);
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    public void flush() {
        repository.flush();
    }

    @Override
    public <S extends T> S saveAndFlush(S s) {
        return repository.saveAndFlush(s);
    }

    @Override
    public void deleteInBatch(Iterable<T> iterable) {
        repository.deleteInBatch(iterable);
    }

    @Override
    public void deleteAllInBatch() {
        repository.deleteAllInBatch();
    }

    @Override
    public T getOne(ID id) {
        return repository.getOne(id);
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        return repository.findOne(example);
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        return repository.exists(example);
    }


    @Override
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

}
