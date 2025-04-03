package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JcTestController {

    @GetMapping("/jctest")
    public ResponseEntity<String> jctest() {
        return ResponseEntity.ok("JcTestController.jctest()");
    }
}
