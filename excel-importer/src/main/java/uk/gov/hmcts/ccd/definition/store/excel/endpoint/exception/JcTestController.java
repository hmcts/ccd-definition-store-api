package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JcTestController {

    private static final Logger LOG = LoggerFactory.getLogger(JcTestController.class);

    private void jclog(final String message) {
        LOG.info("JCDEBUG: info: JcTestController: {}", message);
    }

    /*
     * Test using "curl https://ccd-definition-store-api-pr-1538.preview.platform.hmcts.net/jctest"
     */
    @GetMapping("/jctest")
    public ResponseEntity<String> jctest() {
        try {
            Thread.sleep(300000);  // 5 minutes
        } catch (InterruptedException e) {
            jclog("jctest() InterruptedException: " + e.getMessage());
        }
        ResponseEntity responseEntity = ResponseEntity.ok("JcTestController.jctest()");
        jclog("jctest() " + responseEntity.getStatusCode() + "  ,  " + responseEntity.getBody());
        return responseEntity;
    }
}
