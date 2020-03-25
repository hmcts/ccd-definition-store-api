package uk.gov.hmcts.ccd.definition.store.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
public class IdamProfileClient {

    private final SecurityUtils securityUtils;

    @Autowired IdamProfileClient(final SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    public IdamProperties getLoggedInUserDetails() {
        return toIdamProperties(securityUtils.getUserInfo());
    }

    private IdamProperties toIdamProperties(UserInfo userInfo) {
        IdamProperties idamProperties = new IdamProperties();
        idamProperties.setId(userInfo.getUid());
        idamProperties.setEmail(userInfo.getSub());
        idamProperties.setForename(userInfo.getGivenName());
        idamProperties.setSurname(userInfo.getFamilyName());
        idamProperties.setRoles(userInfo.getRoles().toArray(new String[0]));
        return idamProperties;
    }

}
