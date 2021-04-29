package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.RestEndPointExceptionHandler;
import uk.gov.hmcts.ccd.definition.store.rest.model.ImportAudit;
import uk.gov.hmcts.ccd.definition.store.rest.service.AzureImportAuditsClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static java.util.Collections.emptyList;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ImportAuditControllerTest {

    private static final String URL_IMPORT_AUDITS = "/api/import-audits";

    private MockMvc mockMvc;

    private ImportAuditController subject;

    @Mock
    private AzureImportAuditsClient azureImportAuditsClient;

    @BeforeEach
    void createSubject() throws Exception {
        initMocks(this);
        subject = new ImportAuditController(azureImportAuditsClient);
        mockMvc = standaloneSetup(subject)
            .setControllerAdvice(new RestEndPointExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("Should fetch import audits")
    void shouldFetchImportAudits() throws Exception {
        given(azureImportAuditsClient.fetchLatestImportAudits()).willReturn(asList(buildImportAudit()));
        final MvcResult
            mvcResult =
            mockMvc.perform(get(URL_IMPORT_AUDITS))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString(),
            is("[{\"filename\":\"filename\",\"uri\":\"https://ffs.blob.core.windows"
                + ".net/bugbear/20201015093302_definition_upload\",\"order\":1582934400000,"
                + "\"date_imported\":\"2020-02-29\",\"who_imported\":\"Who else\","
                + "\"case_type\":\"Some case type\"}]"));
        verify(azureImportAuditsClient).fetchLatestImportAudits();
    }

    @Test
    @DisplayName("Should handle empty collection")
    void shouldHandleEmptyCollection() throws Exception {
        given(azureImportAuditsClient.fetchLatestImportAudits()).willReturn(emptyList());
        final MvcResult
            mvcResult =
            mockMvc.perform(get(URL_IMPORT_AUDITS))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString(), is("[]"));
        verify(azureImportAuditsClient).fetchLatestImportAudits();
    }

    @Test
    @DisplayName("Should handle null AzureImportAuditsClient when Azure is not configured")
    void shouldHandleNullAzureImportAuditsClient() throws Exception {
        subject = new ImportAuditController(null);
        final MockMvc
            mockMvcNullAzureImportAuditsClient =
            standaloneSetup(subject).setControllerAdvice(new RestEndPointExceptionHandler()).build();
        final MvcResult
            mvcResult =
            mockMvcNullAzureImportAuditsClient.perform(get(URL_IMPORT_AUDITS)).andExpect(status().isOk()).andReturn();
        assertThat(mvcResult.getResponse().getContentAsString(), is("[]"));
        verifyZeroInteractions(azureImportAuditsClient);
    }

    private ImportAudit buildImportAudit() throws URISyntaxException {
        final ImportAudit audit = new ImportAudit();
        final LocalDate created = LocalDate.of(2020, 2, 29);
        audit.setDateImported(created);
        audit.setFilename("filename");
        audit.setOrder(Date.from(created.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        audit.setUri(new URI("https://ffs.blob.core.windows.net/bugbear/20201015093302_definition_upload"));
        audit.setWhoImported("Who else");
        audit.setCaseType("Some case type");
        return audit;
    }
}
