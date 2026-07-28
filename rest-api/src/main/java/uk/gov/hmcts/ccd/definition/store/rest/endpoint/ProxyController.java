package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.rest.service.ProxyService;

import java.io.IOException;

@RestController
public class ProxyController {

    private ProxyService proxyService;

    @Autowired
    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @PostMapping(value = "/proxy", produces = {"application/json"})
    public String proxyRequest(@RequestBody String url) throws IOException {
        return proxyService.proxyRequest(url);
    }
}
