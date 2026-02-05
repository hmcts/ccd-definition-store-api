package uk.gov.hmcts.ccd.definition.store.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Executor;

@Slf4j
public class ReindexHelper {

    private final RestClient restClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private Executor executor = asyncExecutor();
    private static final String FAILURES = "failures";

    public ReindexHelper(RestClient restClient) {
        this(restClient, null);
    }

    public ReindexHelper(RestClient restClient, Executor executor) {
        this.restClient = restClient;
        this.executor = (executor != null) ? executor : asyncExecutor();
    }

    public String reindexIndex(String sourceIndex,
                               String destIndex,
                               long pollIntervalMs,
                               ReindexListener listener) throws IOException {

        String taskId = startReindexTask(sourceIndex, destIndex);
        log.info("Reindex task started: {}", taskId);

        executor.execute(() -> monitorReindexTask(taskId, destIndex, pollIntervalMs, listener));

        return taskId;
    }

    private String startReindexTask(String sourceIndex, String destIndex) throws IOException {
        String jsonBody = String.format("""
            {
              "source": { "index": "%s" },
              "dest": { "index": "%s" }
            }
            """, sourceIndex, destIndex);

        Request request = buildReindexRequest(jsonBody);
        Response response = restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonNode root = mapper.readTree(responseBody);
        JsonNode taskNode = root.path("task");
        return taskNode.isTextual() ? taskNode.asText() : null;
    }

    private Request buildReindexRequest(String jsonBody) {
        Request request = new Request("POST", "/_reindex");
        request.addParameter("wait_for_completion", "false");
        request.addParameter("refresh", "false");
        request.addParameter("requests_per_second", "-1");
        request.addParameter("timeout", "2h");
        request.addParameter("slices", "auto");
        request.setJsonEntity(jsonBody);
        return request;
    }

    private void monitorReindexTask(String taskId,
                                    String destIndex,
                                    long pollIntervalMs,
                                    ReindexListener listener) {

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Optional<JsonNode> taskJsonNode = fetchTaskResponse(taskId);
                if (shouldExitPolling(taskJsonNode, taskId, listener, destIndex)) {
                    break;
                }
                Thread.sleep(pollIntervalMs);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            listener.onFailure(new RuntimeException("Reindex polling interrupted", ie));
        } catch (IOException ioe) {
            listener.onFailure(new RuntimeException("Reindex process failed: " + ioe.getLocalizedMessage(), ioe));
        }
    }

    private Optional<JsonNode> fetchTaskResponse(String taskId) throws IOException {
        String path = "/_tasks/" + taskId;
        Request request = new Request("GET", path);
        try {
            Response response = restClient.performRequest(request);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JsonNode root = mapper.readTree(responseBody);
            return Optional.ofNullable(root);
        } catch (org.elasticsearch.client.ResponseException re) {
            int status = re.getResponse().getStatusLine().getStatusCode();
            if (status == 404) {
                // not found -> will retry
                return Optional.empty();
            }
            throw re;
        }
    }

    private boolean shouldExitPolling(Optional<JsonNode> taskResponse,
                                      String taskId,
                                      ReindexListener listener,
                                      String destIndex) throws IOException {

        if (taskResponse.isEmpty()) {
            log.info("Task not found: {}. Retrying...", taskId);
            return false;
        }

        JsonNode taskJson = taskResponse.get();
        boolean completed = taskJson.path("completed").asBoolean(false);

        if (!completed) {
            return false;
        }
        return handleCompletion(taskResponse.get(), listener, destIndex);

    }

    private boolean handleCompletion(JsonNode taskJson,
                                     ReindexListener listener,
                                     String destIndex) throws IOException {
        JsonNode responseNode = taskJson.path("response");
        JsonNode errorNode = taskJson.path("error");

        if (!errorNode.isMissingNode() && !errorNode.isNull()) {
            listener.onFailure(new RuntimeException("Task failed with error: " + errorNode));
            return true;
        }

        if (!responseNode.isMissingNode() && !responseNode.isNull()) {
            JsonNode failuresNode = responseNode.path(FAILURES);
            if (failuresNode.isArray() && !failuresNode.isEmpty()) {
                listener.onFailure(new RuntimeException("Reindex process failed: " + failuresNode));
                return true;
            }
            logProgress(destIndex, responseNode);
            listener.onSuccess();
            return true;
        }
        // fallback: if no response and no error, treat as unknown outcome
        listener.onFailure(new RuntimeException("Reindex process completed with unknown status: " + taskJson));
        return true;
    }

    private void logProgress(String destIndex, JsonNode statusJson) {
        int total = statusJson.path("total").asInt(0);
        int created = statusJson.path("created").asInt(0);
        int updated = statusJson.path("updated").asInt(0);
        int batches = statusJson.path("batches").asInt(0);
        log.info("Progress for index {}: total={}, created={}, updated={}, batches={}",
            destIndex, total, created, updated, batches);
    }

    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(50);
        threadPoolTaskExecutor.setThreadNamePrefix("reindex-exec-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
