package uk.gov.hmcts.ccd.definition.store.elastic.endpoint;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticsearchIntegrationTestApplication;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexService;
import uk.gov.hmcts.ccd.definition.store.repository.model.ReindexTask;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {ElasticsearchIntegrationTestApplication.class})
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
class ReindexTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReindexService reindexService;

    private static final String CASE_TYPE_1 = "CT1";
    private static final String CASE_TYPE_2 = "CT2";

    @Test
    void shouldGetAllReindexedTasks() throws Exception {
        ReindexTask task1 = new ReindexTask();
        task1.setCaseType(CASE_TYPE_1);
        ReindexTask task2 = new ReindexTask();
        task2.setCaseType(CASE_TYPE_2);

        when(reindexService.getTasksByCaseType(isNull(), any()))
            .thenReturn(new PageImpl<>(List.of(task1, task2), PageRequest.of(0, 25), 2));

        mockMvc.perform(get(ReindexTaskController.REINDEX_TASKS_URI)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].caseType", is(CASE_TYPE_1)))
            .andExpect(jsonPath("$.content[1].caseType", is(CASE_TYPE_2)));

        verify(reindexService).getTasksByCaseType(isNull(), any());
    }

    @Test
    void shouldFilterReindexedTasksByCaseType() throws Exception {
        ReindexTask task = new ReindexTask();
        task.setCaseType(CASE_TYPE_1);
        ReindexTask task2 = new ReindexTask();
        task2.setCaseType(CASE_TYPE_2);

        when(reindexService.getTasksByCaseType(eq(CASE_TYPE_1), any()))
            .thenReturn(new PageImpl<>(List.of(task), PageRequest.of(0, 25), 1));

        mockMvc.perform(get(ReindexTaskController.REINDEX_TASKS_URI)
                .param("caseType", CASE_TYPE_1)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].caseType", is(CASE_TYPE_1)));

        verify(reindexService).getTasksByCaseType(eq(CASE_TYPE_1), any());
    }

    @Test
    void shouldReturnPaginatedReindexedTasksWhenPageAndSizeAreProvided() throws Exception {
        ReindexTask task = new ReindexTask();
        task.setCaseType(CASE_TYPE_1);

        when(reindexService.getTasksByCaseType(eq(CASE_TYPE_1), any()))
            .thenReturn(new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(ReindexTaskController.REINDEX_TASKS_URI)
                .param("caseType", CASE_TYPE_1)
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].caseType", is(CASE_TYPE_1)))
            .andExpect(jsonPath("$.totalElements", is(1)));

        verify(reindexService).getTasksByCaseType(eq(CASE_TYPE_1), any());
    }
}
