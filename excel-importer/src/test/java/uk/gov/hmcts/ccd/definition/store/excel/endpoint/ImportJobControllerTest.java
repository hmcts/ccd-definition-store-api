package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.domain.service.ImportJobService;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImportJobControllerTest {

    @Mock
    private ImportJobService importJobService;

    @InjectMocks
    private ImportJobController controller;

    private MockMvc mockMvc;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);

        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @DisplayName("Valid id → 200 with response body, expireStaleJobs called before findById")
    @Test
    void validId_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        ImportJobEntity entity = buildEntity(id, "uid-submitter");
        when(importJobService.findById(id)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/import-jobs/" + id).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.submittedBy").value("uid-submitter"));

        var inOrder = org.mockito.Mockito.inOrder(importJobService);
        inOrder.verify(importJobService).expireStaleJobs();
        inOrder.verify(importJobService).findById(id);
    }

    @DisplayName("Valid id, job not found → 404")
    @Test
    void validId_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(importJobService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/import-jobs/" + id).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @DisplayName("Malformed UUID → 400, no service call made")
    @Test
    void malformedUuid_returns400() throws Exception {
        mockMvc.perform(get("/import-jobs/not-a-uuid").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(importJobService, never()).findById(any());
        verify(importJobService, never()).expireStaleJobs();
    }

    @DisplayName("Response correctly deserializes newline-joined warnings into a list")
    @Test
    void warningsDeserializes_intoList() throws Exception {
        UUID id = UUID.randomUUID();
        ImportJobEntity entity = buildEntity(id, "uid-submitter");
        entity.setWarnings("first\nsecond\nthird");
        when(importJobService.findById(id)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/import-jobs/" + id).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.warnings[0]").value("first"))
            .andExpect(jsonPath("$.warnings[1]").value("second"))
            .andExpect(jsonPath("$.warnings[2]").value("third"));
    }

    @DisplayName("Response with null warnings field → empty list, not null")
    @Test
    void nullWarnings_returnsEmptyList() throws Exception {
        UUID id = UUID.randomUUID();
        ImportJobEntity entity = buildEntity(id, "uid-submitter");
        entity.setWarnings(null);
        when(importJobService.findById(id)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/import-jobs/" + id).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.warnings").isArray())
            .andExpect(jsonPath("$.warnings").isEmpty());
    }

    private ImportJobEntity buildEntity(UUID id, String submittedBy) {
        ImportJobEntity entity = new ImportJobEntity();
        entity.setId(id);
        entity.setStatus(ImportJobStatus.COMPLETED);
        entity.setSubmittedBy(submittedBy);
        entity.setSubmittedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        entity.setStartedAt(LocalDateTime.of(2026, 1, 1, 10, 1));
        entity.setCompletedAt(LocalDateTime.of(2026, 1, 1, 10, 5));
        entity.setWarnings(null);
        entity.setReindexTaskId(null);
        return entity;
    }
}
