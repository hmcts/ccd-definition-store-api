package uk.gov.hmcts.ccd.definition.store.security.idam;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.exceptions.ServiceException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Component
@Slf4j
public class IdamRepository {

    private final IdamClient idamClient;

    @Autowired
    public IdamRepository(IdamClient idamClient) {
        this.idamClient = idamClient;
    }

    private void jclog(final String message) {
        log.info("JCDEBUG: info: IdamRepository: {}", message);
        log.error("JCDEBUG: info: IdamRepository: {}", message);
    }

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String jwtToken) {
        try {
            return idamClient.getUserInfo("Bearer " + jwtToken);
        } catch (FeignException e) {
            jclog("FEIGN EXCEPTION: " + e.getMessage());
            log.error("FeignException: retrieve user info: {}", e.getMessage());

            if (isClientError(e)) {
                throw new InvalidTokenException(e.getMessage(), e);
            } else {
                throw new ServiceException(e.getMessage(), e);
            }
        } catch (Exception e) {
            jclog("EXCEPTION: " + e.getMessage());
            throw new ServiceException(e.getMessage(), e);
        }
    }

    private boolean isClientError(FeignException feignException) {
        return feignException.status() >= 400 && feignException.status() <= 499;
    }
}
