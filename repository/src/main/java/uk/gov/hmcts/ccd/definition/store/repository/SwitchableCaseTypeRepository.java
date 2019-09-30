package uk.gov.hmcts.ccd.definition.store.repository;

import com.google.common.collect.Lists;
import feign.Param;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SwitchableCaseTypeRepository implements VersionedDefinitionRepository<CaseTypeEntity, Integer> {

    public static final String NOT_SUPPORTED = "This operation is not supported";

    private CCDCaseTypeRepository ccdCaseTypeRepository;
    private AMCaseTypeRepository amCaseTypeRepository;
    private DefaultRoleSetupImportService defaultRoleSetupImportService;
    private AccessManagementService accessManagementService;

    public SwitchableCaseTypeRepository(final CCDCaseTypeRepository ccdCaseTypeRepository,
                                        final AMCaseTypeRepository amCaseTypeRepository,
                                        final DefaultRoleSetupImportService defaultRoleSetupImportService,
                                        final AccessManagementService accessManagementService) {
        this.ccdCaseTypeRepository = ccdCaseTypeRepository;
        this.amCaseTypeRepository = amCaseTypeRepository;
        this.defaultRoleSetupImportService = defaultRoleSetupImportService;
        this.accessManagementService = accessManagementService;
    }

    @Override
    public Optional<Integer> findLastVersion(@Param("caseTypeReference") String caseTypeReference) {
        return ccdCaseTypeRepository.findLastVersion(caseTypeReference);
    }

    @Override
    public Optional<CaseTypeEntity> findFirstByReferenceOrderByVersionDesc(String reference) {
        Optional<CaseTypeEntity> ccdCaseType = ccdCaseTypeRepository.findFirstByReferenceOrderByVersionDesc(reference);
        ccdCaseType
            .ifPresent(ccdCaseTypeEntity ->
                           amCaseTypeRepository.findFirstByReferenceOrderByVersionDesc(reference)
                               .ifPresent(amCaseTypeEntity ->
                                              ccdCaseTypeEntity.setCaseTypeACLEntities(amCaseTypeEntity.getCaseTypeACLEntities())));
        return ccdCaseType;
    }

    public Integer caseTypeExistsInAnyJurisdiction(String caseTypeReference, String excludedJurisdictionReference) {
        return ccdCaseTypeRepository.caseTypeExistsInAnyJurisdiction(caseTypeReference, excludedJurisdictionReference);
    }

    public List<CaseTypeEntity> findByJurisdictionId(String jurisdiction) {
        List<CaseTypeEntity> ccdByJurisdictionId = ccdCaseTypeRepository.findByJurisdictionId(jurisdiction);
        List<CaseTypeEntity> amByJurisdictionId = amCaseTypeRepository.findByJurisdictionId(jurisdiction);
        Map<Integer, CaseTypeEntity> amCaseTypesMap = amByJurisdictionId.stream().collect(Collectors.toMap(CaseTypeEntity::getId, Function.identity()));
        return ccdByJurisdictionId.stream().map(ccdCaseTypeEntity -> {
            ccdCaseTypeEntity.setCaseTypeACLEntities(amCaseTypesMap.get(ccdCaseTypeEntity.getId()).getCaseTypeACLEntities());
            return ccdCaseTypeEntity;
        }).collect(Collectors.toList());
    }

    public Optional<CaseTypeEntity> findCurrentVersionForReference(String caseTypeReference) {
        Optional<CaseTypeEntity> ccdCurrentVersionForReference = ccdCaseTypeRepository.findCurrentVersionForReference(caseTypeReference);
        ccdCurrentVersionForReference
            .ifPresent(ccdCaseTypeEntity ->
                           amCaseTypeRepository.findCurrentVersionForReference(caseTypeReference)
                               .ifPresent(amCaseTypeEntity ->
                                              ccdCaseTypeEntity.setCaseTypeACLEntities(amCaseTypeEntity.getCaseTypeACLEntities())));
        return ccdCurrentVersionForReference;
    }

    public Optional<CaseTypeEntity> findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(String caseTypeReference) {
        Optional<CaseTypeEntity> ccdCurrentVersionForReference = ccdCaseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(caseTypeReference);
        ccdCurrentVersionForReference
            .ifPresent(ccdCaseTypeEntity ->
                           amCaseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(caseTypeReference)
                               .ifPresent(amCaseTypeEntity ->
                                              ccdCaseTypeEntity.setCaseTypeACLEntities(amCaseTypeEntity.getCaseTypeACLEntities())));
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
        S amSaved = amCaseTypeRepository.save(entity);
        S ccdSaved = ccdCaseTypeRepository.save(entity);
        ccdSaved.setCaseTypeACLEntities(amSaved.getCaseTypeACLEntities());
        return ccdSaved;
    }

    @Override
    public <S extends CaseTypeEntity> List<S> saveAll(Iterable<S> entities) {
        List<S> result = Lists.newArrayList();
        entities.forEach(entity -> result.add(save(entity)));
        return result;
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

}
