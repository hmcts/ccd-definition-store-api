package uk.gov.hmcts.ccd.definition.store.repository.am;

import feign.Param;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.ccd.definition.store.repository.AppConfigBasedAmPersistenceSwitch;
import uk.gov.hmcts.ccd.definition.store.repository.CCDCaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.VersionedDefinitionRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SwitchableCaseTypeRepository implements VersionedDefinitionRepository<CaseTypeEntity, Integer> {

    private static final String NOT_SUPPORTED = "This operation is not supported";

    private CCDCaseTypeRepository ccdCaseTypeRepository;
    private AmCaseTypeACLRepository amCaseTypeACLRepository;
    private AppConfigBasedAmPersistenceSwitch amPersistenceSwitch;

    public SwitchableCaseTypeRepository(final CCDCaseTypeRepository ccdCaseTypeRepository,
                                        final AmCaseTypeACLRepository amCaseTypeACLRepository,
                                        final AppConfigBasedAmPersistenceSwitch amPersistenceSwitch) {
        this.ccdCaseTypeRepository = ccdCaseTypeRepository;
        this.amCaseTypeACLRepository = amCaseTypeACLRepository;
        this.amPersistenceSwitch = amPersistenceSwitch;
    }

    @Override
    public Optional<Integer> findLastVersion(@Param("caseTypeReference") String caseTypeReference) {
        return ccdCaseTypeRepository.findLastVersion(caseTypeReference);
    }

    @Override
    public Optional<CaseTypeEntity> findFirstByReferenceOrderByVersionDesc(String reference) {
        Optional<CaseTypeEntity> ccdCaseType = ccdCaseTypeRepository.findFirstByReferenceOrderByVersionDesc(reference);
        ccdCaseType.ifPresent(this::setAmInfoIfRequired);
        return ccdCaseType;
    }

    public Integer caseTypeExistsInAnyJurisdiction(String caseTypeReference, String excludedJurisdictionReference) {
        return ccdCaseTypeRepository.caseTypeExistsInAnyJurisdiction(caseTypeReference, excludedJurisdictionReference);
    }

    public List<CaseTypeEntity> findByJurisdictionId(String jurisdiction) {
        List<CaseTypeEntity> ccdByJurisdictionId = ccdCaseTypeRepository.findByJurisdictionId(jurisdiction);
        return ccdByJurisdictionId.stream().map(this::setAmInfoIfRequired).collect(Collectors.toList());
    }

    public Optional<CaseTypeEntity> findCurrentVersionForReference(String caseTypeReference) {
        Optional<CaseTypeEntity> ccdCurrentVersionForReference = ccdCaseTypeRepository.findCurrentVersionForReference(caseTypeReference);
        ccdCurrentVersionForReference.ifPresent(this::setAmInfoIfRequired);
        return ccdCurrentVersionForReference;
    }

    public Optional<CaseTypeEntity> findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(String caseTypeReference) {
        Optional<CaseTypeEntity> ccdCurrentVersionForReference = ccdCaseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(caseTypeReference);
        ccdCurrentVersionForReference.ifPresent(this::setAmInfoIfRequired);
        return ccdCurrentVersionForReference;
    }

    @Override
    public List<CaseTypeEntity> findAll() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public List<CaseTypeEntity> findAll(Sort sort) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public Page<CaseTypeEntity> findAll(Pageable pageable) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public List<CaseTypeEntity> findAllById(Iterable<Integer> ids) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void deleteById(Integer id) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void delete(CaseTypeEntity entity) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void deleteAll(Iterable<? extends CaseTypeEntity> entities) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public <S extends CaseTypeEntity> S save(S entity) {
        S ccdSaved = ccdCaseTypeRepository.save(entity);
        amCaseTypeACLRepository.saveAmInfoFor(CaseTypeAmInfo.builder().caseTypeACLs(entity.getCaseTypeACLEntities()).build());
        return ccdSaved;
    }

    @Override
    public <S extends CaseTypeEntity> List<S> saveAll(Iterable<S> entities) {
        List<S> ccdSaved = ccdCaseTypeRepository.saveAll(entities);
        List<CaseTypeAmInfo> caseTypeAmInfos = ccdSaved.stream().map(s -> CaseTypeAmInfo.builder().caseTypeACLs(s.getCaseTypeACLEntities()).build()).collect(Collectors.toList());
        amCaseTypeACLRepository.saveAmInfoFor(caseTypeAmInfos);
        return ccdSaved;
    }

    @Override
    public Optional<CaseTypeEntity> findById(Integer id) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean existsById(Integer id) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public <S extends CaseTypeEntity> S saveAndFlush(S entity) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void deleteInBatch(Iterable<CaseTypeEntity> entities) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public CaseTypeEntity getOne(Integer id) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public <S extends CaseTypeEntity> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public <S extends CaseTypeEntity> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public <S extends CaseTypeEntity> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public <S extends CaseTypeEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public <S extends CaseTypeEntity> long count(Example<S> example) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public <S extends CaseTypeEntity> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    private CaseTypeEntity setAmInfoIfRequired(final CaseTypeEntity ccdCaseTypeEntity) {
        if (amPersistenceSwitch.getReadDataSourceFor(ccdCaseTypeEntity.getReference()).equals(AmPersistenceReadSource.FROM_AM)) {
            //ccdCaseTypeEntity.setCaseTypeACLEntities(amCaseTypeACLRepository.getAmInfoFor(ccdCaseTypeEntity.getReference()).getCaseTypeACLs());

            CaseTypeAmInfo caseTypeAmInfo = amCaseTypeACLRepository.getAmInfoFor(ccdCaseTypeEntity.getReference());
            caseTypeAmInfo.getCaseTypeACLs().forEach(amCaseTypeACLEntity -> {
                ccdCaseTypeEntity.getCaseTypeACLEntities().forEach(ccdCaseTypeACLEntity -> {
                    if (amCaseTypeACLEntity.getCaseType().getName().equals(ccdCaseTypeACLEntity.getCaseType().getName())
                        && amCaseTypeACLEntity.getUserRole().getName().equals(ccdCaseTypeACLEntity.getUserRole().getName())) {
                        ccdCaseTypeACLEntity.setCreate(amCaseTypeACLEntity.getCreate());
                        ccdCaseTypeACLEntity.setRead(amCaseTypeACLEntity.getRead());
                        ccdCaseTypeACLEntity.setUpdate(amCaseTypeACLEntity.getUpdate());
                        ccdCaseTypeACLEntity.setDelete(amCaseTypeACLEntity.getDelete());
                    }
                });
            });
        }
        return ccdCaseTypeEntity;
    }
}
