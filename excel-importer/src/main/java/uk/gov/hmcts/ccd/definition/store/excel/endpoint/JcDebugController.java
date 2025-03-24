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
@Api(value = "jcdebug")
public class JcDebugController {

    @RequestMapping(value = "jcdebug", method = RequestMethod.POST)
    public ResponseEntity<String> jcdebug(@RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>("JcDebugController (POST with RequestParam file)", HttpStatus.OK);
    }
}
