package uk.gov.hmcts.ccd.definition.store.domain.service.nocconfig;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.NoCConfigRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.NoCConfigEntity;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class NoCConfigServiceImplTest {

    @Mock
    private NoCConfigRepository nocConfigRepository;

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    private NoCConfigServiceImpl classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new NoCConfigServiceImpl(nocConfigRepository, dtoMapper);
    }

    @Test
    @DisplayName("Should get Noc Config entities for the passed case type references")
    void shouldGetNoCConfigsForValidJurisdictions() {
        List<NoCConfigEntity> noCConfigEntities = Lists.newArrayList();
        noCConfigEntities.add(createNoCConfigEntity(true, true));
        noCConfigEntities.add(createNoCConfigEntity(false, false));
        doReturn(noCConfigEntities).when(nocConfigRepository).findAllByCaseTypeReferences(anyList());
        List<String> references = Lists.newArrayList("Test", "Divorce");
        List<NoCConfigEntity> configEntities = classUnderTest.getAll(references);
        Assert.assertEquals(2, configEntities.size());
    }

    @Test
    @DisplayName("Should get empty noc config list for the passed empty case references")
    void shouldGetEmptyNoCConfigsListForEmptyJurisdictions() {
        List<NoCConfigEntity> noCConfigEntities = Lists.newArrayList();
        doReturn(noCConfigEntities).when(nocConfigRepository).findAllByCaseTypeReferences(anyList());
        List<NoCConfigEntity> nocConfigsReturned = classUnderTest.getAll(Lists.newArrayList());
        Assert.assertEquals(0, nocConfigsReturned.size());
    }

    @Test
    @DisplayName("Should get empty noc config list for the passed empty case references")
    void shouldSaveNoCConfigEntity() {
        NoCConfigEntity noCConfigEntity = mock(NoCConfigEntity.class);
        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        doReturn(caseTypeEntity).when(noCConfigEntity).getCaseType();
        doReturn("TEST").when(caseTypeEntity).getReference();
        doReturn(null).when(nocConfigRepository).findByCaseTypeReference(anyString());
        classUnderTest.save(noCConfigEntity);
        verify(nocConfigRepository, times(1)).save(eq(noCConfigEntity));
    }

    @Test
    @DisplayName("Should get empty noc config list for the passed empty case references")
    void shouldSaveCopyNoCConfigEntity() {
        NoCConfigEntity configEntity = mock(NoCConfigEntity.class);
        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        doReturn(caseTypeEntity).when(configEntity).getCaseType();
        doReturn("TEST").when(caseTypeEntity).getReference();
        NoCConfigEntity configEntityDB = mock(NoCConfigEntity.class);
        doReturn(configEntityDB).when(nocConfigRepository).findByCaseTypeReference(anyString());
        classUnderTest.save(configEntity);
        verify(configEntityDB, times(1)).copy(eq(configEntity));
        verify(nocConfigRepository, times(1)).save(eq(configEntityDB));
    }

    @Test
    @DisplayName("Should attempt to delete noc config entries when case type reference is provided")
    void shouldAttemptToDeleteNoCConfigEntriesWhenCaseTypeReferenceIsProvided() {
        String reference = "TEST";

        classUnderTest.deleteCaseTypeNocConfig(reference);

        verify(nocConfigRepository, times(1)).deleteByCaseTypeReference(eq(reference));
    }

    @Test
    @DisplayName("Should not attempt to delete noc config entries when case type reference is not provided")
    void shouldNotAttemptToDeleteNoCCofigsWhenNoCaseReferenceIsProvided() {
        classUnderTest.deleteCaseTypeNocConfig(null);
        classUnderTest.deleteCaseTypeNocConfig("");

        verifyNoMoreInteractions(nocConfigRepository);
    }

    private NoCConfigEntity createNoCConfigEntity(boolean reasonRequired,
                                                  boolean nocActionInterpretationRequired) {
        NoCConfigEntity entity = new NoCConfigEntity();
        entity.setCaseType(null);
        entity.setReasonsRequired(reasonRequired);
        entity.setNocActionInterpretationRequired(nocActionInterpretationRequired);
        return entity;
    }
}
