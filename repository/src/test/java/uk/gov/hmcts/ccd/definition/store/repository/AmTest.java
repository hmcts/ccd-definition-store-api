package uk.gov.hmcts.ccd.definition.store.repository;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.repository.am.AmCaseTypeACLRepository;
import uk.gov.hmcts.ccd.definition.store.repository.am.AmPersistenceReadSource;
import uk.gov.hmcts.ccd.definition.store.repository.am.CaseTypeAmInfo;
import uk.gov.hmcts.ccd.definition.store.repository.am.SwitchableCaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

public class AmTest {

    private static final String CASE_TYPE_REFERENCE_1 = "AAT";
    private static final String CASE_TYPE_REFERENCE_2 = "MAPPER";
    private static final String USER_ROLE_1 = "CASEWORKER";
    private static final String USER_ROLE_2 = "CITIZEN";
    private static final String JURISDICTION = "AUTOTEST1";

    @Mock
    private CCDCaseTypeRepository ccdCaseTypeRepository;
    @Mock
    private AmCaseTypeACLRepository amCaseTypeACLRepository;
    @Mock
    private AppConfigBasedAmPersistenceSwitch amPersistenceSwitch;

    private SwitchableCaseTypeRepository switchableCaseTypeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        switchableCaseTypeRepository = new SwitchableCaseTypeRepository(ccdCaseTypeRepository, amCaseTypeACLRepository, amPersistenceSwitch);
    }

    private CaseTypeEntity createCaseTypeEntity(String caseType, List<ACLEntry> aclEntries) {

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        List<CaseTypeACLEntity> caseTypeACLEntities = new ArrayList<>();

        aclEntries.forEach(aclEntity -> caseTypeACLEntities.add(createCaseTypeACLEntity(caseType, aclEntity.getRole(),
            aclEntity.getCreate(), aclEntity.getRead(), aclEntity.getUpdate(), aclEntity.getDelete())));

        caseTypeEntity.setReference(caseType);
        caseTypeEntity.setCaseTypeACLEntities(caseTypeACLEntities);

        return caseTypeEntity;
    }

    private CaseTypeEntity createInnerCaseTypeEntity(String caseType) {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(caseType);
        return caseTypeEntity;
    }

    private UserRoleEntity createUserRole(String role) {
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setReference(role);
        return userRoleEntity;
    }

    private CaseTypeACLEntity createCaseTypeACLEntity(String caseType, String role, boolean create,
                                                      boolean read, boolean update, boolean delete) {

        CaseTypeACLEntity caseTypeACLEntity = new CaseTypeACLEntity();
        caseTypeACLEntity.setCaseType(createInnerCaseTypeEntity(caseType));
        caseTypeACLEntity.setUserRole(createUserRole(role));
        caseTypeACLEntity.setCreate(create);
        caseTypeACLEntity.setRead(read);
        caseTypeACLEntity.setUpdate(update);
        caseTypeACLEntity.setDelete(delete);
        return caseTypeACLEntity;
    }

    private CaseTypeAmInfo createCaseTypeAmInfo(String caseType, List<ACLEntry> aclEntries) {

        List<CaseTypeACLEntity> caseTypeACLEntities = new ArrayList<>();
        aclEntries.forEach(aclEntity -> caseTypeACLEntities.add(createCaseTypeACLEntity(caseType, aclEntity.getRole(),
            aclEntity.getCreate(), aclEntity.getRead(), aclEntity.getUpdate(), aclEntity.getDelete())));

        return CaseTypeAmInfo.builder()
            .caseReference(caseType)
            .caseTypeACLs(caseTypeACLEntities)
            .build();
    }

    // read one
    @Test
    void readFlagOnForSingleCaseType_useAmCaseTypeACL() {

        List<ACLEntry> ccdACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, true, true, true, true)
        );

        List<ACLEntry> amACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, false, false, false, false)
        );

        CaseTypeEntity ccdCaseTypeEntity = createCaseTypeEntity(CASE_TYPE_REFERENCE_1, ccdACLEntries);
        CaseTypeAmInfo caseTypeAmInfo = createCaseTypeAmInfo(CASE_TYPE_REFERENCE_1, amACLEntries);

        Mockito.when(amPersistenceSwitch.getReadDataSourceFor(anyString()))
            .thenReturn(AmPersistenceReadSource.FROM_AM);
        Mockito.when(ccdCaseTypeRepository.findCurrentVersionForReference(anyString()))
            .thenReturn(Optional.of(ccdCaseTypeEntity));
        Mockito.when(amCaseTypeACLRepository.getAmInfoFor(anyString()))
            .thenReturn(caseTypeAmInfo);

        Optional<CaseTypeEntity> result = switchableCaseTypeRepository.findCurrentVersionForReference(CASE_TYPE_REFERENCE_1);

        assertThat(result.isPresent()).isEqualTo(true);
        result.ifPresent(caseTypeEntity -> {
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getCreate()).isEqualTo(false);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getRead()).isEqualTo(false);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getUpdate()).isEqualTo(false);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getDelete()).isEqualTo(false);
        });
    }

    @Test
    void readFlagOffForSingleCaseType_useCcdCaseTypeACL() {

        List<ACLEntry> ccdACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, true, true, true, true)
        );

        List<ACLEntry> amACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, false, false, false, false)
        );

        CaseTypeEntity ccdCaseTypeEntity = createCaseTypeEntity(CASE_TYPE_REFERENCE_1, ccdACLEntries);
        CaseTypeAmInfo caseTypeAmInfo = createCaseTypeAmInfo(CASE_TYPE_REFERENCE_1, amACLEntries);

        Mockito.when(amPersistenceSwitch.getReadDataSourceFor(anyString()))
            .thenReturn(AmPersistenceReadSource.FROM_CCD);
        Mockito.when(ccdCaseTypeRepository.findCurrentVersionForReference(anyString()))
            .thenReturn(Optional.of(ccdCaseTypeEntity));
        Mockito.when(amCaseTypeACLRepository.getAmInfoFor(anyString()))
            .thenReturn(caseTypeAmInfo);

        Optional<CaseTypeEntity> result = switchableCaseTypeRepository.findCurrentVersionForReference(CASE_TYPE_REFERENCE_1);

        assertThat(result.isPresent()).isEqualTo(true);
        result.ifPresent(caseTypeEntity -> {
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getCreate()).isEqualTo(true);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getRead()).isEqualTo(true);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getUpdate()).isEqualTo(true);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getDelete()).isEqualTo(true);
        });
    }

    @Test
    void readFlagOnForSingleCaseType_useAmCaseTypeACLWithMultipleRoles() {

        List<ACLEntry> ccdACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, true, true, true, true),
            new ACLEntry(USER_ROLE_2, false, false, false, false)
        );

        List<ACLEntry> amACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, false, false, false, false),
            new ACLEntry(USER_ROLE_2, true, true, true, true)
        );

        CaseTypeEntity ccdCaseTypeEntity = createCaseTypeEntity(CASE_TYPE_REFERENCE_1, ccdACLEntries);
        CaseTypeAmInfo caseTypeAmInfo = createCaseTypeAmInfo(CASE_TYPE_REFERENCE_1, amACLEntries);

        Mockito.when(amPersistenceSwitch.getReadDataSourceFor(anyString()))
            .thenReturn(AmPersistenceReadSource.FROM_AM);
        Mockito.when(ccdCaseTypeRepository.findCurrentVersionForReference(anyString()))
            .thenReturn(Optional.of(ccdCaseTypeEntity));
        Mockito.when(amCaseTypeACLRepository.getAmInfoFor(anyString()))
            .thenReturn(caseTypeAmInfo);

        Optional<CaseTypeEntity> result = switchableCaseTypeRepository.findCurrentVersionForReference(CASE_TYPE_REFERENCE_1);

        assertThat(result.isPresent()).isEqualTo(true);
        result.ifPresent(caseTypeEntity -> {
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getCreate()).isEqualTo(false);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getRead()).isEqualTo(false);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getUpdate()).isEqualTo(false);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getDelete()).isEqualTo(false);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(1).getCreate()).isEqualTo(true);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(1).getRead()).isEqualTo(true);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(1).getUpdate()).isEqualTo(true);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(1).getDelete()).isEqualTo(true);
        });
    }

    // read multiple
    @Test
    void readFlagOnForMultipleCaseTypes_useAmCaseTypeACLForAllCaseTypes() {

        List<ACLEntry> ccdACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, true, true, true, true)
        );

        List<ACLEntry> amACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, false, false, false, false)
        );

        CaseTypeEntity ccdCaseTypeEntity1 = createCaseTypeEntity(CASE_TYPE_REFERENCE_1, ccdACLEntries);
        CaseTypeEntity ccdCaseTypeEntity2 = createCaseTypeEntity(CASE_TYPE_REFERENCE_2, ccdACLEntries);
        CaseTypeAmInfo caseTypeAmInfo1 = createCaseTypeAmInfo(CASE_TYPE_REFERENCE_1, amACLEntries);
        CaseTypeAmInfo caseTypeAmInfo2 = createCaseTypeAmInfo(CASE_TYPE_REFERENCE_2, amACLEntries);

        Mockito.when(amPersistenceSwitch.getReadDataSourceFor(anyString()))
            .thenReturn(AmPersistenceReadSource.FROM_AM);
        Mockito.when(ccdCaseTypeRepository.findByJurisdictionId(anyString()))
            .thenReturn(ImmutableList.of(ccdCaseTypeEntity1, ccdCaseTypeEntity2));
        Mockito.when(amCaseTypeACLRepository.getAmInfoFor(anyList()))
            .thenReturn(ImmutableList.of(caseTypeAmInfo1, caseTypeAmInfo2));

        List<CaseTypeEntity> result = switchableCaseTypeRepository.findByJurisdictionId(JURISDICTION);

        assertThat(result.size()).isEqualTo(2);
        result.forEach(caseTypeEntity -> {
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getCreate()).isEqualTo(false);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getRead()).isEqualTo(false);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getUpdate()).isEqualTo(false);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getDelete()).isEqualTo(false);
        });
    }

    @Test
    void readFlagOffForMultipleCaseTypes_useCcdCaseTypeACLForAllCaseTypes() {

        List<ACLEntry> ccdACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, true, true, true, true)
        );

        List<ACLEntry> amACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, false, false, false, false)
        );

        CaseTypeEntity ccdCaseTypeEntity1 = createCaseTypeEntity(CASE_TYPE_REFERENCE_1, ccdACLEntries);
        CaseTypeEntity ccdCaseTypeEntity2 = createCaseTypeEntity(CASE_TYPE_REFERENCE_2, ccdACLEntries);
        CaseTypeAmInfo caseTypeAmInfo1 = createCaseTypeAmInfo(CASE_TYPE_REFERENCE_1, amACLEntries);
        CaseTypeAmInfo caseTypeAmInfo2 = createCaseTypeAmInfo(CASE_TYPE_REFERENCE_2, amACLEntries);

        Mockito.when(amPersistenceSwitch.getReadDataSourceFor(anyString()))
            .thenReturn(AmPersistenceReadSource.FROM_CCD);
        Mockito.when(ccdCaseTypeRepository.findByJurisdictionId(anyString()))
            .thenReturn(ImmutableList.of(ccdCaseTypeEntity1, ccdCaseTypeEntity2));
        Mockito.when(amCaseTypeACLRepository.getAmInfoFor(anyList()))
            .thenReturn(ImmutableList.of(caseTypeAmInfo1, caseTypeAmInfo2));

        List<CaseTypeEntity> result = switchableCaseTypeRepository.findByJurisdictionId(JURISDICTION);

        assertThat(result.size()).isEqualTo(2);
        result.forEach(caseTypeEntity -> {
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getCreate()).isEqualTo(true);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getRead()).isEqualTo(true);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getUpdate()).isEqualTo(true);
            assertThat(caseTypeEntity.getCaseTypeACLEntities().get(0).getDelete()).isEqualTo(true);
        });
    }

    @Test
    void readFlagOnForFirstCaseTypeAndOffForSecondCaseType_useAmCaseTypeACLForFirstCaseTypeAndCcdCaseTypeACLForSecondCaseType() {

        List<ACLEntry> ccdACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, true, true, true, true)
        );

        List<ACLEntry> amACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, false, false, false, false)
        );

        CaseTypeEntity ccdCaseTypeEntity1 = createCaseTypeEntity(CASE_TYPE_REFERENCE_1, ccdACLEntries);
        CaseTypeEntity ccdCaseTypeEntity2 = createCaseTypeEntity(CASE_TYPE_REFERENCE_2, ccdACLEntries);
        CaseTypeAmInfo caseTypeAmInfo1 = createCaseTypeAmInfo(CASE_TYPE_REFERENCE_1, amACLEntries);
        CaseTypeAmInfo caseTypeAmInfo2 = createCaseTypeAmInfo(CASE_TYPE_REFERENCE_2, amACLEntries);

        Mockito.when(amPersistenceSwitch.getReadDataSourceFor(CASE_TYPE_REFERENCE_1))
            .thenReturn(AmPersistenceReadSource.FROM_AM);
        Mockito.when(amPersistenceSwitch.getReadDataSourceFor(CASE_TYPE_REFERENCE_2))
            .thenReturn(AmPersistenceReadSource.FROM_CCD);
        Mockito.when(ccdCaseTypeRepository.findByJurisdictionId(anyString()))
            .thenReturn(ImmutableList.of(ccdCaseTypeEntity1, ccdCaseTypeEntity2));
        Mockito.when(amCaseTypeACLRepository.getAmInfoFor(anyList()))
            .thenReturn(ImmutableList.of(caseTypeAmInfo1, caseTypeAmInfo2));

        List<CaseTypeEntity> result = switchableCaseTypeRepository.findByJurisdictionId(JURISDICTION);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getCaseTypeACLEntities().get(0).getCreate()).isEqualTo(false);
        assertThat(result.get(0).getCaseTypeACLEntities().get(0).getRead()).isEqualTo(false);
        assertThat(result.get(0).getCaseTypeACLEntities().get(0).getUpdate()).isEqualTo(false);
        assertThat(result.get(0).getCaseTypeACLEntities().get(0).getDelete()).isEqualTo(false);
        assertThat(result.get(1).getCaseTypeACLEntities().get(0).getCreate()).isEqualTo(true);
        assertThat(result.get(1).getCaseTypeACLEntities().get(0).getRead()).isEqualTo(true);
        assertThat(result.get(1).getCaseTypeACLEntities().get(0).getUpdate()).isEqualTo(true);
        assertThat(result.get(1).getCaseTypeACLEntities().get(0).getDelete()).isEqualTo(true);
    }

    @Test
    void readFlagOnForFirstCaseTypeAndOffForSecondCaseType_useAmCaseTypeACLForFirstCaseTypeAndCcdCaseTypeACLForSecondCaseTypeWithMultipleRoles() {

        List<ACLEntry> ccdACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, true, true, true, true),
            new ACLEntry(USER_ROLE_2, false, false, false, false)
        );

        List<ACLEntry> amACLEntries = ImmutableList.of(
            new ACLEntry(USER_ROLE_1, false, false, false, false),
            new ACLEntry(USER_ROLE_2, true, true, true, true)
        );

        CaseTypeEntity ccdCaseTypeEntity1 = createCaseTypeEntity(CASE_TYPE_REFERENCE_1, ccdACLEntries);
        CaseTypeEntity ccdCaseTypeEntity2 = createCaseTypeEntity(CASE_TYPE_REFERENCE_2, ccdACLEntries);
        CaseTypeAmInfo caseTypeAmInfo1 = createCaseTypeAmInfo(CASE_TYPE_REFERENCE_1, amACLEntries);
        CaseTypeAmInfo caseTypeAmInfo2 = createCaseTypeAmInfo(CASE_TYPE_REFERENCE_2, amACLEntries);

        Mockito.when(amPersistenceSwitch.getReadDataSourceFor(CASE_TYPE_REFERENCE_1))
            .thenReturn(AmPersistenceReadSource.FROM_AM);
        Mockito.when(amPersistenceSwitch.getReadDataSourceFor(CASE_TYPE_REFERENCE_2))
            .thenReturn(AmPersistenceReadSource.FROM_CCD);
        Mockito.when(ccdCaseTypeRepository.findByJurisdictionId(anyString()))
            .thenReturn(ImmutableList.of(ccdCaseTypeEntity1, ccdCaseTypeEntity2));
        Mockito.when(amCaseTypeACLRepository.getAmInfoFor(anyList()))
            .thenReturn(ImmutableList.of(caseTypeAmInfo1, caseTypeAmInfo2));

        List<CaseTypeEntity> result = switchableCaseTypeRepository.findByJurisdictionId(JURISDICTION);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getCaseTypeACLEntities().get(0).getCreate()).isEqualTo(false);
        assertThat(result.get(0).getCaseTypeACLEntities().get(0).getRead()).isEqualTo(false);
        assertThat(result.get(0).getCaseTypeACLEntities().get(0).getUpdate()).isEqualTo(false);
        assertThat(result.get(0).getCaseTypeACLEntities().get(0).getDelete()).isEqualTo(false);
        assertThat(result.get(0).getCaseTypeACLEntities().get(1).getCreate()).isEqualTo(true);
        assertThat(result.get(0).getCaseTypeACLEntities().get(1).getRead()).isEqualTo(true);
        assertThat(result.get(0).getCaseTypeACLEntities().get(1).getUpdate()).isEqualTo(true);
        assertThat(result.get(0).getCaseTypeACLEntities().get(1).getDelete()).isEqualTo(true);

        assertThat(result.get(1).getCaseTypeACLEntities().get(0).getCreate()).isEqualTo(true);
        assertThat(result.get(1).getCaseTypeACLEntities().get(0).getRead()).isEqualTo(true);
        assertThat(result.get(1).getCaseTypeACLEntities().get(0).getUpdate()).isEqualTo(true);
        assertThat(result.get(1).getCaseTypeACLEntities().get(0).getDelete()).isEqualTo(true);
        assertThat(result.get(1).getCaseTypeACLEntities().get(1).getCreate()).isEqualTo(false);
        assertThat(result.get(1).getCaseTypeACLEntities().get(1).getRead()).isEqualTo(false);
        assertThat(result.get(1).getCaseTypeACLEntities().get(1).getUpdate()).isEqualTo(false);
        assertThat(result.get(1).getCaseTypeACLEntities().get(1).getDelete()).isEqualTo(false);
    }

    @Getter
    @AllArgsConstructor
    private static class ACLEntry {
        private String role;
        private Boolean create;
        private Boolean read;
        private Boolean update;
        private Boolean delete;
    }
}
