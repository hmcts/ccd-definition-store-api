package uk.gov.hmcts.ccd.definition.store.elastic;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionService;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.BannerRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeLiteRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.EventRepository;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.GenericLayoutRepository;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionRepository;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionUiConfigRepository;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@Profile("test")
public class TestReindexConfig {

    @Bean
    @Primary
    public ReindexEntityService mockReindexEntityService() {
        ReindexEntityService mockService = mock(ReindexEntityService.class);

        ReindexEntity mockEntity = new ReindexEntity();
        mockEntity.setId(1);
        mockEntity.setReindex(true);
        mockEntity.setDeleteOldIndex(true);
        mockEntity.setCaseType("TestCaseType");
        mockEntity.setJurisdiction("TestJur");
        mockEntity.setIndexName("test-index-000001");
        mockEntity.setStartTime(LocalDateTime.now());
        mockEntity.setStatus("STARTED");

        when(mockService.persistInitialReindexMetadata(any(), any(), any(), any()))
            .thenReturn(mockEntity);

        doNothing().when(mockService).persistSuccess(any(), any());
        doNothing().when(mockService).persistFailure(any(), any());

        return mockService;
    }

    @Bean
    @Primary
    public ReindexRepository mockReindexRepository() {
        ReindexRepository mockRepo = mock(ReindexRepository.class);
        when(mockRepo.findByIndexName(any())).thenReturn(Optional.empty());
        when(mockRepo.saveAndFlush(any())).thenReturn(new ReindexEntity());
        when(mockRepo.save(any())).thenReturn(new ReindexEntity());
        doNothing().when(mockRepo).deleteAll();
        when(mockRepo.findAll()).thenReturn(Collections.emptyList());
        return mockRepo;
    }

    @Bean
    @Primary
    public SynchronousElasticDefinitionImportListener mockSynchronousElasticDefinitionImportListener() {
        SynchronousElasticDefinitionImportListener mockListener = mock(SynchronousElasticDefinitionImportListener.class);

        doNothing().when(mockListener).onDefinitionImported(any(DefinitionImportedEvent.class));

        return mockListener;
    }

    @Bean
    @Primary
    public FieldTypeRepository mockFieldTypeRepository() {
        return mock(FieldTypeRepository.class);
    }

    @Bean
    @Primary
    public CaseFieldRepository mockCaseFieldRepository() {
        return mock(CaseFieldRepository.class);
    }

    @Bean
    @Primary
    public JurisdictionRepository mockJurisdictionRepository() {
        return mock(JurisdictionRepository.class);
    }

    @Bean
    @Primary
    public JurisdictionUiConfigRepository mockJurisdictionUiConfigRepository() {
        return mock(JurisdictionUiConfigRepository.class);
    }

    @Bean
    @Primary
    public BannerRepository mockBannerRepository() {
        return mock(BannerRepository.class);
    }

    @Bean
    @Primary
    public JurisdictionService mockJurisdictionService() {
        return mock(JurisdictionService.class);
    }

    @Bean
    @Primary
    public GenericLayoutRepository mockLayoutService() {
        return mock(GenericLayoutRepository.class);
    }

    @Bean
    @Primary
    public DisplayGroupRepository mockDisplayGroupRepository() {
        return mock(DisplayGroupRepository.class);
    }

    @Bean
    @Primary
    public EventRepository mockEventRepository() {
        return mock(EventRepository.class);
    }

    @Bean
    @Primary
    public IdamClient mockIdamClient() {
        return mock(IdamClient.class);
    }

    @Bean
    @Primary
    public IdamApi mockIdamApi() {
        return mock(IdamApi.class);
    }
}
