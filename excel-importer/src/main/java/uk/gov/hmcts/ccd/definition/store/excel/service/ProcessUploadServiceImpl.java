package uk.gov.hmcts.ccd.definition.store.excel.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.domain.service.ImportJobService;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.ImportJobFailedException;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProcessUploadServiceImpl implements ProcessUploadService {

    public static final String ELASTICSEARCH_REINDEX_TASK_HEADER = "Elasticsearch-Reindex-Task";

    private final ImportWorkService importWorkService;
    private final ImportJobService importJobService;
    private final IdamProfileClient idamProfileClient;

    @Autowired
    public ProcessUploadServiceImpl(ImportWorkService importWorkService,
                                    ImportJobService importJobService,
                                    IdamProfileClient idamProfileClient) {
        this.importWorkService = importWorkService;
        this.importJobService = importJobService;
        this.idamProfileClient = idamProfileClient;
    }

    @Override
    public ProcessUploadResult processUpload(MultipartFile file, boolean reindex, boolean deleteOldIndex,
                                             UUID providedJobId) throws IOException {

        if (file == null || file.getSize() == 0) {
            throw new IOException(IMPORT_FILE_ERROR);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final InputStream inputStream = file.getInputStream()) {
            IOUtils.copy(inputStream, baos);
        }
        byte[] bytes = baos.toByteArray();

        String submitterUid = idamProfileClient.getLoggedInUserDetails().getId();

        importJobService.expireStaleJobs();

        UUID jobId = importJobService.createPending(providedJobId, submitterUid);

        ImportWorkResult workResult;
        try {
            workResult = importWorkService.doImport(bytes, file, reindex, deleteOldIndex, jobId);
        } catch (RuntimeException | IOException ex) {
            try {
                importJobService.markFailed(jobId, ex.getMessage());
            } catch (Exception markFailedEx) {
                log.error("markFailed failed for job {}. Propagating original exception", jobId, markFailedEx);
            }
            throw new ImportJobFailedException(jobId, ex);
        }

        try {
            importJobService.markCompleted(jobId, workResult.warnings(), workResult.metadata().getTaskId());
        } catch (Exception ex) {
            log.error("Import {} committed successfully but markCompleted failed. "
                    + "Job row may be flipped to EXPIRED by sweep despite successful import", jobId, ex);
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(HttpStatus.CREATED);

        List<String> warnings = workResult.warnings();
        if (!warnings.isEmpty()) {
            for (String warning : warnings) {
                log.warn(warning);
            }
            responseBuilder.header(IMPORT_WARNINGS_HEADER, warnings.toArray(new String[0]));
        }

        if (reindex) {
            responseBuilder.header(ELASTICSEARCH_REINDEX_TASK_HEADER, workResult.metadata().getTaskId());
        }

        return new ProcessUploadResult(responseBuilder.body(SUCCESSFULLY_CREATED), jobId);
    }
}
