package uk.gov.hmcts.ccd.definition.store.elastic;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionService;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.BannerRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldRepository;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    public ReindexRepository mockReindexRepository() {
        ReindexRepository mockRepo = mock(ReindexRepository.class);
        when(mockRepo.findByIndexName(any())).thenReturn(Optional.empty());
        when(mockRepo.saveAndFlush(any())).thenReturn(new ReindexEntity());
        when(mockRepo.save(any())).thenReturn(new ReindexEntity());
        doNothing().when(mockRepo).deleteAll();
        when(mockRepo.findAll()).thenReturn(Collections.emptyList());
        return mockRepo;
    }

    //CCD-3509 CVE-2021-22044 required to fix null pointers in integration tests,
    //conflict in Springfox after Springboot 2.6.10
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier,
                                                                         ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier,
                                                                         EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties,
                                                                         WebEndpointProperties webEndpointProperties, Environment environment) {

        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment,
            basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes,
            corsProperties.toCorsConfiguration(),
            new EndpointLinksResolver(allEndpoints, basePath),
            shouldRegisterLinksMapping);
    }

    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment,
                                               String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath)
                                                                    || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
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
