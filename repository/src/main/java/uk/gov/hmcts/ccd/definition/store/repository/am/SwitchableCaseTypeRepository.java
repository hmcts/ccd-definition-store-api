package uk.gov.hmcts.ccd.definition.store.repository.am;

import feign.Param;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.ccd.definition.store.repository.AppConfigBasedAmPersistenceSwitch;
import uk.gov.hmcts.ccd.definition.store.repository.CCDCaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.VersionedDefinitionRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Configuration
public class SwitchableCaseTypeRepository implements VersionedDefinitionRepository<CaseTypeEntity, Integer> {

    private static final String NOT_SUPPORTED = "This operation is not supported";

    private CCDCaseTypeRepository ccdCaseTypeRepository;
    private AmCaseTypeACLRepository amCaseTypeACLRepository;
    private AppConfigBasedAmPersistenceSwitch amPersistenceSwitch;

    @Autowired
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
        ccdCaseType.ifPresent(this::getAmInfoIfRequired);
        return ccdCaseType;
    }

    public Integer caseTypeExistsInAnyJurisdiction(String caseTypeReference, String excludedJurisdictionReference) {
        return ccdCaseTypeRepository.caseTypeExistsInAnyJurisdiction(caseTypeReference, excludedJurisdictionReference);
    }

    public List<CaseTypeEntity> findByJurisdictionId(String jurisdiction) {
        List<CaseTypeEntity> ccdByJurisdictionId = ccdCaseTypeRepository.findByJurisdictionId(jurisdiction);
        return getAmInfosIfRequired(ccdByJurisdictionId);
    }

    public Optional<CaseTypeEntity> findCurrentVersionForReference(String caseTypeReference) {
        Optional<CaseTypeEntity> ccdCurrentVersionForReference = ccdCaseTypeRepository.findCurrentVersionForReference(caseTypeReference);
        ccdCurrentVersionForReference.ifPresent(this::getAmInfoIfRequired);
        return ccdCurrentVersionForReference;
    }

    public Optional<CaseTypeEntity> findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(String caseTypeReference) {
        Optional<CaseTypeEntity> ccdCurrentVersionForReference = ccdCaseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(caseTypeReference);
        ccdCurrentVersionForReference.ifPresent(this::getAmInfoIfRequired);
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
        if (setAmInfoIfRequired(entity)) {
            amCaseTypeACLRepository.saveAmInfoFor(buildCaseTypeAMInfo(entity));
        }
        return ccdSaved;
    }

    @Override
    public <S extends CaseTypeEntity> List<S> saveAll(Iterable<S> entities) {
        List<S> ccdSaved = ccdCaseTypeRepository.saveAll(entities);
        List<CaseTypeAmInfo> caseTypeAmInfos = ccdSaved.stream()
            .filter(s -> setAmInfoIfRequired(s))
            .map(s -> buildCaseTypeAMInfo(s))
            .collect(toList());

        if (CollectionUtils.isNotEmpty(caseTypeAmInfos)) {
            amCaseTypeACLRepository.saveAmInfoFor(caseTypeAmInfos);
        }
        return ccdSaved;
    }

    private <S extends CaseTypeEntity> CaseTypeAmInfo buildCaseTypeAMInfo(S entity) {
        return CaseTypeAmInfo.builder()
            .caseTypeACLs(entity.getCaseTypeACLEntities())
            .securityClassification(entity.getSecurityClassification())
            .caseReference(entity.getReference())
            .jurisdictionId(entity.getJurisdiction().getReference())
            //.userRole(entity.getCaseTypeACLEntities().get(0).getUserRole().getName())
            .build();
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

    private CaseTypeEntity getAmInfoIfRequired(final CaseTypeEntity ccdCaseTypeEntity) {
        if (amPersistenceSwitch.getReadDataSourceFor(ccdCaseTypeEntity.getReference()).equals(AmPersistenceReadSource.FROM_AM)) {
            CaseTypeAmInfo caseTypeAmInfo = amCaseTypeACLRepository.getAmInfoFor(ccdCaseTypeEntity.getReference());
            replaceCcdCaseTypeACLWithAmCaseTypeACL(ccdCaseTypeEntity, caseTypeAmInfo);
        }
        return ccdCaseTypeEntity;
    }

    private List<CaseTypeEntity> getAmInfosIfRequired(List<CaseTypeEntity> ccdCaseTypeEntities) {
        List<CaseTypeEntity> ccdCaseTypeEntitiesForAmInfos = ccdCaseTypeEntities.stream()
            .filter(ccdCaseTypeEntity -> amPersistenceSwitch.getReadDataSourceFor(ccdCaseTypeEntity.getReference())
                .equals(AmPersistenceReadSource.FROM_AM))
            .collect(toList());
        List<String> caseTypeReferences = ccdCaseTypeEntitiesForAmInfos.stream()
            .map(CaseTypeEntity::getReference)
            .collect(toList());
        List<CaseTypeAmInfo> caseTypeAmInfos = amCaseTypeACLRepository.getAmInfoFor(caseTypeReferences);

        ccdCaseTypeEntitiesForAmInfos.forEach(ccdCaseTypeEntity -> {
            caseTypeAmInfos.forEach(caseTypeAmInfo -> {
                if (ccdCaseTypeEntity.getReference().equals(caseTypeAmInfo.getJurisdictionId())) {
                    replaceCcdCaseTypeACLWithAmCaseTypeACL(ccdCaseTypeEntity, caseTypeAmInfo);
                }
            });
        });

        return ccdCaseTypeEntities;
    }

    private void replaceCcdCaseTypeACLWithAmCaseTypeACL(CaseTypeEntity ccdCaseTypeEntity, CaseTypeAmInfo caseTypeAmInfo) {
        for (CaseTypeACLEntity amCaseTypeACLEntity : caseTypeAmInfo.getCaseTypeACLs()) {
            boolean isCaseTypeEntityFound = false;
            for (CaseTypeACLEntity ccdCaseTypeACLEntity : ccdCaseTypeEntity.getCaseTypeACLEntities()) {
                if (amCaseTypeACLEntity.getCaseType().getName().equals(ccdCaseTypeACLEntity.getCaseType().getName())
                    && amCaseTypeACLEntity.getUserRole().getName().equals(ccdCaseTypeACLEntity.getUserRole().getName())) {
                    ccdCaseTypeACLEntity.setCreate(amCaseTypeACLEntity.getCreate());
                    ccdCaseTypeACLEntity.setRead(amCaseTypeACLEntity.getRead());
                    ccdCaseTypeACLEntity.setUpdate(amCaseTypeACLEntity.getUpdate());
                    ccdCaseTypeACLEntity.setDelete(amCaseTypeACLEntity.getDelete());
                    isCaseTypeEntityFound = true;
                    break;
                }
            }
            if (!isCaseTypeEntityFound) {
                throw new IllegalStateException("CCD and AM repositories out of sync. AM library returned " +
                    "permissions for [case type: \"" + amCaseTypeACLEntity.getCaseType().getName() + "\", " +
                    "role: \"" + amCaseTypeACLEntity.getUserRole().getName() + "\"] combination that does " +
                    "not exist in the CCD database");
            }
        }
    }

    private boolean setAmInfoIfRequired(final CaseTypeEntity ccdCaseTypeEntity) {
        return amPersistenceSwitch.getWriteDataSourceFor(ccdCaseTypeEntity.getReference()).equals(AmPersistenceWriteDestination.TO_AM);
    }
}
