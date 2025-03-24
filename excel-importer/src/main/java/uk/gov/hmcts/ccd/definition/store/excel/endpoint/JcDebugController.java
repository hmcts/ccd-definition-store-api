package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class JcDebugController {

    @RequestMapping(value  =  "jcGetWithoutFile", method = RequestMethod.GET)
    public ResponseEntity<String> jcGetWithoutFile() {
        return new ResponseEntity<>("jcGetWithoutFile", HttpStatus.OK);
    }

    @RequestMapping(value  =  "jcPostWithoutFile", method = RequestMethod.POST)
    public ResponseEntity<String> jcPostWithoutFile() {
        return new ResponseEntity<>("jcPostWithoutFile", HttpStatus.OK);
    }


    @RequestMapping(value  =  "jcGetWithFile", method = RequestMethod.GET)
    public ResponseEntity<String> jcGetWithFile(@RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>("jcGetWithFile", HttpStatus.OK);
    }

    @RequestMapping(value  =  "jcPostWithFile", method = RequestMethod.POST)
    public ResponseEntity<String> jcPostWithFile(@RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>("jcPostWithFile", HttpStatus.OK);
    }
}
