package uk.gov.hmcts.ccd.definition.store.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;

import static org.springframework.http.HttpMethod.GET;

@Service
public class IdamProfileClient {

    private final SecurityUtils securityUtils;
    private final RestTemplate restTemplate;
    private ApplicationParams applicationParams;

    @Autowired IdamProfileClient(final SecurityUtils securityUtils,
                                 final RestTemplate restTemplate,
                                 final ApplicationParams applicationParams) {

        this.securityUtils = securityUtils;
        this.restTemplate = restTemplate;
        this.applicationParams = applicationParams;
    }

    public IdamProperties getLoggedInUserDetails() {
        final HttpEntity<ServiceAndUserDetails>
            requestEntity =
            new HttpEntity<>(securityUtils.userAuthorizationHeaders());
        return restTemplate.exchange(applicationParams.idamUserProfileURL(), GET, requestEntity, IdamProperties.class)
                           .getBody();
    }

}
