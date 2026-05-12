package uk.gov.hmcts.ccd.definition.store.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Executor;

@Slf4j
public class ReindexHelper {

    private final RestClient restClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CcdElasticSearchProperties config;
    private Executor executor = asyncExecutor();
    private static final String FAILURES = "failures";
    private static final int MAX_TASK_NOT_FOUND_RETRIES = 10;
    private static final String COMPLETED_TASK = "isn't running and hasn't stored its results";

    public ReindexHelper(RestClient restClient, CcdElasticSearchProperties config) {
        this(restClient, config,null);
    }

    public ReindexHelper(RestClient restClient, CcdElasticSearchProperties config, Executor executor) {
        this.restClient = restClient;
        this.config = config;
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
              "source": {
                "index": "%s",
                "size": %s
              },
              "dest": {
                "index": "%s"
              }
            }
            """, sourceIndex, config.getBatchSize(), destIndex);

        Request request = buildReindexRequest(jsonBody);
        Response response = restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        log.info("Reindex submit response: {}", responseBody);
        JsonNode root = mapper.readTree(responseBody);
        JsonNode taskNode = root.path("task");
        if (!taskNode.isTextual() || taskNode.asText().isBlank()) {
            throw new IOException("Reindex response did not contain a valid task ID. Response: " + responseBody);
        }
        return taskNode.asText();
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
        int taskNotFoundCount = 0;

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Optional<JsonNode> taskJsonNode = fetchTaskResponse(taskId);

                if (taskJsonNode.isEmpty()) {
                    taskNotFoundCount++;
                    if (taskNotFoundCount >= MAX_TASK_NOT_FOUND_RETRIES) {
                        log.error("Task {} not found after {} attempts — giving up", taskId, taskNotFoundCount);
                        safeOnFailure(listener, new RuntimeException(
                            "Reindex task " + taskId + " not found after " + taskNotFoundCount + " polling attempts"));
                        break;
                    }
                    log.warn("Task not found: {} (attempt {}/{}). Retrying...",
                        taskId, taskNotFoundCount, MAX_TASK_NOT_FOUND_RETRIES);
                    Thread.sleep(pollIntervalMs);
                    continue;
                }

                if (shouldExitPolling(taskJsonNode.get(), listener, destIndex)) {
                    break;
                }
                Thread.sleep(pollIntervalMs);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Reindex polling interrupted for task {}", taskId, ie);
            safeOnFailure(listener, new RuntimeException("Reindex polling interrupted", ie));
        } catch (IOException ioe) {
            log.error("Reindex process failed for task {}", taskId, ioe);
            safeOnFailure(listener, new RuntimeException("Reindex process failed: " + ioe.getLocalizedMessage(), ioe));
        } catch (Exception ex) {
            log.error("Unexpected error during reindex monitoring for task {}", taskId, ex);
            safeOnFailure(listener, ex);
        }
    }

    private void safeOnFailure(ReindexListener listener, Exception ex) {
        try {
            listener.onFailure(ex);
        } catch (Exception callbackEx) {
            log.error("Exception thrown inside ReindexListener.onFailure callback", callbackEx);
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
        } catch (ResponseException re) {
            int statusCode = re.getResponse().getStatusLine().getStatusCode();
            if (statusCode == 404) {
                String body = EntityUtils.toString(re.getResponse().getEntity(), StandardCharsets.UTF_8);
                if (body != null && body.contains(COMPLETED_TASK)) {
                    log.info("Task {} completed before results could be retrieved — treating as successful", taskId);
                    ObjectNode synthetic = mapper.createObjectNode();
                    synthetic.put("completed", true);
                    synthetic.putObject("response").put("total", 0).putArray("failures");
                    return Optional.of(synthetic);
                }
                log.warn("Task {} returned 404: {}", taskId, body);
                return Optional.empty();
            }
            throw re;
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            log.error("Unexpected error fetching task response for task {}", taskId, e);
            throw new IOException("Failed to fetch task response", e);
        }
    }

    private boolean shouldExitPolling(JsonNode taskResponse,
                                      ReindexListener listener,
                                      String destIndex) {
        if (!taskResponse.path("completed").asBoolean(false)) {
            return false;
        }
        handleCompletion(taskResponse, listener, destIndex);
        return true;
    }

    private void handleCompletion(JsonNode taskJson, ReindexListener listener, String destIndex) {
        JsonNode errorNode = taskJson.path("error");
        JsonNode responseNode = taskJson.path("response");

        if (hasValue(errorNode)) {
            log.error("Reindex task error for index {}: {}", destIndex, errorNode);
            safeOnFailure(listener, new RuntimeException("Task failed with error: " + errorNode));
            return;
        }

        if (!hasValue(responseNode)) {
            log.error("Reindex completed with unknown status for index {}: {}", destIndex, taskJson);
            safeOnFailure(listener, new RuntimeException("Unknown reindex status: " + taskJson));
            return;
        }

        JsonNode failuresNode = responseNode.path(FAILURES);
        if (failuresNode.isArray() && !failuresNode.isEmpty()) {
            log.error("Reindex failures for index {}: {}", destIndex, failuresNode);
            safeOnFailure(listener, new RuntimeException("Reindex failed: " + failuresNode));
            return;
        }

        logProgress(destIndex, responseNode);
        try {
            listener.onSuccess(responseNode.asText());
            log.info("Reindex completed successfully for index {}", destIndex);
        } catch (Exception ex) {
            log.error("onSuccess callback failed for index {}", destIndex, ex);
        }
    }

    private boolean hasValue(JsonNode node) {
        return !node.isMissingNode() && !node.isNull();
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
