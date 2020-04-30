package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriTemplate;
import uk.gov.hmcts.ccd.definition.store.domain.service.DefinitionService;
import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.model.Definition;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.RestEndPointExceptionHandler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.CREATE;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.UPDATE;

class DraftDefinitionControllerTest {

    private static final String URL_API_DRAFT = "/api/draft";
    private static final String URL_SAVE_API_DRAFT = "/api/draft/save";
    private static final MediaType CONTENT_TYPE = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        Charset.forName(StandardCharsets.UTF_8.name())
    );
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String URL_API_JURISDICTIONS = "/api/drafts";
    private static final UriTemplate URI_TEMPLATE_GET_DRAFTS =
        new UriTemplate(URL_API_JURISDICTIONS + "?jurisdiction={jurisdiction}");
    private static final UriTemplate URI_TEMPLATE_ONE_DRAFT =
        new UriTemplate(URL_API_DRAFT + "?jurisdiction={jurisdiction}&version={version}");
    private MockMvc mockMvc;

    @Mock
    private DefinitionService definitionService;

    private Definition definition;

    private Map<String, Object> uriVariables;

    private Definition def2;

    private Definition def3;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        final DraftDefinitionController controller = new DraftDefinitionController(definitionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new RestEndPointExceptionHandler())
            .build();
        definition = createDefinition();
        def2 = createDefinition();
        def3 = createDefinition();
        uriVariables = new HashMap<>();

    }

    @DisplayName("Should return 201 when creating a draft Definition")
    @Test
    void shouldCreateDraftDefinition() throws Exception {
        when(definitionService.createDraftDefinition(any(Definition.class)))
            .thenReturn(new ServiceResponse<>(definition, CREATE));

        mockMvc.perform(
            post(URL_API_DRAFT)
                .contentType(CONTENT_TYPE)
                .content(MAPPER.writeValueAsBytes(definition)))
            .andExpect(status().isCreated());
        verify(definitionService).createDraftDefinition(any(Definition.class));
    }

    @DisplayName("Should return 200 when saving a draft Definition")
    @Test
    void shouldSaveDraftDefinition() throws Exception {
        when(definitionService.saveDraftDefinition(any(Definition.class)))
            .thenReturn(new ServiceResponse<>(definition, UPDATE));

        mockMvc.perform(put(URL_SAVE_API_DRAFT)
                            .contentType(CONTENT_TYPE)
                            .content(MAPPER.writeValueAsBytes(definition)))
               .andExpect(status().isOk());
        ArgumentCaptor<Definition> argument = ArgumentCaptor.forClass(Definition.class);
        verify(definitionService).saveDraftDefinition(argument.capture());

        assertThat(argument.getValue(), not(definition));
        assertThat(argument.getValue().getJurisdiction(), not(definition.getJurisdiction()));

        assertThat(argument.getValue().getJurisdiction().getId(), is(definition.getJurisdiction().getId()));
        assertThat(argument.getValue().getData(), is(definition.getData()));
    }

    @DisplayName("Should return 204 when deleting a draft Definition")
    @Test
    void shouldDeleteDraftDefinition() throws Exception {
        mockMvc.perform(delete(URL_API_DRAFT + "/Test/10"))
               .andExpect(status().isNoContent());
        verify(definitionService).deleteDraftDefinition("Test", 10);
    }

    @DisplayName("should return 200 when finding definitions")
    @Test
    void shouldReturn200WhenFindDefinitions() throws Exception {
        when(definitionService.findByJurisdictionId("jurisdiction")).thenReturn(asList(def3, def2));
        uriVariables.put("jurisdiction", "jurisdiction");
        final MvcResult
            mvcResult =
            mockMvc.perform(get(URI_TEMPLATE_GET_DRAFTS.expand(uriVariables)))
                   .andExpect(status().isOk())
                   .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString(),
                   is("[{\"jurisdiction\":{\"id\":\"TEST\",\"name\":null,\"description\":null,"
                       + "\"live_from\":null,\"live_until\":null,\"case_types\":[]},\"description\":\"Description\","
                       + "\"version\":null,\"data\":{\"Data\":{\"Field1\":\"Value1\",\"Field2\":[]}},\"author\":\"ccd2@hmcts\","
                       + "\"status\":null,\"case_types\":\"CaseType1,CaseType2\",\"created_at\":null,\"last_modified\":null,"
                       + "\"deleted\":false},{\"jurisdiction\":{\"id\":\"TEST\",\"name\":null,\"description\":null,"
                       + "\"live_from\":null,\"live_until\":null,\"case_types\":[]},\"description\":\"Description\","
                       + "\"version\":null,\"data\":{\"Data\":{\"Field1\":\"Value1\",\"Field2\":[]}},\"author\":\"ccd2@hmcts\","
                       + "\"status\":null,\"case_types\":\"CaseType1,CaseType2\",\"created_at\":null,\"last_modified\":null,\"deleted\":false}]"
                   ));
    }

    @DisplayName("should return 200 when finding a draft by jurisdiction and version")
    @Test
    void shouldReturn200WhenFindByJurisdictionAndVersion() throws Exception {
        when(definitionService.findByJurisdictionIdAndVersion("jurisdiction", -1)).thenReturn(def2);
        uriVariables.put("jurisdiction", "jurisdiction");
        uriVariables.put("version", -1);
        final MvcResult
            mvcResult =
            mockMvc.perform(get(URI_TEMPLATE_ONE_DRAFT.expand(uriVariables)))
                   .andExpect(status().isOk())
                   .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString(),
                   is("{\"jurisdiction\":{\"id\":\"TEST\",\"name\":null,\"description\":null,\"live_from\":null,\"live_until\":null,"
                       + "\"case_types\":[]},\"description\":\"Description\",\"version\":null,"
                       + "\"data\":{\"Data\":{\"Field1\":\"Value1\",\"Field2\":[]}},\"author\":\"ccd2@hmcts\",\"status\":null,"
                       + "\"case_types\":\"CaseType1,CaseType2\",\"created_at\":null,\"last_modified\":null,\"deleted\":false}"
                   ));
    }

    private Definition createDefinition() throws IOException {
        final Definition definition = new Definition();
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("TEST");
        definition.setJurisdiction(jurisdiction);
        definition.setCaseTypes("CaseType1,CaseType2");
        definition.setDescription("Description");
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree("{\"Field1\": \"Value1\", \"Field2\": []}");
        Map<String, JsonNode> data = new HashMap<>();
        data.put("Data", node);
        definition.setData(data);
        definition.setAuthor("ccd2@hmcts");
        definition.setDeleted(false);
        return definition;
    }

}
