package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.domain.service.ImportJobService;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobEntity;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileClient;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
public class ImportJobController {

    private final ImportJobService importJobService;
    private final IdamProfileClient idamProfileClient;

    public ImportJobController(ImportJobService importJobService, IdamProfileClient idamProfileClient) {
        this.importJobService = importJobService;
        this.idamProfileClient = idamProfileClient;
    }

    @GetMapping("/import-jobs/{id}")
    public ResponseEntity<Object> getImportJob(@PathVariable("id") String idString) {
        UUID uuid;
        try {
            uuid = UUID.fromString(idString);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                .body("Invalid import job ID: must be a valid UUID");
        }

        String callerUid = idamProfileClient.getLoggedInUserDetails().getId();
        importJobService.expireStaleJobs();

        Optional<ImportJobEntity> optionalImportJobEntity = importJobService.findById(uuid);
        if (optionalImportJobEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Import job not found");
        }

        ImportJobEntity importJobEntity = optionalImportJobEntity.get();
        if (!importJobEntity.getSubmittedBy().equals(callerUid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return ResponseEntity.ok(ImportJobResponse.from(importJobEntity));
    }
}
