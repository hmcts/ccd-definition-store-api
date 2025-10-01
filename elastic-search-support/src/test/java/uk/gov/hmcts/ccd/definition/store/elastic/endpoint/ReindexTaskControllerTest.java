package uk.gov.hmcts.ccd.definition.store.elastic.endpoint;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticsearchIntegrationTestApplication;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexDBService;
import uk.gov.hmcts.ccd.definition.store.repository.model.ReindexTask;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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

    @MockBean
    private ReindexDBService reindexDBService;

    private static final String CASE_TYPE_1 = "CT1";
    private static final String CASE_TYPE_2 = "CT2";

    @Test
    void shouldGetAllReindexedTasks() throws Exception {
        ReindexTask task1 = new ReindexTask();
        task1.setCaseType(CASE_TYPE_1);
        ReindexTask task2 = new ReindexTask();
        task2.setCaseType(CASE_TYPE_2);

        when(reindexDBService.getTasksByCaseType(null)).thenReturn(List.of(task1, task2));

        mockMvc.perform(get(ReindexTaskController.REINDEX_TASKS_URI)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].caseType", is(CASE_TYPE_1)))
            .andExpect(jsonPath("$[1].caseType", is(CASE_TYPE_2)));

        verify(reindexDBService).getTasksByCaseType(null);
    }

    @Test
    void shouldFilterReindexedTasksByCaseType() throws Exception {
        ReindexTask task = new ReindexTask();
        task.setCaseType(CASE_TYPE_1);
        ReindexTask task2 = new ReindexTask();
        task2.setCaseType(CASE_TYPE_2);

        when(reindexDBService.getTasksByCaseType(CASE_TYPE_1)).thenReturn(List.of(task));

        mockMvc.perform(get(ReindexTaskController.REINDEX_TASKS_URI)
                .param("caseType", CASE_TYPE_1)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].caseType", is(CASE_TYPE_1)));

        verify(reindexDBService).getTasksByCaseType(CASE_TYPE_1);
    }
}
