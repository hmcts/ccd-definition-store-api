package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.springframework.http.ResponseEntity;

import java.util.UUID;

public record ProcessUploadResult(ResponseEntity<String> response, UUID jobId) {
}
