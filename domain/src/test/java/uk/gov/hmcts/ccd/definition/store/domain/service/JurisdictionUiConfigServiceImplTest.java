package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionUiConfigRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionUiConfigEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.JurisdictionUiConfig;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

class JurisdictionUiConfigServiceImplTest {

    private final String reference = "Reference 1";

    @Mock
    EntityToResponseDTOMapper dtoMapper;

    @Mock
    private JurisdictionUiConfigRepository jurisdictionUiConfigRepository;

    @Captor
    private ArgumentCaptor captor;

    private JurisdictionUiConfigServiceImpl classUnderTest;

    private JurisdictionUiConfigEntity jurisdictionUiConfigEntity1 = new JurisdictionUiConfigEntity();
    private JurisdictionUiConfigEntity jurisdictionUiConfigEntity2 = new JurisdictionUiConfigEntity();
    private JurisdictionUiConfigEntity jurisdictionUiConfigEntity3 = new JurisdictionUiConfigEntity();
    private JurisdictionUiConfig jurisdictionUiConfig1 = new JurisdictionUiConfig();
    private JurisdictionUiConfig jurisdictionUiConfig2 = new JurisdictionUiConfig();
    private JurisdictionUiConfig jurisdictionUiConfig3 = new JurisdictionUiConfig();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new JurisdictionUiConfigServiceImpl(jurisdictionUiConfigRepository, dtoMapper);
        jurisdictionUiConfig1.setId(reference);
        jurisdictionUiConfigEntity1.setShuttered(false);
        jurisdictionUiConfigEntity2.setShuttered(true);
        doReturn(Arrays.asList(jurisdictionUiConfigEntity1, jurisdictionUiConfigEntity2, jurisdictionUiConfigEntity3))
            .when(jurisdictionUiConfigRepository)
            .findAllByReference(any());
        doReturn(jurisdictionUiConfig1).when(dtoMapper).map(jurisdictionUiConfigEntity1);
        doReturn(jurisdictionUiConfig2).when(dtoMapper).map(jurisdictionUiConfigEntity2);
        doReturn(jurisdictionUiConfig3).when(dtoMapper).map(jurisdictionUiConfigEntity3);
    }

    @DisplayName("should return all Jurisdiction UI Configs")
    @Test
    void getAll() {
        final List<JurisdictionUiConfig> configs = classUnderTest.getAll(Arrays.asList("Reference"));

        assertAll(
            () -> assertThat(configs.size(), is(3)),
            () -> assertThat(configs.get(0).getId(), is(reference))
        );
    }

    @DisplayName("should save a new Jurisdiction UI Config")
    @Test
    void saveNewConfig() {
        JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
        jurisdictionEntity.setReference(reference);
        jurisdictionUiConfigEntity1.setJurisdiction(jurisdictionEntity);
        doReturn(null).when(jurisdictionUiConfigRepository).findByJurisdictionId(reference);

        classUnderTest.save(jurisdictionUiConfigEntity1);

        Mockito.verify(jurisdictionUiConfigRepository).save((JurisdictionUiConfigEntity) captor.capture());
        JurisdictionUiConfigEntity savedEntity = (JurisdictionUiConfigEntity) captor.getValue();

        assertAll(
            () -> assertThat(savedEntity, is(jurisdictionUiConfigEntity1)),
            () -> assertFalse(savedEntity.getShuttered())
        );
    }

    @DisplayName("should save an updated Jurisdiction UI Config")
    @Test
    void saveUpdatedConfig() {
        JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
        jurisdictionEntity.setReference(reference);
        jurisdictionUiConfigEntity1.setJurisdiction(jurisdictionEntity);
        jurisdictionUiConfigEntity2.setJurisdiction(jurisdictionEntity);
        doReturn(jurisdictionUiConfigEntity1).when(jurisdictionUiConfigRepository).findByJurisdictionId(reference);

        classUnderTest.save(jurisdictionUiConfigEntity2);

        Mockito.verify(jurisdictionUiConfigRepository).save((JurisdictionUiConfigEntity) captor.capture());
        JurisdictionUiConfigEntity savedEntity = (JurisdictionUiConfigEntity) captor.getValue();

        assertAll(
            () -> assertThat(savedEntity, is(jurisdictionUiConfigEntity1)),
            () -> assertTrue(savedEntity.getShuttered())
        );
    }
}
