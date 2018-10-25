package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.CREATE;

class DraftDefinitionControllerTest {

    private static final String URL_API_DRAFT = "/api/draft";
    private static final MediaType CONTENT_TYPE = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        Charset.forName(StandardCharsets.UTF_8.name())
    );
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private MockMvc mockMvc;

    @Mock
    private DefinitionService definitionService;

    private Definition definition;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        final DraftDefinitionController controller = new DraftDefinitionController(definitionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new RestEndPointExceptionHandler())
            .build();
        definition = createDefinition();
    }

    @DisplayName("Should create a draft Definition")
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
